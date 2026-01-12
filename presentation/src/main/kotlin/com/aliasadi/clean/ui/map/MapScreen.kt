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
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression.*
import org.maplibre.android.style.layers.*
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import org.maplibre.geojson.Point
import java.net.URL

// =======================================================
// CONSTANTS
// =======================================================

private val DEFAULT_USER_POS = LatLng(10.359721, 106.679593)
private const val DEFAULT_ZOOM = 16.0

data class TileXY(val x: Int, val y: Int)

data class TileLatLngBounds(
    val north: Double,
    val south: Double,
    val west: Double,
    val east: Double
)

// =======================================================
// VIEWPORT DEBUG DATA
// =======================================================

data class MapCorners(
    val topLeft: LatLng,
    val topRight: LatLng,
    val bottomLeft: LatLng,
    val bottomRight: LatLng
)

// =======================================================
// COMPOSABLES
// =======================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPage(
    mainRouter: MainRouter,
    viewModel: MapViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    MapScreen(state = uiState, viewModel = viewModel)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    state: MapUIState,
    viewModel: MapViewModel
) {
    val context = LocalContext.current

    var mapView by remember { mutableStateOf<MapView?>(null) }
    var map by remember { mutableStateOf<MapLibreMap?>(null) }
    var style by remember { mutableStateOf<Style?>(null) }

    // ===== VIEWPORT DEBUG STATE =====
    var mapCorners by remember { mutableStateOf<MapCorners?>(null) }

    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        topBar = { TopAppBar(title = { Text(state.title) }) }
    ) { padding ->

        Box(Modifier.fillMaxSize()) {

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

            // ===== DEBUG OVERLAY =====
            mapCorners?.let { c ->
                Card(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    )
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("🗺 Viewport Corners", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(8.dp))
                        Text("TopLeft     : ${c.topLeft.format()}")
                        Text("TopRight    : ${c.topRight.format()}")
                        Text("BottomLeft  : ${c.bottomLeft.format()}")
                        Text("BottomRight : ${c.bottomRight.format()}")
                    }
                }
            }
        }
    }

    // ===== INIT MAP =====
    LaunchedEffect(style) {
        val map = map ?: return@LaunchedEffect
        val style = style ?: return@LaunchedEffect
        val mapView = mapView ?: return@LaunchedEffect

        safeAddUser(style, DEFAULT_USER_POS)
        safeAddShops(style)
        moveCameraToUser(map, DEFAULT_USER_POS)

        bindShopClick(map) { shop ->
            viewModel.selectShop(shop)
        }

        // UPDATE VIEWPORT CORNERS
        map.addOnCameraIdleListener {
            val corners = getMapCorners(map, mapView)
            val zoom = map.cameraPosition.zoom.toInt()

            showTileDebugGrid(
                style = style,
                corners = corners,
                zoom = zoom
            )
        }
    }

    // ===== NAVIGATION FLOW =====
    LaunchedEffect(state.navigatingTo) {
        val shop = state.navigatingTo ?: return@LaunchedEffect
        val map = map ?: return@LaunchedEffect
        val style = style ?: return@LaunchedEffect

        val geometry = loadRoute(
            DEFAULT_USER_POS,
            LatLng(shop.lat, shop.lng)
        )
        val route = parseRoutePoints(geometry)

        clearRoute(style)
        addRoute(style, geometry)
        zoomToRoute(map, route)

        startNavigation(
            scope = this,
            map = map,
            style = style,
            route = route
        )

        viewModel.clearNavigation()
    }

    DisposableEffect(Unit) {
        onDispose { mapView?.onDestroy() }
    }
}

// =======================================================
// VIEWPORT UTILS
// =======================================================

fun getMapCorners(map: MapLibreMap, mapView: MapView): MapCorners {
    val w = mapView.width.toFloat()
    val h = mapView.height.toFloat()
    val p = map.projection

    return MapCorners(
        topLeft = p.fromScreenLocation(PointF(0f, 0f)),
        topRight = p.fromScreenLocation(PointF(w, 0f)),
        bottomLeft = p.fromScreenLocation(PointF(0f, h)),
        bottomRight = p.fromScreenLocation(PointF(w, h))
    )
}

fun LatLng.format(): String =
    "%.6f, %.6f".format(latitude, longitude)

// =======================================================
// CAMERA
// =======================================================

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

// =======================================================
// ROUTE
// =======================================================

fun clearRoute(style: Style) {
    style.removeLayer("route-layer")
    style.removeLayer("route-casing")
    style.removeSource("route-source")
}

suspend fun loadRoute(start: LatLng, end: LatLng): JSONObject =
    withContext(Dispatchers.IO) {

        val url =
            "http://100.114.31.30:5000/route/v1/driving/" +
                    "${start.longitude},${start.latitude};" +
                    "${end.longitude},${end.latitude}" +
                    "?geometries=geojson&overview=full"

        val response = URL(url).readText()

        JSONObject(response)
            .getJSONArray("routes")
            .getJSONObject(0)
            .getJSONObject("geometry")
    }

fun parseRoutePoints(geometry: JSONObject): List<LatLng> {
    val coords = geometry.getJSONArray("coordinates")
    val list = mutableListOf<LatLng>()
    for (i in 0 until coords.length()) {
        val c = coords.getJSONArray(i)
        list.add(LatLng(c.getDouble(1), c.getDouble(0)))
    }
    return list
}

fun addRoute(style: Style, geometry: JSONObject) {
    val feature = JSONObject().apply {
        put("type", "Feature")
        put("geometry", geometry)
    }

    val collection = JSONObject().apply {
        put("type", "FeatureCollection")
        put("features", JSONArray().put(feature))
    }

    style.addSource(GeoJsonSource("route-source", collection.toString()))

    val casing = LineLayer("route-casing", "route-source").apply {
        setProperties(
            lineColor("#0D47A1"),
            lineWidth(10f)
        )
    }

    val routeLayer = LineLayer("route-layer", "route-source").apply {
        setProperties(
            lineColor("#007AFF"),
            lineWidth(6f)
        )
    }

    style.addLayer(casing)
    style.addLayerAbove(routeLayer, "route-casing")
}

// =======================================================
// USER
// =======================================================

fun safeAddUser(style: Style, pos: LatLng) {
    if (style.getSource("user-source") != null) return
    addUserMarker(style, pos)
}

fun addUserMarker(style: Style, pos: LatLng) {
    style.addSource(
        GeoJsonSource("user-source", pointFeature(pos).toString())
    )

    style.addLayer(
        CircleLayer("user-layer", "user-source").apply {
            setProperties(
                circleRadius(8f),
                circleColor("#FF3B30")
            )
        }
    )
}

fun updateUser(style: Style, pos: LatLng) {
    val source = style.getSourceAs<GeoJsonSource>("user-source") ?: return
    source.setGeoJson(pointFeature(pos).toString())
}

fun startNavigation(
    scope: CoroutineScope,
    map: MapLibreMap,
    style: Style,
    route: List<LatLng>
) {
    scope.launch {
        var last = route.first()
        for (i in 1 until route.size) {
            val next = route[i]
            var t = 0.0
            while (t <= 1.0) {
                val pos = lerp(last, next, t)
                updateUser(style, pos)
                moveCamera(map, pos, last, next)
                t += 0.1
                delay(50)
            }
            last = next
        }
    }
}

// =======================================================
// UTILS
// =======================================================

fun pointFeature(pos: LatLng): JSONObject =
    JSONObject().apply {
        put("type", "FeatureCollection")
        put(
            "features", JSONArray().put(
                JSONObject().apply {
                    put("type", "Feature")
                    put(
                        "geometry", JSONObject().apply {
                            put("type", "Point")
                            put(
                                "coordinates",
                                JSONArray().put(pos.longitude).put(pos.latitude)
                            )
                        }
                    )
                }
            )
        )
    }

fun lerp(a: LatLng, b: LatLng, t: Double): LatLng =
    LatLng(
        a.latitude + (b.latitude - a.latitude) * t,
        a.longitude + (b.longitude - a.longitude) * t
    )

fun bearing(a: LatLng, b: LatLng): Double {
    val dLon = Math.toRadians(b.longitude - a.longitude)
    val y = Math.sin(dLon) * Math.cos(Math.toRadians(b.latitude))
    val x =
        Math.cos(Math.toRadians(a.latitude)) * Math.sin(Math.toRadians(b.latitude)) -
                Math.sin(Math.toRadians(a.latitude)) *
                Math.cos(Math.toRadians(b.latitude)) *
                Math.cos(dLon)
    return (Math.toDegrees(Math.atan2(y, x)) + 360) % 360
}

fun moveCamera(
    map: MapLibreMap,
    pos: LatLng,
    a: LatLng,
    b: LatLng
) {
    map.easeCamera(
        CameraUpdateFactory.newCameraPosition(
            CameraPosition.Builder()
                .target(pos)
                .zoom(16.0)
                .tilt(45.0)
                .bearing(bearing(a, b))
                .build()
        ),
        100
    )
}

// =======================================================
// SHOPS
// =======================================================

fun demoShopGeoJson(): JSONObject =
    JSONObject().apply {
        put("type", "FeatureCollection")
        put(
            "features", JSONArray().apply {
                put(shop("shop_1", "Quán Cà Phê A", 10.3621, 106.6812))
                put(shop("shop_2", "Tiệm Bún Bò B", 10.3585, 106.6840))
            }
        )
    }

fun shop(id: String, name: String, lat: Double, lng: Double) =
    JSONObject().apply {
        put("type", "Feature")
        put("properties", JSONObject().apply {
            put("id", id)
            put("name", name)
        })
        put("geometry", JSONObject().apply {
            put("type", "Point")
            put("coordinates", JSONArray().put(lng).put(lat))
        })
    }

fun addShopMarkers(style: Style, geoJson: JSONObject) {
    style.addSource(GeoJsonSource("shop-source", geoJson.toString()))

    style.addLayer(
        CircleLayer("shop-layer", "shop-source").apply {
            setProperties(
                circleRadius(6f),
                circleColor("#34C759"),
                circleStrokeColor("#FFFFFF"),
                circleStrokeWidth(1.5f)
            )
        }
    )
}

fun safeAddShops(style: Style) {
    if (style.getSource("shop-source") == null) {
        addShopMarkers(style, demoShopGeoJson())
    }
}

fun bindShopClick(
    map: MapLibreMap,
    onShopSelected: (ShopUi) -> Unit
) {
    map.addOnMapClickListener { latLng ->
        val screen = map.projection.toScreenLocation(latLng)
        val features = map.queryRenderedFeatures(screen, "shop-layer")
        if (features.isEmpty()) return@addOnMapClickListener false

        val f = features[0]
        val point = f.geometry() as? Point ?: return@addOnMapClickListener false

        onShopSelected(
            ShopUi(
                id = f.getStringProperty("id"),
                name = f.getStringProperty("name"),
                lat = point.latitude(),
                lng = point.longitude()
            )
        )
        true
    }
}

fun zoomToRoute(
    map: MapLibreMap,
    route: List<LatLng>
) {
    if (route.isEmpty()) return

    val boundsBuilder = LatLngBounds.Builder()
    route.forEach { boundsBuilder.include(it) }

    map.animateCamera(
        CameraUpdateFactory.newLatLngBounds(
            boundsBuilder.build(),
            200 // padding px
        )
    )
}


fun latLngToTile(lat: Double, lng: Double, zoom: Int): TileXY {
    val n = 1 shl zoom
    val x = ((lng + 180.0) / 360.0 * n).toInt()

    val latRad = Math.toRadians(lat)
    val y = (
            (1.0 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI)
                    / 2.0 * n
            ).toInt()

    return TileXY(x, y)
}

fun tileToLatLngBounds(
    x: Int,
    y: Int,
    zoom: Int
): TileLatLngBounds {

    val n = 1 shl zoom

    val west = x.toDouble() / n * 360.0 - 180.0
    val east = (x + 1).toDouble() / n * 360.0 - 180.0

    val northRad = Math.atan(Math.sinh(Math.PI * (1 - 2.0 * y / n)))
    val southRad = Math.atan(Math.sinh(Math.PI * (1 - 2.0 * (y + 1) / n)))

    return TileLatLngBounds(
        north = Math.toDegrees(northRad),
        south = Math.toDegrees(southRad),
        west = west,
        east = east
    )
}


fun tilesInViewport(
    corners: MapCorners,
    zoom: Int
): List<Pair<Int, Int>> {

    val tl = latLngToTile(corners.topLeft.latitude, corners.topLeft.longitude, zoom)
    val tr = latLngToTile(corners.topRight.latitude, corners.topRight.longitude, zoom)
    val bl = latLngToTile(corners.bottomLeft.latitude, corners.bottomLeft.longitude, zoom)
    val br = latLngToTile(corners.bottomRight.latitude, corners.bottomRight.longitude, zoom)

    val minX = minOf(tl.x, tr.x, bl.x, br.x)
    val maxX = maxOf(tl.x, tr.x, bl.x, br.x)
    val minY = minOf(tl.y, tr.y, bl.y, br.y)
    val maxY = maxOf(tl.y, tr.y, bl.y, br.y)

    val tiles = mutableListOf<Pair<Int, Int>>()
    for (x in minX..maxX) {
        for (y in minY..maxY) {
            tiles.add(x to y)
        }
    }
    return tiles
}


fun buildTileGridGeoJson(
    tiles: List<Pair<Int, Int>>,
    zoom: Int
): String {

    val features = tiles.map { (x, y) ->
        val b = tileToLatLngBounds(x, y, zoom)

        """
        {
          "type": "Feature",
          "properties": {
            "label": "z=$zoom\nx=$x\ny=$y"
          },
          "geometry": {
            "type": "Polygon",
            "coordinates": [[
              [${b.west}, ${b.north}],
              [${b.east}, ${b.north}],
              [${b.east}, ${b.south}],
              [${b.west}, ${b.south}],
              [${b.west}, ${b.north}]
            ]]
          }
        }
        """.trimIndent()
    }

    return """
    {
      "type": "FeatureCollection",
      "features": [${features.joinToString(",")}]
    }
    """.trimIndent()
}



fun showTileDebugGrid(
    style: Style,
    corners: MapCorners,
    zoom: Int
) {
    val tiles = tilesInViewport(corners, zoom)
    val geoJson = buildTileGridGeoJson(tiles, zoom)

    val sourceId = "tile-debug-source"
    val lineLayerId = "tile-debug-line"
    val textLayerId = "tile-debug-text"

    val source = style.getSourceAs<GeoJsonSource>(sourceId)
    if (source == null) {
        style.addSource(GeoJsonSource(sourceId, geoJson))

        style.addLayer(
            LineLayer(lineLayerId, sourceId).withProperties(
                lineColor("#FF0000"),
                lineWidth(1.2f)
            )
        )

        style.addLayer(
            SymbolLayer(textLayerId, sourceId).withProperties(
                textField(get("label")),
                textSize(12f),
                textColor("#000000"),
                textHaloColor("#FFFFFF"),
                textHaloWidth(1.5f),
                textAllowOverlap(true)
            )
        )
    } else {
        source.setGeoJson(geoJson)
    }
}

