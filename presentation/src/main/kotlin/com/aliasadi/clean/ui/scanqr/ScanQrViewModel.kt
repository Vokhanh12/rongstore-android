package com.aliasadi.clean.ui.scanqr

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.common.Barcode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

@HiltViewModel
class ScanQrViewModel @Inject constructor(
    private val jsonParser: Json // hoặc bạn inject sẵn Json { ignoreUnknownKeys = true }
) : ViewModel() {

    private val _barScanState = MutableStateFlow<BarScanState>(BarScanState.Ideal)
    val barScanState = _barScanState.asStateFlow()

    fun onBarCodeDetected(barcodes: List<Barcode>) {
        viewModelScope.launch {
            if (barcodes.isEmpty()) {
                _barScanState.value = BarScanState.Error("No barcode detected")
                return@launch
            }

            _barScanState.value = BarScanState.Loading

            barcodes.forEach { barcode ->
                barcode.rawValue?.let { barcodeValue ->
                    try {
                        val barModel: BarModel = jsonParser.decodeFromString(barcodeValue)
                        _barScanState.value = BarScanState.ScanSuccess(barModel)
                    } catch (e: Exception) {
                        Log.i("ScanQrViewModel", "onBarCodeDetected: $e")
                        _barScanState.value = BarScanState.Error("Invalid JSON format in barcode")
                    }
                    return@launch
                }
            }

            _barScanState.value = BarScanState.Error("No valid barcode value")
        }
    }

    fun resetState() {
        _barScanState.value = BarScanState.Ideal
    }
}
