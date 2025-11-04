package com.aliasadi.clean.navigation

import com.aliasadi.clean.ui.result.QRCodeRawData
import kotlinx.serialization.Serializable

sealed class Page {
    @Serializable
    data object NavigationBar : Page()
    @Serializable
    data object Feed : Page()
    @Serializable
    data object Favorites : Page()
    @Serializable
    data object Search : Page()
    @Serializable
    data class MovieDetails(val movieId: Int) : Page()
    @Serializable
    data object Map : Page()
    @Serializable
    data object ScanQr : Page()
    @Serializable
    data class QrCodeResult(val dbRowId: Int): Page()
}

sealed class Graph {
    @Serializable
    data object Main : Graph()
}

fun Page.route(): String? {
    return this.javaClass.canonicalName
}