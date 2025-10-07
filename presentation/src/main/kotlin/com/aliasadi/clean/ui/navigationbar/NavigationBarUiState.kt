package com.aliasadi.clean.ui.navigationbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.ui.graphics.vector.ImageVector
import com.aliasadi.clean.navigation.Page

data class NavigationBarUiState(
    val bottomItems: List<BottomNavigationBarItem> = listOf(
        BottomNavigationBarItem.Feed,
        BottomNavigationBarItem.MyFavorites,
        BottomNavigationBarItem.Map
    )
)

sealed class BottomNavigationBarItem(
    val tabName: String,
    val imageVector: ImageVector,
    val page: Page,
) {
    data object Feed : BottomNavigationBarItem("Feed", imageVector = Icons.Default.DynamicFeed, Page.Feed)
    data object MyFavorites : BottomNavigationBarItem("My Favorites", imageVector = Icons.Default.FavoriteBorder, Page.Favorites)
    data object Map : BottomNavigationBarItem("Map", imageVector = Icons.Default.FavoriteBorder, Page.Map)
}
