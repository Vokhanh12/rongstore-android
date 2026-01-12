package com.aliasadi.clean.ui.map

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ShopBottomSheetContent(shop: ShopUi, onNavigate: (ShopUi) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = shop.name,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "ID: ${shop.id}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { /* mở chi tiết */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Xem chi tiết")
        }

        Button(
            onClick = { onNavigate(shop) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dẫn đường")
        }
    }
}
