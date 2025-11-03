package com.aliasadi.clean.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import androidx.compose.material3.Typography

private val darkColors = darkColorScheme(
    primary = Color.Black, // Main Primary Color
    onPrimary = Color.White, // Used for text and icons that appear on top of primary elements.

    primaryContainer = AppColor.RedC1, // Used for elements that contain primary elements, such as floating action buttons.

    background = Color.Black, // Background color used for the app's screens

    surface = Color.Black, // Background color used in components like the NavigationBar and TopAppBar
    onSurface = Color.White, // For example, selected text in NavigationBar

    surfaceVariant = Color.Black, // Used in TextField, SearchView
    onSurfaceVariant = Color.White, // Used for text and icons in NavigationBar and Application Icons

    secondaryContainer = AppColor.RedC1, // Hover color on (NavigationBar)
    onSecondaryContainer = Color.White // Selected icon color on (NavigationBar)
)

private val lightColors = lightColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    primaryContainer = AppColor.DarkGrayD3,
    secondaryContainer = AppColor.LightGrayD3,
    background = Color.White,
    surfaceVariant = Color.White,
    surface = Color.White
)

lateinit var colors: ColorScheme

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val systemUiController = rememberSystemUiController()

    colors = if (darkTheme) {
        darkColors
    } else {
        lightColors
    }

    systemUiController.setStatusBarColor(color = colors.primary)

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

@Composable
fun QRCodeScannerTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> darkColors
        else -> lightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
