package com.aliasadi.clean.util.camera

import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class QRAnalyzerMLKit(
    private val onQrResult: (String, Boolean) -> Unit,
    private val onQrBoundingBox: (Rect?) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()
    private var lastValue: String? = null
    private var lastTime = 0L

    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: return imageProxy.close()
        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val barcode = barcodes.firstOrNull { it.format == Barcode.FORMAT_QR_CODE }
                    barcode?.let {
                        val value = it.rawValue ?: ""
                        onQrBoundingBox(it.boundingBox) // <-- Gửi khung thật từ MLKit
                        val authorized = listOf("12345", "67890", "abcde").contains(value)
                        if (value != lastValue) {
                            lastValue = value
                            onQrResult(value, authorized)
                        }
                    }
                } else {
                    onQrBoundingBox(null)
                }
            }
            .addOnFailureListener {
                onQrBoundingBox(null)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
