package com.aliasadi.clean.ui.navigationbar

import com.aliasadi.clean.R
import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.aliasadi.clean.navigation.route
import com.aliasadi.clean.ui.components.RiveAnimation
import com.aliasadi.clean.ui.main.MainRouter
import com.aliasadi.clean.ui.theme.AppColor
import com.aliasadi.clean.ui.widget.BottomNavigationBar
import com.aliasadi.clean.ui.widget.TopBar
import com.aliasadi.clean.util.preview.PreviewContainer
import kotlinx.coroutines.delay

@Composable
fun NavigationBarScreen(
    sharedViewModel: NavigationBarSharedViewModel,
    mainRouter: MainRouter,
    darkMode: Boolean,
    onThemeUpdated: () -> Unit,
    nestedNavController: NavHostController,
    content: @Composable () -> Unit
) {
    val uiState = NavigationBarUiState()
    var showAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500) // đợi 0.5s cho UI ổn định
        showAnimation = true
    }


    Scaffold(
        topBar = {
            TopBar(
                "MovieClean",
                darkMode,
                onThemeUpdated = onThemeUpdated,
                onSearchClick = {
                    mainRouter.navigateToSearch()
                }
            )

        },
        bottomBar = {
            Box {
                BottomNavigationBar(
                    items = uiState.bottomItems,
                    navController = nestedNavController,
                    onItemClick = { bottomItem ->

                        Log.d("debug", "OKE")

                        when (bottomItem.directionType) {

                            DirectionType.Tab -> {
                                val currentPageRoute = nestedNavController.currentDestination?.route
                                val clickedPageRoute = bottomItem.page
                                Log.d("debug", "msg: $clickedPageRoute")

                                if (currentPageRoute != clickedPageRoute.route()) {
                                    nestedNavController.navigate(clickedPageRoute) {
                                        launchSingleTop = true
                                        popUpTo(nestedNavController.graph.findStartDestination().id)
                                    }
                                }
                            }

                            DirectionType.Navigate -> {
                                val clickedPageRoute = bottomItem.page
                                Log.d("debug", "msg: $clickedPageRoute")
                                sharedViewModel.onBottomItemClicked(bottomItem)
                            }

                        }
                    }
                )

                if (showAnimation) {
                    RiveAnimation(
                        resId = R.raw.qr_code_scanner,
                        animationName = "main",
                        onClick = {
                            Log.d("Preview", "Clicked in Preview")
                        }
                    )

                    Button(
                        onClick = {
                            Log.d("Preview", "Clicked in Preview")
                        }
                    ) {
                        Text("Click me")
                    }

                }

            }
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .shadow(8.dp, RoundedCornerShape(24.dp))
                    .background(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                    )
            ) {


            }
        }
    ) { paddingValues ->
        Box(
            Modifier
                .fillMaxSize(1f)
                .padding(paddingValues)
        ) {
            content()
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NavigationBarScreenPreview() = PreviewContainer {
    val navController = rememberNavController()
    val mainRouter = MainRouter(navController)
    val darkTheme = isSystemInDarkTheme()

    NavigationBarScreen(
        sharedViewModel = NavigationBarSharedViewModel(),
        mainRouter = mainRouter,
        darkMode = darkTheme,
        onThemeUpdated = { },
        nestedNavController = navController,
        content = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(AppColor.GrayB3)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    fontSize = 20.sp,
                    text = "Page Content"
                )
            }
        }
    )
}