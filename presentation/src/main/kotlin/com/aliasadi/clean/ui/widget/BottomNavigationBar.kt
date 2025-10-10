package com.aliasadi.clean.ui.widget

import ComposableRiveAnimationView
import android.content.res.Configuration
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.aliasadi.clean.navigation.route
import com.aliasadi.clean.ui.navigationbar.BottomNavigationBarItem
import com.aliasadi.clean.ui.navigationbar.BottomNavigationBarItem.*
import com.aliasadi.clean.util.preview.PreviewContainer

@Composable
fun BottomNavigationBar(
    items: List<BottomNavigationBarItem>,
    navController: NavController,
    onItemClick: (BottomNavigationBarItem) -> Unit
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    NavigationBar {
        items.forEach { item ->
            val selected = item.page.route() == backStackEntry.value?.destination?.route
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item) },
                icon = {
                    if (item.riveFile != null) {
                        ComposableRiveAnimationView(
                            animation = item.riveFile,
                            modifier = Modifier
                                .size(60.dp)
                        )
                    } else if(item.imageVector != null) {
                        Icon(
                            imageVector = item.imageVector,
                            contentDescription = null
                        )
                    }
                },
                label = {
                    if(item.tabName != null)
                    Text(text = item.tabName)
                }
            )
        }
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BottomNavigationBarViewPreview() {
    PreviewContainer {
        BottomNavigationBar(listOf(Feed, MyFavorites), rememberNavController()) {}
    }
}