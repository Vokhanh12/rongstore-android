package com.aliasadi.clean.ui.widget

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.clickable
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
import com.aliasadi.clean.R
import com.aliasadi.clean.navigation.route
import com.aliasadi.clean.ui.components.RiveAnimation
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
                onClick = {onItemClick(item)},
                icon = {
                  if(item.imageVector != null) {
                        Icon(
                            imageVector = item.imageVector,
                            contentDescription = null
                        )
                  } else if(item.riveFile != null && item.riveAnimationName != null) {
                      RiveAnimation(
                          resId = item.riveFile,
                          modifier = Modifier
                              .size(60.dp),
                          animationName = item.riveAnimationName,
                          onClick = {
                              onItemClick(item)
                          }
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