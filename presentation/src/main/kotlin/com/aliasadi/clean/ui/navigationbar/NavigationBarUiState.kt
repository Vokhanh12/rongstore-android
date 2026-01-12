package com.aliasadi.clean.ui.navigationbar
import com.aliasadi.clean.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Map
import androidx.compose.ui.graphics.vector.ImageVector
import com.aliasadi.clean.navigation.Page

enum class DirectionType {
    Tab,
    Navigate,
}

data class NavigationBarUiState(
    val bottomItems: List<BottomNavigationBarItem> = listOf(
        BottomNavigationBarItem.Feed,
        BottomNavigationBarItem.ScanQr,
        BottomNavigationBarItem.Map,
    )
)

sealed class BottomNavigationBarItem(
    val tabName: String? = null,
    val page: Page,
    val imageVector: ImageVector? = null,
    val riveFile: Int? = null,
    val riveAnimationName: String? = null,
    val directionType: DirectionType
) {
    data object Feed : BottomNavigationBarItem(
        tabName = "Feed",
        imageVector = Icons.Default.DynamicFeed,
        page = Page.Feed,
        directionType = DirectionType.Tab
    )

    data object Map : BottomNavigationBarItem(
        tabName = "Map",
        imageVector = Icons.Default.Map,
        page = Page.Map,
        directionType = DirectionType.Tab
    )

    data object MyFavorites : BottomNavigationBarItem(
        tabName = "Favorites",
        imageVector = Icons.Default.FavoriteBorder,
        page = Page.Favorites,
        directionType = DirectionType.Tab
    )

    data object ScanQr : BottomNavigationBarItem(
        riveFile = R.raw.qr_code_scanner,
        riveAnimationName = "main",
        page = Page.ScanQr,
        directionType = DirectionType.Navigate
    )
}