package com.aliasadi.clean.ui.components

import android.util.Log
import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.rive.runtime.kotlin.RiveAnimationView
import app.rive.runtime.kotlin.controllers.RiveFileController
import app.rive.runtime.kotlin.core.Alignment
import app.rive.runtime.kotlin.core.Fit
import app.rive.runtime.kotlin.core.Loop
import app.rive.runtime.kotlin.core.PlayableInstance
import com.aliasadi.clean.R

class ClickableRiveView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : RiveAnimationView(context, attrs) {

    var onViewClick: (() -> Unit)? = null

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            Log.d("RiveAnimation", "ClickableRiveView received click")
            onViewClick?.invoke()
            return true
        }
        return super.onTouchEvent(event)
    }
}

@Composable
fun RiveAnimation(
    modifier: Modifier = Modifier,
    @RawRes resId: Int,
    autoplay: Boolean = true,
    artboardName: String? = null,
    animationName: String? = null,
    stateMachineName: String? = null,
    fit: Fit = Fit.CONTAIN,
    alignment: Alignment = Alignment.CENTER,
    loop: Loop = Loop.AUTO,
    contentDescription: String? = null,
    notifyLoop: ((PlayableInstance) -> Unit)? = null,
    notifyPause: ((PlayableInstance) -> Unit)? = null,
    notifyPlay: ((PlayableInstance) -> Unit)? = null,
    notifyStateChanged: ((String, String) -> Unit)? = null,
    notifyStop: ((PlayableInstance) -> Unit)? = null,
    update: (RiveAnimationView) -> Unit = {},
    onClick: (() -> Unit)? = null
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    if (LocalInspectionMode.current) {
        Image(
            modifier = modifier.size(120.dp),
            painter = painterResource(id = R.drawable.bg_image),
            contentDescription = contentDescription
        )
    } else {
        val semanticsModifier = if (contentDescription != null) {
            Modifier.semantics {
                this.contentDescription = contentDescription
                this.role = Role.Image
            }
        } else Modifier

        AndroidView(
            modifier = modifier
                .then(semanticsModifier)
                .clipToBounds(),
            factory = { context ->
                ClickableRiveView(context).apply {
                    setRiveResource(
                        resId = resId,
                        artboardName = artboardName,
                        animationName = animationName,
                        stateMachineName = stateMachineName,
                        autoplay = autoplay,
                        fit = fit,
                        alignment = alignment
                    )
                    listenerSetup(
                        riveView = this,
                        notifyLoop = notifyLoop,
                        notifyPause = notifyPause,
                        notifyPlay = notifyPlay,
                        notifyStateChanged = notifyStateChanged,
                        notifyStop = notifyStop
                    )

                    onViewClick = {
                        Log.d("RiveAnimation", "Clicked Custom Rive View (onViewClick)")
                        onClick?.invoke()
                    }
                }
            },
            update = { update(it) }
        )

        DisposableEffect(lifecycleOwner) {
            onDispose {
                Log.d("RiveAnimation", "Disposed RiveAnimationView")
            }
        }
    }
}

private fun listenerSetup(
    riveView: RiveAnimationView,
    notifyLoop: ((PlayableInstance) -> Unit)?,
    notifyPause: ((PlayableInstance) -> Unit)?,
    notifyPlay: ((PlayableInstance) -> Unit)?,
    notifyStateChanged: ((String, String) -> Unit)?,
    notifyStop: ((PlayableInstance) -> Unit)?
) {
    if (
        notifyLoop == null && notifyPause == null &&
        notifyPlay == null && notifyStateChanged == null &&
        notifyStop == null
    ) return

    val listener = object : RiveFileController.Listener {
        override fun notifyLoop(animation: PlayableInstance) = notifyLoop?.invoke(animation) ?: Unit
        override fun notifyPause(animation: PlayableInstance) = notifyPause?.invoke(animation) ?: Unit
        override fun notifyPlay(animation: PlayableInstance) = notifyPlay?.invoke(animation) ?: Unit
        override fun notifyStateChanged(stateMachineName: String, stateName: String) =
            notifyStateChanged?.invoke(stateMachineName, stateName) ?: Unit
        override fun notifyStop(animation: PlayableInstance) = notifyStop?.invoke(animation) ?: Unit
    }
    riveView.registerListener(listener)
}

@Preview(showSystemUi = true)
@Composable
fun RiveComposablePreview() {
    RiveAnimation(
        resId = R.raw.qr_code_scanner,
        autoplay = true,
        animationName = "main",
        contentDescription = "QR Scanner",
        onClick = { Log.d("Preview", "Clicked in Preview") }
    )
}
