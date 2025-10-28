package com.aliasadi.clean.ui.scanqr

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ScanQrScreen(
    viewModel: ScanQrViewModel = hiltViewModel()
) {
    val state by viewModel.barScanState.collectAsState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            is BarScanState.Ideal -> {
                Text("Hãy đưa mã QR vào khung quét")
            }

            is BarScanState.Loading -> {
                CircularProgressIndicator()
            }

            is BarScanState.ScanSuccess -> {
                val data = (state as BarScanState.ScanSuccess).barStateModel
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("✅ Quét thành công!")
                    Text("Client: ${data.client}")
                    Text("Purchase: ${data.purchase}")
                    Text("TotalAmount: ${data.totalAmount}")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.resetState() }) {
                        Text("Quét lại")
                    }
                }
            }

            is BarScanState.Error -> {
                val msg = (state as BarScanState.Error).error
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("❌ Lỗi: $msg")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { viewModel.resetState() }) {
                        Text("Thử lại")
                    }
                }
            }
        }
    }
}
