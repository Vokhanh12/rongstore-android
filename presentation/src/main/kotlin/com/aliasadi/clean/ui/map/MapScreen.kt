package com.aliasadi.clean.ui.map

import android.graphics.PointF
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aliasadi.clean.ui.main.MainRouter
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.*
import org.maplibre.android.style.expressions.Expression.get
import org.maplibre.android.style.layers.*
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import kotlin.math.*

/* =======================================================
   CONSTANTS
======================================================= */

private val DEFAULT_USER_POS = LatLng(10.359721, 106.679593)
private const val DEFAULT_ZOOM = 16.0

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
data class TileKey(val z: Int, val x: Int, val y: Int)

data class TileLatLngBounds(
    val north: Double,
    val south: Double,
    val west: Double,
    val east: Double
)

/* =======================================================
   PAGE
======================================================= */

@OptIn(ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(state: MapUIState) {

    val context = LocalContext.current

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var style by remember { mutableStateOf<Style?>(null) }

    val tileCache = remember { TileCache() }
    val prefetchManager = remember {
        TilePrefetchManager(CoroutineScope(Dispatchers.Main), tileCache)
    }

    var cameraJob by remember { mutableStateOf<Job?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(state.title) }) }
    ) { padding ->

        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
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

    /* ---------------- Camera listener ---------------- */

    LaunchedEffect(style) {
        val m = map ?: return@LaunchedEffect
        val s = style ?: return@LaunchedEffect
        val mv = mapView ?: return@LaunchedEffect

        moveCameraToUser(m, DEFAULT_USER_POS)
        safeAddUser(s, DEFAULT_USER_POS)

        m.addOnCameraIdleListener {
            cameraJob?.cancel()
            cameraJob = CoroutineScope(Dispatchers.Main).launch {
                delay(200)

                val corners = getMapCorners(m, mv)
                val zoom = m.cameraPosition.zoom.toInt()

                showTileDebugGrid(s, corners, zoom)

                prefetchManager.onViewportChanged(
                    corners = corners,
                    zoom = zoom,
                    padding = 1
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

fun tileToLatLngBounds(x: Int, y: Int, zoom: Int): TileLatLngBounds {
    val n = 1 shl zoom

    val west = x.toDouble() / n * 360 - 180
    val east = (x + 1).toDouble() / n * 360 - 180

    val northRad = atan(sinh(Math.PI * (1 - 2.0 * y / n)))
    val southRad = atan(sinh(Math.PI * (1 - 2.0 * (y + 1) / n)))

    return TileLatLngBounds(
        Math.toDegrees(northRad),
        Math.toDegrees(southRad),
        west,
        east
    )
}

fun tilesInViewport(c: MapCorners, zoom: Int): List<Pair<Int, Int>> {
    val tiles = listOf(
        latLngToTile(c.topLeft.latitude, c.topLeft.longitude, zoom),
        latLngToTile(c.topRight.latitude, c.topRight.longitude, zoom),
        latLngToTile(c.bottomLeft.latitude, c.bottomLeft.longitude, zoom),
        latLngToTile(c.bottomRight.latitude, c.bottomRight.longitude, zoom),
    )

    val minX = tiles.minOf { it.x }
    val maxX = tiles.maxOf { it.x }
    val minY = tiles.minOf { it.y }
    val maxY = tiles.maxOf { it.y }

    return (minX..maxX).flatMap { x ->
        (minY..maxY).map { y -> x to y }
    }
}

/* =======================================================
   DEBUG GRID
======================================================= */

fun buildTileGridGeoJson(tiles: List<Pair<Int, Int>>, zoom: Int): String =
    """{
      "type":"FeatureCollection",
      "features":[${tiles.joinToString(",") { (x, y) ->
        val b = tileToLatLngBounds(x, y, zoom)
        """
        {
          "type":"Feature",
          "properties":{"label":"z=$zoom\nx=$x\ny=$y"},
          "geometry":{
            "type":"Polygon",
            "coordinates":[[
              [${b.west},${b.north}],
              [${b.east},${b.north}],
              [${b.east},${b.south}],
              [${b.west},${b.south}],
              [${b.west},${b.north}]
            ]]
          }
        }
        """
    }}]
    }"""

fun showTileDebugGrid(style: Style, corners: MapCorners, zoom: Int) {
    val srcId = "tile-debug-source"
    val geoJson = buildTileGridGeoJson(tilesInViewport(corners, zoom), zoom)

    if (style.getSource(srcId) == null) {
        style.addSource(GeoJsonSource(srcId, geoJson))
        style.addLayer(
            LineLayer("tile-debug-line", srcId)
                .withProperties(lineColor("#FF0000"), lineWidth(1.2f))
        )
        style.addLayer(
            SymbolLayer("tile-debug-text", srcId)
                .withProperties(textField(get("label")), textSize(12f), textAllowOverlap(true))
        )
    } else {
        style.getSourceAs<GeoJsonSource>(srcId)!!.setGeoJson(geoJson)
    }
}

/* =======================================================
   PREFETCH
======================================================= */

class TileCache(private val maxSize: Int = 200) {
    private val map = LinkedHashMap<TileKey, Unit>(16, 0.75f, true)
    fun has(t: TileKey) = map.containsKey(t)
    fun put(t: TileKey) {
        map[t] = Unit
        if (map.size > maxSize) map.remove(map.entries.first().key)
    }
}

class TilePrefetchManager(
    private val scope: CoroutineScope,
    private val cache: TileCache
) {
    fun onViewportChanged(corners: MapCorners, zoom: Int, padding: Int) {
        if (zoom < 14) return

        val tiles = tilesInViewport(corners, zoom)
        val expanded = tiles.flatMap { (x, y) ->
            (-padding..padding).flatMap { dx ->
                (-padding..padding).map { dy ->
                    TileKey(zoom, x + dx, y + dy)
                }
            }
        }

        val need = expanded.filterNot(cache::has)
        if (need.isEmpty()) return

        scope.launch {
            delay(600)
            need.forEach {
                Log.d("TILE_PREFETCH", it.toString())
                cache.put(it)
            }
        }
    }
}

/* =======================================================
   CAMERA & USER
======================================================= */

fun moveCameraToUser(map: MapLibreMap, pos: LatLng) {
    map.moveCamera(
        CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder().target(pos).zoom(DEFAULT_ZOOM).build()
        )
    )
}

fun safeAddUser(style: Style, pos: LatLng) {
    if (style.getSource("user-source") != null) return

    style.addSource(
        GeoJsonSource(
            "user-source",
            """{"type":"FeatureCollection","features":[{
               "type":"Feature",
               "geometry":{"type":"Point","coordinates":[${pos.longitude},${pos.latitude}]}
            }]}"""
        )
    )

    style.addLayer(
        CircleLayer("user-layer", "user-source")
            .withProperties(circleRadius(8f), circleColor("#FF3B30"))
    )
}
