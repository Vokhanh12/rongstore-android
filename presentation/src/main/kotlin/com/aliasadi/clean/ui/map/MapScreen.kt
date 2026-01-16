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
import org.maplibre.android.style.layers.*
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
    "https://7c9a9eaa8942.ngrok-free.app/v1/store-owners:search-by-tiles"

private const val ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJtVW5SOGhMQ0FnUEMybjJpNVhVSmVHS1BjYllRZHk4SDJLZ2o0V0ZvU1k0In0.eyJleHAiOjE3Njg1NzA4MTUsImlhdCI6MTc2ODU2OTAxNSwianRpIjoiNzY4ZWM1YjktYzVhZi00M2RiLWJlNDctYWM1ZjJlMWYxZTg0IiwiaXNzIjoiaHR0cDovLzEwMC4xMTQuMzEuMzA6OTA5MC9yZWFsbXMvcm9uZ3N0b3JlLXJlYWxtIiwiYXVkIjoiYWNjb3VudCIsInN1YiI6IjI1MDkxMTQ1LTVjYTgtNDE2MS05NzQ4LWI3YjcwYmI3Nzg0YyIsInR5cCI6IkJlYXJlciIsImF6cCI6InJvbmdzdG9yZS1zZXJ2aWNlIiwic2lkIjoiYzYxMTk5YzgtZjYxMC00MDAzLWFkMmEtMjdlNjhmZGI1ZTcyIiwiYWNyIjoiMSIsImFsbG93ZWQtb3JpZ2lucyI6WyIvKiJdLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsiZGVmYXVsdC1yb2xlcy1yb25nc3RvcmUtcmVhbG0iLCJvZmZsaW5lX2FjY2VzcyIsInVtYV9hdXRob3JpemF0aW9uIl19LCJyZXNvdXJjZV9hY2Nlc3MiOnsicm9uZ3N0b3JlLXNlcnZpY2UiOnsicm9sZXMiOlsiVXNlciJdfSwiYWNjb3VudCI6eyJyb2xlcyI6WyJtYW5hZ2UtYWNjb3VudCIsIm1hbmFnZS1hY2NvdW50LWxpbmtzIiwidmlldy1wcm9maWxlIl19fSwic2NvcGUiOiJvcGVuaWQgZW1haWwgcHJvZmlsZSIsImVtYWlsX3ZlcmlmaWVkIjpmYWxzZSwicHJlZmVycmVkX3VzZXJuYW1lIjoidm9raGFuaDEyIn0.BnHmPNW7S6cPvc2GqTy4y9FJg3WQroipnLK0FJuLVt7bVQ7k4DePdHJ_ATZJEmgQmYaPcIBxUI66pjKMJX5gzMMc6Z9EhDd-VsngimOweIhaZarylDGJOynVSfZl5Jcm4VURC1CVsW4G2R5cqMyOvd2HIPxsXx-4hC1Yh0ly7ie8vFHl5PJ5-5yWhW_nJ4N8YptyUpl_IcYIOdacNF-zPYqgLygmbHoQ9farEyvZgDCG2iqrjC_IhlYf2KkV8gpmqtTfbGZYdSghwbYoC3PgGhMj5LIU99QOgJPdN4k0T63JAlTf0KY8FPzgHc-OW87W5sXGSEakksIzlnCm9o7iJA"

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

data class TileBounds(
    val north: Double,
    val south: Double,
    val west: Double,
    val east: Double
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

    Scaffold { padding ->
        AndroidView(
            modifier = Modifier.fillMaxSize().padding(padding),
            factory = {
                MapView(context).apply {
                    onCreate(null)
                    getMapAsync { m ->
                        map = m
                        m.setStyle(
                            Style.Builder().fromUri(state.mapStyleUrl)
                        ) { s ->
                            style = s
                        }
                    }
                }.also { mapView = it }
            }
        )
    }

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
                if (zoom < 14) return@launch

                val corners = getMapCorners(m, mv)
                val range = tileRangeFromCorners(corners, zoom, 1)

                Log.d(TAG_TILE, "z=$zoom x=${range.minX}..${range.maxX} y=${range.minY}..${range.maxY}")

                showDebugTiles(s, zoom, range)

                if (tileCache.has(range, zoom)) return@launch
                tileCache.put(range, zoom)

                val items = fetchStoreOwners(zoom, range)
                showStoreOwners(s, items)
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
    val y =
        ((1 - ln(tan(Math.toRadians(lat)) + 1 / cos(Math.toRadians(lat))) / Math.PI) / 2 * n).toInt()
    return TileXY(x, y)
}

fun tileRangeFromCorners(c: MapCorners, zoom: Int, padding: Int): TileRange {
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

fun tileBounds(x: Int, y: Int, z: Int): TileBounds {
    val n = 1 shl z
    val west = x.toDouble() / n * 360 - 180
    val east = (x + 1).toDouble() / n * 360 - 180

    val north = atan(sinh(Math.PI * (1 - 2.0 * y / n)))
    val south = atan(sinh(Math.PI * (1 - 2.0 * (y + 1) / n)))

    return TileBounds(
        Math.toDegrees(north),
        Math.toDegrees(south),
        west,
        east
    )
}

fun tileCenterLatLng(x: Int, y: Int, z: Int): LatLng {
    val b = tileBounds(x, y, z)
    return LatLng((b.north + b.south) / 2, (b.west + b.east) / 2)
}

fun tilePolygon(x: Int, y: Int, z: Int): JSONArray {
    val b = tileBounds(x, y, z)
    return JSONArray()
        .put(JSONArray().put(b.west).put(b.north))
        .put(JSONArray().put(b.east).put(b.north))
        .put(JSONArray().put(b.east).put(b.south))
        .put(JSONArray().put(b.west).put(b.south))
        .put(JSONArray().put(b.west).put(b.north))
}

/* =======================================================
   DEBUG TILE GRID + LABEL
======================================================= */

fun showDebugTiles(style: Style, zoom: Int, range: TileRange) {

    val features = JSONArray()

    for (x in range.minX..range.maxX) {
        for (y in range.minY..range.maxY) {

            val center = tileCenterLatLng(x, y, zoom)

            features.put(JSONObject().apply {
                put("type", "Feature")
                put("geometry", JSONObject().apply {
                    put("type", "Point")
                    put("coordinates", JSONArray().put(center.longitude).put(center.latitude))
                })
                put("properties", JSONObject().apply {
                    put("label", "z:$zoom\nx:$x\ny:$y")
                })
            })

            features.put(JSONObject().apply {
                put("type", "Feature")
                put("geometry", JSONObject().apply {
                    put("type", "LineString")
                    put("coordinates", tilePolygon(x, y, zoom))
                })
            })
        }
    }

    val src = "debug-tiles"

    val fc = JSONObject().apply {
        put("type", "FeatureCollection")
        put("features", features)
    }

    val source = style.getSource(src) as? GeoJsonSource

    if (source == null) {
        style.addSource(GeoJsonSource(src, fc.toString()))

        style.addLayer(
            LineLayer("tile-line", src).withProperties(
                lineColor("#00FFFF"),
                lineWidth(1.2f)
            )
        )

        style.addLayer(
            SymbolLayer("tile-text", src).withProperties(
                textField("{label}"),
                textSize(12f),
                textColor("#FF9500"),
                textHaloColor("#000000"),
                textHaloWidth(1.5f),
                textAllowOverlap(true)
            )
        )
    } else {
        source.setGeoJson(fc.toString())
    }
}

/* =======================================================
   STORE MARKERS
======================================================= */

fun showStoreOwners(style: Style, items: JSONArray) {

    if (items.length() == 0) return

    val features = JSONArray()

    for (i in 0 until items.length()) {
        val o = items.getJSONObject(i)
        features.put(JSONObject().apply {
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
        })
    }

    val fc = JSONObject().apply {
        put("type", "FeatureCollection")
        put("features", features)
    }

    val srcId = "store-source"
    val layerId = "store-layer"

    // 🔥 REMOVE CŨ
    style.getLayer(layerId)?.let { style.removeLayer(it) }
    style.getSource(srcId)?.let { style.removeSource(it) }

    // 🔥 ADD MỚI
    style.addSource(GeoJsonSource(srcId, fc.toString()))
    style.addLayer(
        CircleLayer(layerId, srcId).withProperties(
            circleRadius(6f),
            circleColor("#34C759"),
            circleStrokeColor("#000000"),
            circleStrokeWidth(1f)
        )
    )
}

/* =======================================================
   API
======================================================= */

suspend fun fetchStoreOwners(zoom: Int, range: TileRange): JSONArray =
    withContext(Dispatchers.IO) {

        val body = JSONObject().apply {
            put("zoom", zoom)
            put("tile_range", JSONObject().apply {
                put("min_x", range.minX)
                put("max_x", range.maxX)
                put("min_y", range.minY)
                put("max_y", range.maxY)
            })
        }

        Log.d(TAG_API, "REQUEST $body")

        val conn = URL(API_URL).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.setRequestProperty("Content-Type", "application/json")
            conn.setRequestProperty("Authorization", "Bearer $ACCESS_TOKEN")
            conn.doOutput = true

            conn.outputStream.use { it.write(body.toString().toByteArray()) }

            val res = conn.inputStream.bufferedReader().readText()
            Log.d(TAG_API, "RESPONSE $res")

            JSONObject(res).optJSONObject("data")?.optJSONArray("items") ?: JSONArray()
        } catch (e: Exception) {
            Log.e(TAG_API, "ERROR", e)
            JSONArray()
        } finally {
            conn.disconnect()
        }
    }

/* =======================================================
   USER & CACHE
======================================================= */

class TileCache {
    private val set = HashSet<String>()
    fun has(r: TileRange, z: Int) = set.contains("$z:$r")
    fun put(r: TileRange, z: Int) { set.add("$z:$r") }
}

fun moveCameraToUser(map: MapLibreMap, pos: LatLng) {
    map.moveCamera(CameraUpdateFactory.newCameraPosition(
        CameraPosition.Builder().target(pos).zoom(DEFAULT_ZOOM).build()
    ))
}

fun safeAddUser(style: Style, pos: LatLng) {
    if (style.getSource("user") != null) return
    style.addSource(
        GeoJsonSource("user",
            """{"type":"FeatureCollection","features":[{"type":"Feature","geometry":{"type":"Point","coordinates":[${pos.longitude},${pos.latitude}]}}]}"""
        )
    )
    style.addLayer(
        CircleLayer("user-layer", "user")
            .withProperties(circleRadius(8f), circleColor("#FF3B30"))
    )
}
