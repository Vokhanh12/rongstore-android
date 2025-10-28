package com.aliasadi.clean.ui.scanqr

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.aliasadi.clean.util.camera.QRAnalyzer
import kotlinx.coroutines.delay

@Composable
fun ScanQrScreen() {
    val context = LocalContext.current
    var frameBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var resultText by remember { mutableStateOf<String?>(null) }
    var qrBox by remember { mutableStateOf<Rect?>(null) }
    var lineOffset by remember { mutableStateOf(0f) }

    // --- Animation đường quét ---
    LaunchedEffect(Unit) {
        while (true) {
            lineOffset += 6f
            if (lineOffset > 600f) lineOffset = 0f
            delay(16L)
        }
    }

    // --- Xử lý animation của khung QR ---
    val targetLeft = qrBox?.left?.toFloat() ?: 0f
    val targetTop = qrBox?.top?.toFloat() ?: 0f
    val targetRight = qrBox?.right?.toFloat() ?: 0f
    val targetBottom = qrBox?.bottom?.toFloat() ?: 0f

    // ✅ animateFloatAsState gọi ở ngoài Canvas
    val left by animateFloatAsState(targetLeft, label = "leftAnim")
    val top by animateFloatAsState(targetTop, label = "topAnim")
    val right by animateFloatAsState(targetRight, label = "rightAnim")
    val bottom by animateFloatAsState(targetBottom, label = "bottomAnim")

    Box(modifier = Modifier.fillMaxSize()) {
        // --- Camera Preview ---
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    // ép preview full không viền
                    scaleType = PreviewView.ScaleType.FILL_START
                    implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                }
                val displayMetrics = ctx.resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val screenHeight = displayMetrics.heightPixels
                val screenRatio = aspectRatio(screenWidth, screenHeight)

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()

                    val preview = Preview.Builder()
                        .setTargetAspectRatio(screenRatio)
                        .setTargetRotation(previewView.display.rotation)
                        .build()
                        .also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                    val analyzer = ImageAnalysis.Builder()
                        .setTargetAspectRatio(screenRatio)
                        .setTargetRotation(previewView.display.rotation)
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build()
                        .also {
                            it.setAnalyzer(
                                ContextCompat.getMainExecutor(ctx),
                                QRAnalyzer(
                                    onFrameAnalyzed = { bmp -> frameBitmap = bmp },
                                    onQrResult = { decoded, authorized ->
                                        resultText = if (authorized) "✅ $decoded" else "❌ $decoded"
                                        Log.d("QR_RESULT", "decoded=$decoded authorized=$authorized")
                                    },
                                    onQrBoundingBox = { rect -> qrBox = rect }
                                )
                            )
                        }

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        ctx as LifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        analyzer
                    )
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )


        // --- Hiển thị frame (tùy chọn) ---
        frameBitmap?.let {
            Image(bitmap = it.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
        }

        // --- Canvas khung quét ---
        Canvas(modifier = Modifier.fillMaxSize()) {
            val defaultSize = size.minDimension * 0.65f
            val centerX = size.width / 2
            val centerY = size.height / 2

            // Nếu chưa có bounding box, dùng khung giữa màn hình
            val drawLeft = if (qrBox == null) centerX - defaultSize / 2 else left
            val drawTop = if (qrBox == null) centerY - defaultSize / 2 else top
            val drawRight = if (qrBox == null) centerX + defaultSize / 2 else right
            val drawBottom = if (qrBox == null) centerY + defaultSize / 2 else bottom

            val width = drawRight - drawLeft
            val height = drawBottom - drawTop

            // --- Vẽ khung bo góc ---
            drawRoundRect(
                color = Color.White.copy(alpha = 0.4f),
                topLeft = Offset(drawLeft, drawTop),
                size = Size(width, height),
                cornerRadius = CornerRadius(16f, 16f),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 5f)
            )

            // --- Đường quét chuyển động ---
            drawLine(
                color = Color(0xFF00FF00),
                start = Offset(drawLeft, drawTop + (lineOffset % height)),
                end = Offset(drawRight, drawTop + (lineOffset % height)),
                strokeWidth = 4f
            )
        }

        resultText?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }
    }

}

private fun aspectRatio(width: Int, height: Int): Int {
    val previewRatio = maxOf(width, height).toDouble() / minOf(width, height)
    return if (kotlin.math.abs(previewRatio - 4.0 / 3.0) <= kotlin.math.abs(previewRatio - 16.0 / 9.0)) {
        AspectRatio.RATIO_4_3
    } else {
        AspectRatio.RATIO_16_9
    }
}
