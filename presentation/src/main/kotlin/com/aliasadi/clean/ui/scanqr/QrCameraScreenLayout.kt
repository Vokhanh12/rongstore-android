package com.aliasadi.clean.ui.scanqr

import com.aliasadi.clean.R
import android.Manifest
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.ContactsContract
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.withResumed
import androidx.navigation.NavHostController
import com.aliasadi.clean.navigation.Page
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import java.util.Calendar
import kotlin.random.Random
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState


@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrCameraScreenLayout(mainNavController: NavHostController, vm: ScanQrViewModel) {

    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val cameraController: LifecycleCameraController = remember { LifecycleCameraController(context) }.apply {
        bindToLifecycle(LocalLifecycleOwner.current)
    }

    val galleryPicker = rememberLauncherForActivityResult(contract = ActivityResultContracts.PickVisualMedia(), onResult = {
        vm.handleGalleryUri(it)
    })

    var appSettingState by remember {
        mutableStateOf(vm.appSettingState.value)
    }

    var mainUIState by remember {
        mutableStateOf(vm.mainUiState.value)
    }

    val isLoadingState by vm.mainUiState.map { it.isLoading }.collectAsStateWithLifecycle(initialValue = false)

    LaunchedEffect(key1 = Unit) {
        vm.appSettingState.collectLatest {
            appSettingState = it
        }
    }

    LaunchedEffect(key1 = Unit) {
        vm.mainUiState.collectLatest @androidx.annotation.RequiresPermission(android.Manifest.permission.VIBRATE) {
            mainUIState = it

            cameraController.cameraSelector = if (it.isFrontCamera && cameraController.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)) {
                CameraSelector.DEFAULT_FRONT_CAMERA
            } else {
                CameraSelector.DEFAULT_BACK_CAMERA
            }

            cameraController.enableTorch(it.isTorchOn)
            if (it.isQRCodeFound) {
                if (appSettingState?.isEnableVibrate == true) {
                    context.vibrate(200L)
                }
                if (appSettingState?.isEnableSound == true) {
                    context.playPiplingSound()
                }
                cameraController.clearImageAnalysisAnalyzer()
            } else {
                delay(Random(Calendar.getInstance().timeInMillis).nextLong(1000))
                cameraController.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context)) { imageProxy ->
                    imageProxy.image?.let { img ->
                        InputImage.fromMediaImage(img, imageProxy.imageInfo.rotationDegrees)
                            .let { image ->
                                val scanner = BarcodeScanning.getClient()
                                scanner.process(image)
                                    .addOnSuccessListener { barcodes ->
                                        if (barcodes.isEmpty()) {
                                            return@addOnSuccessListener
                                        }
                                        barcodes.forEach { barcode ->
                                            if (barcode.rawValue != null) {
                                                vm.scanQRSuccess(barcode)
                                            }
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        exception.printStackTrace()
                                    }
                                    .addOnCompleteListener {
                                        imageProxy.close()
                                    }
                            }
                    }
                }
            }
        }
    }

    LaunchedEffect(key1 = Unit) {
        vm.qrCodeActionState.collect {
            when (it) {
                is QRCodeAction.ToastAction -> {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
                is QRCodeAction.OpenUrl -> {
                    val url = it.url
                    lifecycle.withResumed {
                        if (url.isNotEmpty()) {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(intent)
                        }
                    }
                }

                is QRCodeAction.OpenQRCodeResult -> {
                    if (it.id != ScanQrViewModel.INVALID_DB_ROW_ID) {
                        mainNavController.navigate(
                            Page.QrCodeResult(0)
                        )
                    }
                }

                is QRCodeAction.CopyText -> {
                    val copyText = it.text
                    if (copyText.isNotEmpty()) {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Copied Text", copyText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, context.resources.getString(R.string.copied_to_clipboard, copyText), Toast.LENGTH_SHORT).show()
                    }
                }

                is QRCodeAction.ContactInfo -> {
                    val intent = Intent(Intent.ACTION_INSERT).apply {
                        type = ContactsContract.Contacts.CONTENT_TYPE
                        putExtra(ContactsContract.Intents.Insert.NAME, it.contact.name)
                        putExtra(ContactsContract.Intents.Insert.EMAIL, it.contact.email?.firstOrNull())
                        putExtra(ContactsContract.Intents.Insert.PHONE, it.contact.phone?.firstOrNull())
                        putExtra(ContactsContract.Intents.Insert.COMPANY, it.contact.organization)
                        putExtra(ContactsContract.Intents.Insert.JOB_TITLE, it.contact.title)
                        putExtra(ContactsContract.Intents.Insert.NOTES, it.contact.urls?.firstOrNull())
                    }
                    context.startActivity(intent)
                }

                is QRCodeAction.TextSearchGoogle -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=${it.text}"))
                    context.startActivity(intent)
                }

                is QRCodeAction.TextShareAction -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, it.text)
                    }
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
                }

                is QRCodeAction.CallPhoneAction -> {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${it.phone}"))
                    context.startActivity(intent)
                }

                is QRCodeAction.SendSMSAction -> {
                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${it.sms.number}"))
                    intent.putExtra("sms_body", it.sms.message)
                    context.startActivity(intent)
                }

                is QRCodeAction.PickGalleryImage -> {
                    val uri = it.uri
                    vm.showLoading()
                    BarcodeScanning.getClient().process(InputImage.fromFilePath(context, uri))
                        .addOnSuccessListener { barcodes ->
                            vm.hideLoading()
                            if (barcodes.isEmpty()) {
                                Toast.makeText(context, context.getString(R.string.no_qr_code_detected), Toast.LENGTH_SHORT).show()
                                return@addOnSuccessListener
                            }
                            barcodes.forEach { barcode ->
                                if (barcode.rawValue != null) {
                                    vm.scanQRSuccess(barcode)
                                }
                            }
                        }
                        .addOnFailureListener {
                            vm.hideLoading()
                            Toast.makeText(context, context.getString(R.string.failed_to_scan_qr_code), Toast.LENGTH_SHORT).show()
                        }
                }

                else -> {}
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            if (cameraPermissionState.status.isGranted) {
                QRCameraScreen(cameraController,
                    appSettingState,
                    handleSwitchKeepScanning = {
                        vm.toggleKeepScanning()
                    })
            } else {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(text = stringResource(R.string.camera_permission_is_not_granted), color = MaterialTheme.colorScheme.onPrimary)
                    Button(onClick = {
                        cameraPermissionState.launchPermissionRequest()
                    }) {
                        Text(text = stringResource(R.string.click_to_grant_camera_permission), color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }

        Box(
            Modifier
                .safeDrawingPadding()
                .fillMaxSize()
                .background(Color.Transparent)
        ) {
            TopTools(
                mainUIState,
                appSettingState,
                toggleTorch = remember {
                    vm::toggleTorch
                },
                toggleCamera = remember {
                    vm::toggleCamera
                },
                navSetting = remember {
                    {
                        // appNavHost.navigate(AppScreen.SETTING.value)
                    }
                },
                navPremium = remember {
                    {
                        // appNavHost.navigate(AppScreen.PREMIUM.value)
                    }
                }
            )

            FooterTools(
                navToHistory = remember {
                    {
                        // appNavHost.navigate(AppScreen.HISTORY.value)
                    }
                },
                pickGallery = remember {
                    {
                        galleryPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                })
        }

        AnimatedVisibility(visible = isLoadingState, enter = fadeIn(), exit = fadeOut()) {
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color(0x901c1c1c))
                .clickable { }) {
                Text(
                    text = stringResource(R.string.loading_qr_code_scanner_engine),
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
        }
    }
}

@RequiresPermission(Manifest.permission.VIBRATE)
fun Context.vibrate(milliseconds: Long) {
    val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        v?.vibrate(VibrationEffect.createOneShot(milliseconds, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        v?.vibrate(milliseconds)
    }
}

fun Context.playPiplingSound() {
    val mediaPlayer = MediaPlayer.create(this, R.raw.ping).apply {
        setOnCompletionListener {
            it.release()
        }
        setVolume(0.5f, 0.5f)
    }
    mediaPlayer.start()
}