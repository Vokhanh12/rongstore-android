package com.aliasadi.clean.ui.map

import android.content.res.Configuration
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aliasadi.clean.ui.main.MainRouter
import com.aliasadi.clean.util.preview.PreviewContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.geometry.LatLngBounds
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.PropertyFactory.*
import org.maplibre.android.style.sources.GeoJsonSource
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPage(
    mainRouter: MainRouter,
    viewModel: MapViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    MapScreen(state = uiState)
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    state: MapUIState
) {
    var animationVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        delay(150)
        animationVisible = true
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(state.title) }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                factory = { context ->
                    val mapView = MapView(context)
                    mapView.onCreate(null)

                    mapView.getMapAsync { mapLibreMap ->
                        Log.d("MAPLIBRE", "Loading style: ${state.mapStyleUrl}")

                        mapLibreMap.setStyle(
                            Style.Builder().fromUri(state.mapStyleUrl)
                        ) { style ->
                            Log.d("MAPLIBRE", "Style loaded successfully")

                            // 🔹 Tọa độ xuất phát và kết thúc
                            val start = Pair(106.679593, 10.359721)
                            val end = Pair(106.729660, 10.737567)

                            val osrmUrl =
                                "http://100.114.31.30:5000/route/v1/driving/${start.first},${start.second};${end.first},${end.second}?geometries=geojson&overview=full"

                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val response = URL(osrmUrl).openStream().bufferedReader().use { it.readText() }
                                    val json = JSONObject(response)
                                    val geometry =
                                        json.getJSONArray("routes").getJSONObject(0).getJSONObject("geometry")

                                    // 🔹 Feature Collection chuẩn
                                    val featureJson = JSONObject().apply {
                                        put("type", "Feature")
                                        put("geometry", geometry)
                                    }
                                    val featureCollection = JSONObject().apply {
                                        put("type", "FeatureCollection")
                                        put("features", JSONArray().put(featureJson))
                                    }

                                    withContext(Dispatchers.Main) {
                                        val sourceId = "route-source"
                                        val layerId = "route-layer"

                                        val routeSource = GeoJsonSource(sourceId, featureCollection.toString())
                                        if (style.getSource(sourceId) == null) style.addSource(routeSource)

                                        // 🔹 Line Layer hiển thị tuyến
                                        val lineLayer = LineLayer(layerId, sourceId).apply {
                                            setProperties(
                                                lineColor("#007AFF"),
                                                lineWidth(5f),
                                                lineCap("round"),
                                                lineJoin("round")
                                            )
                                        }

                                        // Thêm layer trên cùng đường
                                        val lastLayerId = style.layers.lastOrNull()?.id
                                        if (lastLayerId != null) {
                                            style.addLayerAbove(lineLayer, lastLayerId)
                                        } else {
                                            style.addLayer(lineLayer)
                                        }

                                        // 🔹 Zoom vào tuyến đường
                                        val coordinates = geometry.getJSONArray("coordinates")
                                        val boundsBuilder = LatLngBounds.Builder()
                                        for (i in 0 until coordinates.length()) {
                                            val coord = coordinates.getJSONArray(i)
                                            boundsBuilder.include(LatLng(coord.getDouble(1), coord.getDouble(0)))
                                        }
                                        val bounds = boundsBuilder.build()
                                        mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 200))
                                    }
                                } catch (e: Exception) {
                                    Log.e("OSRM_ERROR", "Failed to get route: ${e.message}", e)
                                }
                            }
                        }
                    }

                    mapView
                }
            )

            AnimatedVisibility(visible = animationVisible, enter = fadeIn()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = state.title, style = MaterialTheme.typography.titleLarge)
                    Text(
                        text = state.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun MapScreenPreview() {
    PreviewContainer {
        MapScreen(
            state = MapUIState(
                mapStyleUrl = "http://100.114.31.30:8082/styles/maptiler-basic/style.json",
                title = "OSRM Route Ho Chi Minh City",
                description = "Hiển thị đường đi từ 10.409431,106.653762 đến 10.737567,106.729660"
            )
        )
    }
}
