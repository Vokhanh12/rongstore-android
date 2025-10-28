package com.aliasadi.clean.util.camera

import android.graphics.Bitmap
import android.graphics.Rect as AndroidRect // 👈 alias cho Rect của Android
import android.media.Image
import android.os.SystemClock
import androidx.camera.core.AspectRatio
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.imgproc.Imgproc
import org.opencv.objdetect.QRCodeDetector

class QRAnalyzer(
    private val roiRatio: Float = 0.65f,  // vùng quét mặc định 65%
    private val holdMs: Long = 1500L,     // giữ kết quả 1.5s
    private val authorizedUsers: List<String> = listOf("12345", "67890", "abcde"),
    private val onFrameAnalyzed: (Bitmap) -> Unit,
    private val onQrResult: (decoded: String, authorized: Boolean) -> Unit,
    private val onQrBoundingBox: (AndroidRect?) -> Unit   // 👈 callback gửi vị trí QR code
) : ImageAnalysis.Analyzer {

    private val detector = QRCodeDetector()
    private var lastDecoded: String? = null
    private var lastTime = 0L

    override fun analyze(imageProxy: ImageProxy) {
        val image = imageProxy.image ?: run {
            imageProxy.close(); return
        }

        try {
            // --- Chuyển ảnh sang Mat ---
            val nv21 = yuvToNv21(image)
            val yuvMat = Mat(imageProxy.height + imageProxy.height / 2, imageProxy.width, CvType.CV_8UC1)
            yuvMat.put(0, 0, nv21)

            val rgb = Mat()
            Imgproc.cvtColor(yuvMat, rgb, Imgproc.COLOR_YUV2RGB_NV21)
            yuvMat.release()

            // --- Xoay đúng hướng ---
            val rotation = imageProxy.imageInfo.rotationDegrees
            val oriented = rotateMatIfNeeded(rgb, rotation)
            if (oriented !== rgb) rgb.release()

            // --- Tạo ROI trung tâm ---
            val w = oriented.cols()
            val h = oriented.rows()
            val roiSize = (minOf(w, h) * roiRatio).toInt()
            val left = (w - roiSize) / 2
            val top = (h - roiSize) / 2
            val roiRect = org.opencv.core.Rect(left, top, roiSize, roiSize) // ✅ đúng loại Rect
            val roiMat = Mat(oriented, roiRect)

            val points = Mat()
            val decoded = detector.detectAndDecode(roiMat, points)
            val now = SystemClock.elapsedRealtime()

            // --- Tạo ảnh hiển thị ---
            val display = oriented.clone()
            Imgproc.rectangle(
                display,
                Point(left.toDouble(), top.toDouble()),
                Point((left + roiSize).toDouble(), (top + roiSize).toDouble()),
                Scalar(255.0, 255.0, 255.0),
                3
            )

            var qrRectToSend: AndroidRect? = null

            if (!points.empty() && decoded.isNotEmpty()) {
                val corners = mutableListOf<Point>()
                for (i in 0 until points.cols()) {
                    val p = points.get(0, i)
                    corners.add(Point(p[0] + left, p[1] + top))
                }

                // --- Tính bounding box ---
                val xs = corners.map { it.x }
                val ys = corners.map { it.y }
                val rectLeft = xs.minOrNull() ?: 0.0
                val rectTop = ys.minOrNull() ?: 0.0
                val rectRight = xs.maxOrNull() ?: 0.0
                val rectBottom = ys.maxOrNull() ?: 0.0
                qrRectToSend = AndroidRect(
                    rectLeft.toInt(),
                    rectTop.toInt(),
                    rectRight.toInt(),
                    rectBottom.toInt()
                )

                // --- Vẽ đường viền ---
                val authorized = authorizedUsers.contains(decoded)
                val color = if (authorized) Scalar(0.0, 255.0, 0.0) else Scalar(0.0, 0.0, 255.0)
                for (i in corners.indices) {
                    Imgproc.line(display, corners[i], corners[(i + 1) % corners.size], color, 6)
                }

                Imgproc.putText(
                    display,
                    if (authorized) "Access Granted" else "Unauthorized",
                    Point(left.toDouble(), (top - 10).toDouble()),
                    Imgproc.FONT_HERSHEY_SIMPLEX,
                    1.0,
                    color,
                    3
                )

                // --- Callback kết quả ---
                if (decoded != lastDecoded || now - lastTime > holdMs) {
                    lastDecoded = decoded
                    lastTime = now
                    onQrResult(decoded, authorized)
                }
            } else if (now - lastTime > holdMs) {
                lastDecoded = null
            }

            // --- Callback vị trí QR ---
            onQrBoundingBox(qrRectToSend)

            val bmp = Bitmap.createBitmap(display.cols(), display.rows(), Bitmap.Config.ARGB_8888)
            Utils.matToBitmap(display, bmp)
            onFrameAnalyzed(bmp)

            display.release()
            roiMat.release()
            points.release()
            oriented.release()

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            imageProxy.close()
        }
    }

    private fun rotateMatIfNeeded(src: Mat, rotation: Int): Mat {
        if (rotation == 0) return src
        val dst = Mat()
        when (rotation) {
            90 -> Core.rotate(src, dst, Core.ROTATE_90_CLOCKWISE)
            180 -> Core.rotate(src, dst, Core.ROTATE_180)
            270 -> Core.rotate(src, dst, Core.ROTATE_90_COUNTERCLOCKWISE)
        }
        return dst
    }

    private fun yuvToNv21(image: Image): ByteArray {
        val y = image.planes[0].buffer
        val u = image.planes[1].buffer
        val v = image.planes[2].buffer
        val ySize = y.remaining()
        val uSize = u.remaining()
        val vSize = v.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        y.get(nv21, 0, ySize)
        v.get(nv21, ySize, vSize)
        u.get(nv21, ySize + vSize, uSize)
        return nv21
    }


}
