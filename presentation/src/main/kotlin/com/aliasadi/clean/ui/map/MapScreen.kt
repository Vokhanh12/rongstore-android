package com.aliasadi.clean.ui.map

import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.aliasadi.clean.ui.main.MainRouter
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.*
import org.maplibre.android.style.layers.CircleLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.*

/* =======================================================
   TAGS
======================================================= */

private const val TAG_API = "MAP_API"
private const val TAG_CAMERA = "MAP_CAMERA"
private const val TAG_TILE = "MAP_TILE"

/* =======================================================
   CONSTANTS
======================================================= */

private val DEFAULT_USER_POS = LatLng(10.359721, 106.679593)
private const val DEFAULT_ZOOM = 16.0
private const val API_URL =
    "https://d469f0d7f4ab.ngrok-free.app/v1/store-owners:search-by-tiles"

private const val ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJtVW5SOGhMQ0FnUEMybjJpNVhVSmVHS1BjYllRZHk4SDJLZ2o0V0ZvU1k0In0.eyJleHAiOjE3Njg0MDA5NzQsImlhdCI6MTc2ODM5OTE3NCwianRpIjoiYmY4YjEwY2MtYzFlNS00MDcyLTg1YTQtNDQ1ZTcwMmIzMzI0IiwiaXNzIjoiaHR0cDovLzEwMC4xMTQuMzEuMzA6OTA5MC9yZWFsbXMvcm9uZ3N0b3JlLXJlYWxtIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjI1MDkxMTQ1LTVjYTgtNDE2MS05NzQ4LWI3YjcwYmI3Nzg0YyIsInR5cCI6IkJlYXJlciIsImF6cCI6InJvbmdzdG9yZS1zZXJ2aWNlIiwic2lkIjoiNmI4MzBiMjEtYjM4MC00NGY2LWIxZjYtNWUwMGM3OWIxYmJmIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIvKiJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1yb25nc3RvcmUtcmVhbG0iLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsicm9uZ3N0b3JlLXNlcnZpY2UiOnsicm9sZXMiOlsiVXNlciJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgZW1haWwgcHJvZmlsZSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoidm9raGFuaDEyIn0.nWWCoSqezXKYtCbI0s5-AManCgURi-Hba2LzqVZ5iv50CizxH6HBolZFO3dydSr9DyTaXcq79WEpbvni2K0zzyVvildgL00cieJTs6W6pBJRG75HeldlFX127X858vLpu-60AYPleHPWGohX3ppb4S7V7C8JfiXR3q6XpNP0dOq8W2LFgGJn70XAwCAnBp6C9Q4n8KVQ1snrDEQRn8JfLXrsEZclb2Ibp7tYK4E3YeJMrVfd5CHfYvAbsNdsYx_zrymIPz9lsDT4UuYda97ar4XIAcier-ga04lBa5EJ4EDV9Cq6YjX6QoLscPrbBCCXxVndgjL3hQT_kmPREo7rPg"

/* =======================================================
   DATA MODELS
======================================================= */

data class MapCorners(
    val topLeft: LatLng,
    val topRight: LatLng,
    val bottomLeft: LatLng,
    val bottomRight: LatLng
)

data class TileXY(val x: Int, val y: Int)

data class TileRange(
    val minX: Int,
    val maxX: Int,
    val minY: Int,
    val maxY: Int,
)

/* =======================================================
   PAGE
======================================================= */

@Composable
fun MapPage(
    mainRouter: MainRouter,
    viewModel: MapViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    MapScreen(uiState)
}

/* =======================================================
   SCREEN
======================================================= */

@Composable
fun MapScreen(state: MapUIState) {

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var style by remember { mutableStateOf<Style?>(null) }

    val tileCache = remember { TileCache() }
    var cameraJob by remember { mutableStateOf<Job?>(null) }

    Scaffold() { padding ->

        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding),
            factory = {
                MapView(context).apply {
                    onCreate(null)
                    getMapAsync { m ->
                        map = m
                        m.setStyle(Style.Builder().fromUri(state.mapStyleUrl)) {
                            style = it
                        }
                    }
                }.also { mapView = it }
            }
        )
    }

    /* ================= CAMERA LISTENER ================= */

    LaunchedEffect(style) {

        val m = map ?: return@LaunchedEffect
        val s = style ?: return@LaunchedEffect
        val mv = mapView ?: return@LaunchedEffect

        moveCameraToUser(m, DEFAULT_USER_POS)
        safeAddUser(s, DEFAULT_USER_POS)

        m.addOnCameraIdleListener {

            cameraJob?.cancel()
            cameraJob = scope.launch {

                delay(250)

                val zoom = m.cameraPosition.zoom.toInt()
                if (zoom < 14) {
                    Log.d(TAG_CAMERA, "Zoom too small ($zoom) → skip")
                    return@launch
                }

                val corners = getMapCorners(m, mv)
                val range = tileRangeFromCorners(corners, zoom, padding = 1)

                Log.d(
                    TAG_TILE,
                    "Tile z=$zoom x=[${range.minX},${range.maxX}] y=[${range.minY},${range.maxY}]"
                )

                if (tileCache.has(range, zoom)) {
                    Log.d(TAG_TILE, "Tile cache HIT → skip API")
                    return@launch
                }

                tileCache.put(range, zoom)

                val items = fetchStoreOwners(zoom, range)
                Log.d(TAG_API, "API success → items=${items.length()}")

                showStoreOwners(
                    style = s,
                    items = items,
                    zoom = zoom,
                    range = range
                )
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { mapView?.onDestroy() }
    }
}

/* =======================================================
   VIEWPORT
======================================================= */

fun getMapCorners(map: MapLibreMap, mapView: MapView): MapCorners {
    val w = mapView.width.toFloat()
    val h = mapView.height.toFloat()
    val p = map.projection

    return MapCorners(
        p.fromScreenLocation(PointF(0f, 0f)),
        p.fromScreenLocation(PointF(w, 0f)),
        p.fromScreenLocation(PointF(0f, h)),
        p.fromScreenLocation(PointF(w, h)),
    )
}

/* =======================================================
   TILE MATH
======================================================= */

fun latLngToTile(lat: Double, lng: Double, zoom: Int): TileXY {
    val n = 1 shl zoom
    val x = ((lng + 180) / 360 * n).toInt()
    val latRad = Math.toRadians(lat)
    val y =
        ((1 - ln(tan(latRad) + 1 / cos(latRad)) / Math.PI) / 2 * n).toInt()
    return TileXY(x, y)
}

fun tileRangeFromCorners(
    c: MapCorners,
    zoom: Int,
    padding: Int
): TileRange {

    val tiles = listOf(
        latLngToTile(c.topLeft.latitude, c.topLeft.longitude, zoom),
        latLngToTile(c.topRight.latitude, c.topRight.longitude, zoom),
        latLngToTile(c.bottomLeft.latitude, c.bottomLeft.longitude, zoom),
        latLngToTile(c.bottomRight.latitude, c.bottomRight.longitude, zoom),
    )

    return TileRange(
        tiles.minOf { it.x } - padding,
        tiles.maxOf { it.x } + padding,
        tiles.minOf { it.y } - padding,
        tiles.maxOf { it.y } + padding,
    )
}

/* =======================================================
   API
======================================================= */

suspend fun fetchStoreOwners(
    zoom: Int,
    range: TileRange,
): JSONArray = withContext(Dispatchers.IO) {

    val body = JSONObject().apply {
        put("zoom", zoom)
        put(
            "tile_range",
            JSONObject().apply {
                put("min_x", range.minX)
                put("max_x", range.maxX)
                put("min_y", range.minY)
                put("max_y", range.maxY)
            }
        )
    }

    Log.d(TAG_API, "REQUEST → $body")

    val conn = URL(API_URL).openConnection() as HttpURLConnection

    try {
        conn.requestMethod = "POST"
        conn.setRequestProperty("Content-Type", "application/json")
        conn.setRequestProperty("Authorization", "Bearer $ACCESS_TOKEN")
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        conn.doOutput = true

        conn.outputStream.use {
            it.write(body.toString().toByteArray())
        }

        val code = conn.responseCode
        val stream =
            if (code in 200..299) conn.inputStream else conn.errorStream

        val responseText = stream.bufferedReader().readText()
        Log.d(TAG_API, "RESPONSE($code) → $responseText")

        val root = JSONObject(responseText)
        val data = root.optJSONObject("data") ?: return@withContext JSONArray()
        val items = data.optJSONArray("items") ?: JSONArray()

        if (items.length() > 0) {
            Log.d(TAG_API, "Sample item → ${items.getJSONObject(0)}")
        }

        items

    } catch (e: Exception) {
        Log.e(TAG_API, "API ERROR", e)
        JSONArray()
    } finally {
        conn.disconnect()
    }
}

/* =======================================================
   MAP RENDER
======================================================= */

fun showStoreOwners(
    style: Style,
    items: JSONArray,
    zoom: Int,
    range: TileRange,
) {

    if (items.length() == 0) {
        Log.w(TAG_API, "No markers to render")
        return
    }

    val features = JSONArray()

    for (i in 0 until items.length()) {
        val o = items.getJSONObject(i)
        features.put(
            JSONObject().apply {
                put("type", "Feature")
                put("geometry", JSONObject().apply {
                    put("type", "Point")
                    put(
                        "coordinates",
                        JSONArray()
                            .put(o.getDouble("lng"))
                            .put(o.getDouble("lat"))
                    )
                })
            }
        )
    }

    val sourceId =
        "store-z${zoom}-x${range.minX}-${range.maxX}-y${range.minY}-${range.maxY}"
    val layerId = "$sourceId-layer"

    val fc = JSONObject().apply {
        put("type", "FeatureCollection")
        put("features", features)
    }

    Log.d(TAG_API, "Render markers → source=$sourceId count=${features.length()}")

    if (style.getSource(sourceId) == null) {

        style.addSource(GeoJsonSource(sourceId, fc.toString()))

        style.addLayer(
            CircleLayer(layerId, sourceId)
                .withProperties(
                    circleRadius(6f),
                    circleColor("#34C759"),
                    circleOpacity(0.9f)
                )
        )
    }
}

/* =======================================================
   TILE CACHE
======================================================= */

class TileCache {
    private val set = HashSet<String>()

    fun has(range: TileRange, zoom: Int): Boolean =
        set.contains(key(range, zoom))

    fun put(range: TileRange, zoom: Int) {
        set.add(key(range, zoom))
    }

    private fun key(r: TileRange, z: Int) =
        "$z:${r.minX}:${r.maxX}:${r.minY}:${r.maxY}"
}

/* =======================================================
   CAMERA & USER
======================================================= */

fun moveCameraToUser(map: MapLibreMap, pos: LatLng) {
    map.moveCamera(
        CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .target(pos)
                .zoom(DEFAULT_ZOOM)
                .build()
        )
    )
}

fun safeAddUser(style: Style, pos: LatLng) {
    if (style.getSource("user") != null) return

    style.addSource(
        GeoJsonSource(
            "user",
            """{"type":"FeatureCollection","features":[{
               "type":"Feature",
               "geometry":{"type":"Point","coordinates":[${pos.longitude},${pos.latitude}]}
            }]}"""
        )
    )

    style.addLayer(
        CircleLayer("user-layer", "user")
            .withProperties(circleRadius(8f), circleColor("#FF3B30"))
    )
}
