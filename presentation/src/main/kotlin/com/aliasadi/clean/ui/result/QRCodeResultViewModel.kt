package com.aliasadi.clean.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aliasadi.domain.entities.QRCodeEntity
import com.aliasadi.domain.usecase.GetQRCodeByRowIdFlowUseCase
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

    private val _qrCodeResultUIState: MutableStateFlow<QRCodeResultUIState> =
        MutableStateFlow(QRCodeResultUIState.Loading)
    val qrCodeResultUIState: StateFlow<QRCodeResultUIState> = _qrCodeResultUIState

    fun getQRCodeByRowId(rowId: Int) {
        viewModelScope.launch {
            _qrCodeResultUIState.value = QRCodeResultUIState.Loading
            getQRCodeByRowIdUseCase.invoke(rowId)
                .catch { throwable ->
                    _qrCodeResultUIState.value = QRCodeResultUIState.Error(throwable)
                }
                .collectLatest {
                    _qrCodeResultUIState.value = if (it != null) QRCodeResultUIState.Success(it) else QRCodeResultUIState.Loading
                }
        }
    }
}

sealed class QRCodeResultUIState {
    data object Loading : QRCodeResultUIState()
    data class Success(val qrCodeResult: QRCodeEntity) : QRCodeResultUIState()
    data class Error(val throwable: Throwable) : QRCodeResultUIState()
}