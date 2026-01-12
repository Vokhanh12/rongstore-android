package com.aliasadi.clean.ui.navigationbar

import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.aliasadi.clean.navigation.Page
import com.aliasadi.clean.ui.favorites.FavoritesPage
import com.aliasadi.clean.ui.favorites.FavoritesViewModel
import com.aliasadi.clean.ui.feed.FeedPage
import com.aliasadi.clean.ui.feed.FeedViewModel
import com.aliasadi.clean.ui.main.MainRouter
import com.aliasadi.clean.ui.map.MapPage
import com.aliasadi.clean.ui.map.MapScreen
import com.aliasadi.clean.ui.map.MapViewModel
import com.aliasadi.clean.ui.scanqr.QrCameraScreenLayout
import com.aliasadi.clean.ui.scanqr.ScanQrViewModel
import com.aliasadi.clean.util.composableHorizontalSlide
import com.aliasadi.clean.util.sharedViewModel
import kotlin.reflect.KClass

@Composable
fun NavigationBarNestedGraph(
    navController: NavHostController,
    mainNavController: NavHostController,
    parentRoute: KClass<*>?
) {
    LocalContext.current

    NavHost(
        navController = navController,
        startDestination = Page.Feed,
        route = parentRoute
    ) {
        composableHorizontalSlide<Page.Feed> { backStack ->
            val viewModel = hiltViewModel<FeedViewModel>()
            FeedPage(
                mainRouter = MainRouter(mainNavController),
                viewModel = viewModel,
                sharedViewModel = backStack.sharedViewModel(navController = mainNavController)
            )
        }
        composableHorizontalSlide<Page.Favorites> {
            val viewModel = hiltViewModel<FavoritesViewModel>()
            FavoritesPage(
                mainRouter = MainRouter(mainNavController),
                viewModel = viewModel,
            )
        }
        composableHorizontalSlide<Page.Map> {
            val viewModel = hiltViewModel<MapViewModel>()
            MapPage(
                mainRouter = MainRouter(mainNavController),
                viewModel = viewModel,
            )
        }
        composableHorizontalSlide<Page.ScanQr> {
            val viewModel = hiltViewModel<ScanQrViewModel>()
            QrCameraScreenLayout(mainNavController = mainNavController, vm = viewModel)
        }
    }
}