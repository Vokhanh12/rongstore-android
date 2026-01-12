package com.aliasadi.clean.ui.main

import androidx.camera.view.LifecycleCameraController
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.aliasadi.clean.navigation.Graph
import com.aliasadi.clean.navigation.Page
import com.aliasadi.clean.ui.moviedetails.MovieDetailsPage
import com.aliasadi.clean.ui.moviedetails.MovieDetailsViewModel
import com.aliasadi.clean.ui.navigationbar.NavigationBarNestedGraph
import com.aliasadi.clean.ui.navigationbar.NavigationBarScreen
import com.aliasadi.clean.ui.result.QRCodeResultPage
import com.aliasadi.clean.ui.result.QRCodeResultViewModel
import com.aliasadi.clean.ui.scanqr.QrCameraScreenLayout
import com.aliasadi.clean.ui.scanqr.ScanQrViewModel
import com.aliasadi.clean.ui.search.SearchPage
import com.aliasadi.clean.ui.search.SearchViewModel
import com.aliasadi.clean.util.composableHorizontalSlide
import com.aliasadi.clean.util.sharedViewModel

@Composable
fun MainGraph(
    mainNavController: NavHostController,
    darkMode: Boolean,
    onThemeUpdated: () -> Unit
) {
    val context = LocalContext.current
    remember { LifecycleCameraController(context) }.apply {
        bindToLifecycle(LocalLifecycleOwner.current)
    }

    NavHost(
        navController = mainNavController,
        startDestination = Page.NavigationBar,
        route = Graph.Main::class
    ) {
        composableHorizontalSlide<Page.NavigationBar> { backStack ->
            val nestedNavController = rememberNavController()
            NavigationBarScreen(
                sharedViewModel = backStack.sharedViewModel(navController = mainNavController),
                mainRouter = MainRouter(mainNavController),
                darkMode = darkMode,
                onThemeUpdated = onThemeUpdated,
                nestedNavController = nestedNavController
            ) {
                NavigationBarNestedGraph(
                    navController = nestedNavController,
                    mainNavController = mainNavController,
                    parentRoute = Graph.Main::class
                )
            }
        }

        composableHorizontalSlide<Page.Search> {
            val viewModel = hiltViewModel<SearchViewModel>()
            SearchPage(
                mainNavController = mainNavController,
                viewModel = viewModel,
            )
        }

        composableHorizontalSlide<Page.MovieDetails> {
            val viewModel = hiltViewModel<MovieDetailsViewModel>()
            MovieDetailsPage(
                mainNavController = mainNavController,
                viewModel = viewModel,
            )
        }

        composableHorizontalSlide<Page.QrCodeResult> { params ->
            val viewModel = hiltViewModel<QRCodeResultViewModel>()
            QRCodeResultPage(
                mainNavController = mainNavController,
                viewModel = viewModel,
                dbRowId = 0,
                dismiss = {
                    mainNavController.popBackStack()
                }
            )
        }

        composableHorizontalSlide<Page.ScanQr> {
            val viewModel = hiltViewModel<ScanQrViewModel>()
            QrCameraScreenLayout(mainNavController, viewModel)
        }
    }
}