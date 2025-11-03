package com.aliasadi.clean.ui.scanqr

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliasadi.clean.ui.result.QRCodeRawData
import com.aliasadi.data.repository.user.UserDataSource
import com.aliasadi.domain.entities.QRCodeContact
import com.aliasadi.domain.entities.QRCodePhone
import com.aliasadi.domain.entities.QRCodeSMS
import com.aliasadi.domain.entities.QRCodeURL
import com.aliasadi.domain.entities.QRCodeWifi
import com.google.mlkit.vision.barcode.common.Barcode
import com.squareup.moshi.Moshi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Stable
data class MainUIState(
    val isFrontCamera: Boolean = false,
    val isTorchOn: Boolean = false,
    val isLoading: Boolean = false,
    val isQRCodeFound: Boolean = false,
)

sealed class QRCodeAction {
    data object None : QRCodeAction()
    data class ToastAction(val message: String) : QRCodeAction()
    data class OpenUrl(val url: String) : QRCodeAction()
    data class CopyText(val text: String) : QRCodeAction()
    data class ContactInfo(val contact: QRCodeContact) : QRCodeAction()
    data class TextSearchGoogle(val text: String) : QRCodeAction()
    data class TextShareAction(val text: String) : QRCodeAction()
    data class SendSMSAction(val sms: QRCodeSMS) : QRCodeAction()
    data class CallPhoneAction(val phone: String) : QRCodeAction()
    data class OpenQRCodeResult(val id: Int) : QRCodeAction()
    data class PickGalleryImage(val uri: Uri) : QRCodeAction()
}

interface ICameraController {
    fun toggleTorch()
    fun toggleCamera()
    fun toggleKeepScanning()
}

@HiltViewModel
class ScanQrViewModel @Inject constructor(
    private val moshi: Moshi,
    userDataSource: UserDataSource,
    @ApplicationContext private val context: Context
) : ViewModel(), ICameraController {

    companion object {
        const val INVALID_DB_ROW_ID = 0
    }

    private val setQRResults: MutableSet<String> = mutableSetOf()

    val appSettingState = userDataSource.userSettingData.stateIn(
        viewModelScope,
        started = SharingStarted.Lazily,
        initialValue = null
    )

    private val _mainUiState = MutableStateFlow(MainUIState())
    val mainUiState: StateFlow<MainUIState> = _mainUiState.asStateFlow()

    private val _qrCodeActionState = MutableSharedFlow<QRCodeAction>(extraBufferCapacity = 1)
    val qrCodeActionState = _qrCodeActionState.asSharedFlow()

    // ----------- CAMERA CONTROLS -----------

    override fun toggleTorch() {
        _mainUiState.value = _mainUiState.value.copy(isTorchOn = !_mainUiState.value.isTorchOn)
    }

    override fun toggleCamera() {
        _mainUiState.value = _mainUiState.value.copy(isFrontCamera = !_mainUiState.value.isFrontCamera)
    }

    override fun toggleKeepScanning() {
        // Cần implement nếu muốn lưu state vào DataStore
    }

    // ----------- QR SCAN LOGIC -----------


    fun scanQRSuccess(result: Barcode) {
        // Nếu đã có hoặc đang giữ QR hiện tại => bỏ qua

        Log.d("debug", "test")

        if (mainUiState.value.isQRCodeFound ||
            (setQRResults.contains(result.rawValue) && appSettingState.value?.isKeepScanning == true)) {
            return
        }

        Log.d("debug", "test1")

        // Đánh dấu đã quét QR
        setQRResults.add(result.rawValue ?: return)
        _mainUiState.value = _mainUiState.value.copy(isQRCodeFound = true)

        viewModelScope.launch {
            if (appSettingState.value?.isKeepScanning != true) {
                _qrCodeActionState.tryEmit(
                    QRCodeAction.OpenQRCodeResult(result.rawValue.hashCode())
                )
            } else {
                // Hiển thị thông báo và tiếp tục quét
                _qrCodeActionState.tryEmit(
                    QRCodeAction.ToastAction("QR Code Scanned ${result.rawValue}")
                )
                resetScanQR()
            }
        }
    }



    fun resetScanQR() {
        _mainUiState.value = _mainUiState.value.copy(isQRCodeFound = false)
        _qrCodeActionState.tryEmit(QRCodeAction.None)
    }

    fun handleBarcodeResult(barcode: QRCodeRawData) {
        when (barcode.type) {
            Barcode.TYPE_URL -> handleJson<QRCodeURL>(
                barcode,
                onSuccess = { _qrCodeActionState.tryEmit(QRCodeAction.OpenUrl(it.url.orEmpty())) }
            )

            Barcode.TYPE_WIFI -> handleJson<QRCodeWifi>(
                barcode,
                onSuccess = { _qrCodeActionState.tryEmit(QRCodeAction.CopyText(it.pass.orEmpty())) }
            )

            Barcode.TYPE_CONTACT_INFO -> handleJson<QRCodeContact>(
                barcode,
                onSuccess = { _qrCodeActionState.tryEmit(QRCodeAction.ContactInfo(it)) }
            )

            Barcode.TYPE_PHONE -> handleJson<QRCodePhone>(
                barcode,
                onSuccess = { _qrCodeActionState.tryEmit(QRCodeAction.CallPhoneAction(it.number.orEmpty())) }
            )

            Barcode.TYPE_SMS -> handleJson<QRCodeSMS>(
                barcode,
                onSuccess = { _qrCodeActionState.tryEmit(QRCodeAction.SendSMSAction(it)) }
            )

            else -> {
                _qrCodeActionState.tryEmit(QRCodeAction.TextSearchGoogle(barcode.rawData))
            }
        }
    }

    private inline fun <reified T> handleJson(barcode: QRCodeRawData, onSuccess: (T) -> Unit) {
        barcode.jsonDetails?.let { json ->
            runCatching {
                moshi.adapter(T::class.java).fromJson(json)?.let(onSuccess)
                    ?: throw Exception("Failed to parse ${T::class.java.simpleName}")
            }.onFailure {
                _qrCodeActionState.tryEmit(QRCodeAction.CopyText(barcode.rawData))
            }
        } ?: _qrCodeActionState.tryEmit(QRCodeAction.CopyText(barcode.rawData))
    }

    // ----------- ACTION HANDLERS -----------

    fun handleCopyText(text: String) {
        _qrCodeActionState.tryEmit(QRCodeAction.CopyText(text))
    }

    fun handleShareText(text: String) {
        _qrCodeActionState.tryEmit(QRCodeAction.TextShareAction(text))
    }

    fun handleGalleryUri(uri: Uri?) {
        uri?.let { _qrCodeActionState.tryEmit(QRCodeAction.PickGalleryImage(it)) }
    }

    fun showLoading() {
        _mainUiState.value = _mainUiState.value.copy(isLoading = true)
    }

    fun hideLoading() {
        _mainUiState.value = _mainUiState.value.copy(isLoading = false)
    }
}
