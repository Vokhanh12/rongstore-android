package com.aliasadi.clean.ui.result

import android.content.Intent
import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.aliasadi.clean.R
import com.aliasadi.clean.ui.navigationbar.TopNavigationBar
import com.aliasadi.clean.ui.shadow
import com.aliasadi.domain.entities.QRCodeEntity
import com.google.mlkit.vision.barcode.common.Barcode
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun QRCodeResultPage(
    mainNavController: NavHostController,
    viewModel: QRCodeResultViewModel,
    dbRowId: Int,
    dismiss: (() -> Unit) = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle(null)

    LaunchedEffect(Unit) {
        viewModel.getQRCodeByRowId(dbRowId)
    }

    BackHandler {
        dismiss.invoke()
        mainNavController.popBackStack()
    }

    QRCodeResultScreen(
        state = state,
        onBack = {
            dismiss.invoke()
            mainNavController.popBackStack()
        },
        onHandle = viewModel::handleQRCodeAction,
        onCopy = viewModel::copyRawValue,
        onShare = viewModel::shareRawValue
    )
}

@Composable
private fun QRCodeResultScreen(
    state: QRCodeResultUIState?,
    onBack: () -> Unit,
    onHandle: (QRCodeRawData?) -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit
) {
    val qrCodeRawData = (state as? QRCodeResultUIState.Success)
        ?.qrCodeResult
        ?.toQRCodeRawData()

    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Scaffold(
        topBar = {
            TopNavigationBar(titleResId = R.string.qr_code_result, backNavClick = onBack)
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp)),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                        .shadow(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            blurRadius = 8.dp, borderRadius = 8.dp,
                            spread = 0.dp, offsetY = 0.dp, offsetX = 0.dp
                        )
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onBack() }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.icon_qr_white),
                        contentDescription = stringResource(id = R.string.continue_to_scan),
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = stringResource(R.string.continue_to_scan),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge
                    )
                }
                Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(18.dp)
                .shadow(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    blurRadius = 8.dp, borderRadius = 8.dp,
                    spread = 0.dp, offsetY = 0.dp, offsetX = 0.dp
                )
                .background(Color.White, RoundedCornerShape(8.dp))
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(
                            MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                            blurRadius = 8.dp, borderRadius = 4.dp,
                            spread = 0.dp, offsetY = 0.dp, offsetX = 0.dp
                        )
                        .background(MaterialTheme.colorScheme.inverseOnSurface, RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    Image(
                        painter = painterResource(id = qrCodeRawData?.typeIcon ?: R.drawable.icon_qr),
                        contentDescription = stringResource(R.string.qr_code_type),
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = qrCodeRawData?.typeStringRes?.let { stringResource(it) }.orEmpty(),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = qrCodeRawData?.scanDate.orEmpty(),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
            }

            Text(
                text = qrCodeRawData?.rawData.orEmpty(),
                modifier = Modifier
                    .align(Alignment.Start)
                    .fillMaxWidth()
                    .heightIn(64.dp, Dp.Unspecified)
                    .shadow(
                        MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
                        blurRadius = 8.dp, borderRadius = 4.dp,
                        spread = 0.dp, offsetY = 0.dp, offsetX = 0.dp
                    )
                    .background(MaterialTheme.colorScheme.inverseOnSurface, RoundedCornerShape(4.dp))
                    .padding(8.dp)
            )

            // Nút xử lý QR (mở link, gọi điện, gửi SMS,...)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        blurRadius = 8.dp, borderRadius = 8.dp,
                        spread = 0.dp, offsetY = 0.dp, offsetX = 0.dp
                    )
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onHandle(qrCodeRawData) }
                    .padding(12.dp)
            ) {
                Text(
                    text = stringResource(id = qrCodeRawData?.ctaHandleStringRes ?: R.string.copy).uppercase(),
                    modifier = Modifier.align(Alignment.Center),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            // Copy / Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            blurRadius = 8.dp, borderRadius = 8.dp
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            val text = qrCodeRawData?.rawData.orEmpty()
                            clipboard.setText(AnnotatedString(text))
                            onCopy(text)
                        }
                        .padding(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.copy_text).uppercase(),
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            blurRadius = 8.dp, borderRadius = 8.dp
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary)
                        .clickable {
                            val text = qrCodeRawData?.rawData.orEmpty()
                            onShare(text)
                        }
                        .padding(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.share_text).uppercase(),
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}

data class QRCodeRawData(
    val type: Int = 0,
    @DrawableRes val typeIcon: Int = 0,
    val qrCodeBitmap: Bitmap,
    @StringRes val typeStringRes: Int = 0,
    val scanDate: String,
    val rawData: String,
    @StringRes val ctaHandleStringRes: Int,
    val jsonDetails: String? = null,
)

fun QRCodeEntity.toQRCodeRawData(): QRCodeRawData {
    return QRCodeRawData(
        type = qrType,
        typeIcon = when (qrType) {
            Barcode.TYPE_URL -> R.drawable.icon_link
            Barcode.TYPE_WIFI -> R.drawable.icon_wifi
            Barcode.TYPE_CONTACT_INFO -> R.drawable.icon_contact
            Barcode.TYPE_CALENDAR_EVENT -> R.drawable.icon_calendar_event
            Barcode.TYPE_EMAIL -> R.drawable.icon_email
            Barcode.TYPE_GEO -> R.drawable.icon_geo
            Barcode.TYPE_PHONE -> R.drawable.icon_phone
            Barcode.TYPE_SMS -> R.drawable.icon_sms
            Barcode.TYPE_TEXT -> R.drawable.icon_text
            Barcode.TYPE_DRIVER_LICENSE -> R.drawable.icon_license
            Barcode.TYPE_PRODUCT -> R.drawable.icon_product
            Barcode.TYPE_ISBN -> R.drawable.icon_isbn
            else -> R.drawable.icon_qr
        },
        qrCodeBitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888),
        typeStringRes = when (qrType) {
            Barcode.TYPE_URL -> R.string.url
            Barcode.TYPE_WIFI -> R.string.wifi
            Barcode.TYPE_CONTACT_INFO -> R.string.contact
            Barcode.TYPE_CALENDAR_EVENT -> R.string.calendar
            Barcode.TYPE_EMAIL -> R.string.email
            Barcode.TYPE_GEO -> R.string.geo
            Barcode.TYPE_PHONE -> R.string.phone
            Barcode.TYPE_SMS -> R.string.sms
            Barcode.TYPE_TEXT -> R.string.text
            Barcode.TYPE_DRIVER_LICENSE -> R.string.driver_license
            Barcode.TYPE_PRODUCT -> R.string.product
            Barcode.TYPE_ISBN -> R.string.isbn
            else -> R.string.text
        },
        scanDate = SimpleDateFormat("HH:mm, E dd MMM, yyyy", Locale.getDefault())
            .format(Date(scanDateTimeMillis)).toString(),
        rawData = rawData.orEmpty(),
        ctaHandleStringRes = when (qrType) {
            Barcode.TYPE_URL -> R.string.open_in_browser
            Barcode.TYPE_WIFI -> R.string.copy_password
            Barcode.TYPE_CONTACT_INFO -> R.string.add_contact
            Barcode.TYPE_CALENDAR_EVENT -> R.string.add_calendar
            Barcode.TYPE_EMAIL -> R.string.send_email
            Barcode.TYPE_GEO -> R.string.search
            Barcode.TYPE_PHONE -> R.string.call
            Barcode.TYPE_SMS -> R.string.send_sms
            else -> R.string.search
        },
        jsonDetails = qrDetails,
    )
}
