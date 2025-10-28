package com.aliasadi.clean.ui.scanqr

data class BarModel(
    val invoiceNumber: String,
    val client: Client,
    val purchase: List<PurchaseItem>,
    val totalAmount: Double
)

data class Client(
    val name: String,
    val email: String,
    val address: String
)

data class PurchaseItem(
    val item: String,
    val quantity: Int,
    val price: Double
)

sealed interface BarScanState {
    data object Ideal : BarScanState
    data class ScanSuccess(val barStateModel: BarModel) : BarScanState
    data class Error(val error: String) : BarScanState
    data object Loading : BarScanState
}