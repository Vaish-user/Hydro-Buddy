package com.hydrobuddy.app.ui.mascot

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hydrobuddy.app.data.model.AnimationSpeed
import com.hydrobuddy.app.data.model.CharacterSize
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.InputStream

/**
 * 2D Sprite-sheet renderer for Pip (pip_001).
 * Renders high-resolution (256x256 per frame) animations from the master atlas.
 * Highly optimized, zero external C++ dependencies, battery-friendly, and transparent.
 */
@Composable
fun PipSpriteView(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    characterSize: CharacterSize = CharacterSize.MEDIUM,
    animationSpeed: AnimationSpeed = AnimationSpeed.NORMAL,
    animationState: PipAnimationState = PipAnimationState.A_IDLE,
    loopOverride: Boolean? = null,
    onAnimationFinished: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var masterBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentFrameIndex by remember { mutableIntStateOf(0) }

    // Load sprite sheet bitmap once with lifecycle safety
    DisposableEffect(Unit) {
        var stream: InputStream? = null
        try {
            stream = context.assets.open("sprites/pip_master_spritesheet.png")
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            masterBitmap = BitmapFactory.decodeStream(stream, null, options)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            stream?.close()
        }

        onDispose {
            masterBitmap = null
        }
    }

    // Animation frame cycling
    LaunchedEffect(animationState, animationSpeed, loopOverride) {
        currentFrameIndex = 0
        val frameDelay = when (animationSpeed) {
            AnimationSpeed.GENTLE -> 120L // ~8.3 FPS
            AnimationSpeed.NORMAL -> 83L  // 12 FPS
            AnimationSpeed.BOUNCY -> 55L  // ~18 FPS
        }

        val shouldLoop = loopOverride ?: animationState.isLooping
        if (shouldLoop) {
            while (isActive) {
                delay(frameDelay)
                currentFrameIndex = (currentFrameIndex + 1) % 8
            }
        } else {
            for (f in 0 until 8) {
                currentFrameIndex = f
                delay(frameDelay)
            }
            onAnimationFinished?.invoke()
        }
    }

    val actualSize = size * characterSize.scaleFactor
    val paint = remember {
        Paint().apply {
            isFilterBitmap = true
            isAntiAlias = true
        }
    }

    Box(
        modifier = modifier.size(actualSize),
        contentAlignment = Alignment.Center
    ) {
        val bitmap = masterBitmap
        if (bitmap != null) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(1f)
            ) {
                val frameW = 256
                val frameH = 256
                val srcX = currentFrameIndex * frameW
                val srcY = animationState.rowIndex * frameH

                val srcRect = Rect(srcX, srcY, srcX + frameW, srcY + frameH)
                val dstRect = Rect(0, 0, this.size.width.toInt(), this.size.height.toInt())

                drawIntoCanvas { canvas ->
                    canvas.nativeCanvas.drawBitmap(bitmap, srcRect, dstRect, paint)
                }
            }
        }
    }
}
