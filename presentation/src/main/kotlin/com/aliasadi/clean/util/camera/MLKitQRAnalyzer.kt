package com.aliasadi.clean.util.camera

import android.graphics.Bitmap
import android.graphics.Rect as AndroidRect
import android.os.SystemClock
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class MLKitQRAnalyzer(
    private val roiRatio: Float = 0.8f,
    private val holdMs: Long = 1500L,
    private val analyzeInterval: Long = 200L,
    private val authorizedUsers: List<String> = listOf("12345", "67890", "abcde"),
    private val onFrameAnalyzed: (Bitmap?) -> Unit,  // optional preview
    private val onQrResult: (decoded: String, authorized: Boolean) -> Unit,
    private val onQrBoundingBox: (AndroidRect?) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    private var lastDecoded: String? = null
    private var lastTime = 0L
    private var lastAnalyzeTime = 0L

    override fun analyze(imageProxy: ImageProxy) {
        val now = SystemClock.elapsedRealtime()

        // Giới hạn tốc độ phân tích
        if (now - lastAnalyzeTime < analyzeInterval) {
            imageProxy.close()
            return
        }
        lastAnalyzeTime = now

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        try {
            val rotation = imageProxy.imageInfo.rotationDegrees
            val image = InputImage.fromMediaImage(mediaImage, rotation)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    var found = false
                    for (barcode in barcodes) {
                        val rawValue = barcode.rawValue ?: continue
                        val box = barcode.boundingBox
                        val rect = box?.let {
                            AndroidRect(it.left, it.top, it.right, it.bottom)
                        }

                        // ROI filtering (nếu bạn vẫn muốn chỉ trong vùng quét)
                        if (rect != null && !isInRoi(rect, imageProxy.width, imageProxy.height)) {
                            continue
                        }

                        found = true
                        val authorized = authorizedUsers.contains(rawValue)

                        if (rawValue != lastDecoded || now - lastTime > holdMs) {
                            lastDecoded = rawValue
                            lastTime = now
                            onQrResult(rawValue, authorized)
                        }

                        onQrBoundingBox(rect)
                        break
                    }

                    if (!found && now - lastTime > holdMs) {
                        lastDecoded = null
                        onQrBoundingBox(null)
                    }

                    // Không cần convert bitmap ở đây, MLKit tự xử lý ảnh
                    onFrameAnalyzed(null)
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }

        } catch (e: Exception) {
            e.printStackTrace()
            imageProxy.close()
        }
    }

    private fun isInRoi(rect: AndroidRect, width: Int, height: Int): Boolean {
        val roiSize = (minOf(width, height) * roiRatio).toInt()
        val left = (width - roiSize) / 2
        val top = (height - roiSize) / 2
        val roi = AndroidRect(left, top, left + roiSize, top + roiSize)
        return roi.contains(rect.centerX(), rect.centerY())
    }
}
