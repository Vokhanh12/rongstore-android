package com.aliasadi.clean.ui.scanqr

/**
 * Trạng thái UI cho màn hình Map
 */
data class MapUIState(
    val mapStyleUrl: String = "http://100.114.31.30:8082/styles/maptiler-basic/style.json",
    val title: String = "Map Overview",
    val description: String = "This is a demo MapLibre map with markers and styles.",
    val showLoading: Boolean = false,
    val errorMessage: String? = null
)