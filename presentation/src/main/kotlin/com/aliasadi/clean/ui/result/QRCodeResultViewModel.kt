package com.aliasadi.clean.ui.result

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliasadi.domain.model.entities.QRCodeEntity
import com.aliasadi.domain.usecase.GetQRCodeByRowIdFlowUseCase
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QRCodeResultViewModel @Inject constructor(
    private val getQRCodeByRowIdUseCase: GetQRCodeByRowIdFlowUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<QRCodeResultUIState>(QRCodeResultUIState.Loading)
    val uiState: StateFlow<QRCodeResultUIState> = _uiState

    fun getQRCodeByRowId(rowId: Int) {
        viewModelScope.launch {
            _uiState.value = QRCodeResultUIState.Loading
            getQRCodeByRowIdUseCase.invoke(rowId)
                .catch { throwable ->
                    _uiState.value = QRCodeResultUIState.Error(throwable)
                }
                .collectLatest { entity ->
                    _uiState.value = if (entity != null) {
                        QRCodeResultUIState.Success(entity)
                    } else {
                        QRCodeResultUIState.Error(NullPointerException("QR not found"))
                    }
                }
        }
    }

    // ----------- Hành động từ UI ------------

    fun handleQRCodeAction(qrCodeRawData: QRCodeRawData?) {
        qrCodeRawData ?: return
        // Tùy loại QR sẽ thực hiện hành động khác nhau
        when (qrCodeRawData.type) {
            Barcode.TYPE_URL -> {
                openUrl(qrCodeRawData.rawData)
            }

            Barcode.TYPE_PHONE -> {
                openDialer(qrCodeRawData.rawData)
            }

            Barcode.TYPE_EMAIL -> {
                openEmail(qrCodeRawData.rawData)
            }

            Barcode.TYPE_SMS -> {
                openSMS(qrCodeRawData.rawData)
            }

            else -> {
                // Các loại khác không có hành động đặc biệt
            }
        }
    }

    fun copyRawValue(rawValue: String) {
        // Gửi event nếu muốn hiển thị toast ở UI (nếu bạn dùng snackbar / toast collector)
        println("📋 Copied value: $rawValue")
    }

    fun shareRawValue(rawValue: String) {
        println("📤 Share value: $rawValue")
        // Logic share nên được xử lý tại UI layer bằng Intent
    }

    // ----------- Các hàm hỗ trợ ------------
    private fun openUrl(url: String) {
        try {
            val context = appContext ?: return
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openDialer(phone: String) {
        try {
            val context = appContext ?: return
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openEmail(email: String) {
        try {
            val context = appContext ?: return
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openSMS(number: String) {
        try {
            val context = appContext ?: return
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("sms:$number"))
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        // ⚠️ Bổ sung context toàn cục (bạn có thể inject bằng Hilt nếu có App context)
        var appContext: android.content.Context? = null
    }
}

// ----------------- UI State -----------------

sealed class QRCodeResultUIState {
    data object Loading : QRCodeResultUIState()
    data class Success(val qrCodeResult: QRCodeEntity) : QRCodeResultUIState()
    data class Error(val throwable: Throwable) : QRCodeResultUIState()
}
