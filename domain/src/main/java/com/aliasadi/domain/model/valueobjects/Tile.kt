package com.aliasadi.domain.model.valueobjects

import kotlin.math.*

data class Tile(
    val x: Int,
    val y: Int
) {
    companion object {

        fun fromLatLng(
            lat: Double,
            lng: Double,
            zoom: Int = DEFAULT_ZOOM
        ): Tile {
            val n = 1 shl zoom

            val x = ((lng + 180.0) / 360.0 * n).toInt()

            val latRad = Math.toRadians(lat)
            val y = (
                    (1 - ln(tan(latRad) + 1 / cos(latRad)) / Math.PI) / 2 * n
                    ).toInt()

            return Tile(x, y)
        }

        private const val DEFAULT_ZOOM = 16
    }
}
