package com.aliasadi.clean.ui.main

import android.Manifest
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.navigation.compose.rememberNavController
import com.aliasadi.clean.di.AppSettingsSharedPreference
import com.aliasadi.clean.ui.theme.AppTheme
import com.aliasadi.clean.ui.widget.NoInternetConnectionBanner
import com.aliasadi.domain.util.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    companion object {
        const val DARK_MODE = "dark_mode"
    }

    @Inject
    @AppSettingsSharedPreference
    lateinit var appSettings: SharedPreferences

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    private fun isDarkModeEnabled() = appSettings.getBoolean(DARK_MODE, false)
    private fun enableDarkMode(enable: Boolean) = appSettings.edit().putBoolean(DARK_MODE, enable).commit()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val navController = rememberNavController()
            var darkMode by remember { mutableStateOf(isDarkModeEnabled()) }

            // Biến trạng thái kiểm tra quyền camera
            var hasCameraPermission by remember { mutableStateOf(false) }

            // Launcher xin quyền runtime
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { granted ->
                    hasCameraPermission = granted
                }
            )

            // Kiểm tra và xin quyền khi vào app
            LaunchedEffect(Unit) {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }

            AppTheme(darkMode) {
                Column {
                    val networkStatus by networkMonitor.networkState.collectAsState(null)

                    networkStatus?.let {
                        if (!it.isOnline) {
                            NoInternetConnectionBanner()
                        }
                    }

                    if (hasCameraPermission) {
                        // ✅ Đã có quyền — hiển thị giao diện chính
                        MainGraph(
                            mainNavController = navController,
                            darkMode = darkMode,
                            onThemeUpdated = {
                                val updated = !darkMode
                                enableDarkMode(updated)
                                darkMode = updated
                            }
                        )
                    } else {
                        // ❌ Chưa có quyền — hiển thị thông báo
                        Text("Ứng dụng cần quyền camera để hoạt động. Vui lòng cấp quyền trong Cài đặt.")
                    }
                }
            }
        }
    }
}
