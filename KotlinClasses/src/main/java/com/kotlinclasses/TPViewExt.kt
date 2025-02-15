package com.kotlinclasses

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun Modifier.tpViewExt(): TPViewExt {
    val scope = rememberCoroutineScope()
    return remember { TPViewExt(scope) }
}

class TPViewExt(private val scope: CoroutineScope) {
    private var isVisible by mutableStateOf(true)
    private var alpha by mutableStateOf(1f)
    private var scale by mutableStateOf(1f)
    private var rotation by mutableStateOf(0f)
    private var translateY by mutableStateOf(0f)
    private var translateX by mutableStateOf(0f)

    fun applyEffects(modifier: Modifier = Modifier): Modifier {
        return modifier
            .alpha(if (isVisible) alpha else 0f)
            .scale(scale)
            .rotate(rotation)
            .offset(x = translateX.dp, y = translateY.dp)
    }

    fun makeCircle(border: Float? = null, borderColor: Color? = null): Modifier {
        return Modifier
            .clip(CircleShape)
            .then(
                if (border != null && borderColor != null) {
                    Modifier.border(border.dp, borderColor, CircleShape)
                } else {
                    Modifier
                }
            )
    }

    fun makeSlideCorners(radius: Double = 30.0): Modifier {
        return Modifier.clip(RoundedCornerShape(radius.dp))
    }

    fun addDiamondMask(cornerRadius: Double = 0.0): Modifier {
        return Modifier.clip(DiamondShape(cornerRadius))
    }

    fun addDiamondMaskWithCornersShaped(cornerRadius: Double = 0.0): Modifier {
        return Modifier.clip(DiamondShape(0.0, cornerRadius, cornerRadius, cornerRadius, cornerRadius))
    }

    fun addDiamondMaskWithCornersShaped(cornerRadius: Double = 0.0, cornerRadiusTop: Double = 0.0, cornerRadiusRight: Double = 0.0, cornerRadiusBottom: Double = 0.0, cornerRadiusLeft: Double = 0.0): Modifier {
        return Modifier.clip(DiamondShape(cornerRadius, cornerRadiusTop, cornerRadiusRight, cornerRadiusBottom, cornerRadiusLeft))
    }

    fun unHide() {
        isVisible = true
        alpha = 1f
    }

    fun hide() {
        isVisible = false
        alpha = 0f
    }

    fun dim() {
        alpha = 0.5f
    }

    fun unDim() {
        alpha = 1f
    }

    @Composable
    fun keepConstraintsAlignedWithLanguageDirection(
        appLanguage: TPAppLanguage,
        content: @Composable () -> Unit
    ) {
        val isRTL = appLanguage.getCurrentLanguageCode() == "ar"
        val layoutDirection = if (isRTL) {
            LayoutDirection.Rtl
        } else {
            LayoutDirection.Ltr
        }
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            content()
        }
    }

    fun shakeIt() {
        scope.launch {
            repeat(3) {
                translateX = 10f
                delay(50)
                translateX = -10f
                delay(50)
            }
            translateX = 0f
        }
    }

    fun rotateClockWise360Degrees(duration: Double = 1.0, completionDelegate: (() -> Unit)? = null) {
        scope.launch {
            val rotationAnim = Animatable(rotation)
            rotationAnim.animateTo(
                targetValue = rotation + 360f,
                animationSpec = tween(durationMillis = (duration * 1000).roundToInt())
            ) {
                rotation = value
            }
            completionDelegate?.invoke()
        }
    }

    fun rotateAntiClockWise360Degrees(duration: Double = 1.0, completionDelegate: (() -> Unit)? = null) {
        scope.launch {
            val rotationAnim = Animatable(rotation)
            rotationAnim.animateTo(
                targetValue = rotation - 360f,
                animationSpec = tween(durationMillis = (duration * 1000).roundToInt())
            ) {
                rotation = value
            }
            completionDelegate?.invoke()
        }
    }

    fun fadeInQuickly(duration: Double = 0.15) {
        scope.launch {
            val fadeAnim = Animatable(alpha)
            fadeAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = (duration * 1000).roundToInt())
            ) {
                alpha = value
            }
        }
    }

    fun fadeOutQuickly(duration: Double = 0.15) {
        scope.launch {
            val fadeAnim = Animatable(alpha)
            fadeAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = (duration * 1000).roundToInt())
            ) {
                alpha = value
            }
        }
    }

    fun scaleInOutQuickly(duration: Double = 0.1) {
        scope.launch {
            val scaleAnim = Animatable(1f)
            scaleAnim.animateTo(
                targetValue = 1.2f,
                animationSpec = tween(durationMillis = (duration * 500).roundToInt())
            ) {
                scale = value
            }
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = (duration * 500).roundToInt())
            ) {
                scale = value
            }
        }
    }

    fun stopAllAnimations() {
        scope.launch {
            alpha = 1f
            scale = 1f
            rotation = 0f
            translateX = 0f
            translateY = 0f
        }
    }

    fun flash(numberOfFlashes: Double = 1.0) {
        scope.launch {
            repeat(numberOfFlashes.roundToInt() * 2) {
                val fadeAnim = Animatable(alpha)
                fadeAnim.animateTo(
                    targetValue = if (it % 2 == 0) 0f else 1f,
                    animationSpec = tween(durationMillis = 100)
                ) {
                    alpha = value
                }
            }
        }
    }

    fun scaleOutInOneTime(duration: Double = 0.5) {
        scope.launch {
            val scaleAnim = Animatable(1f)
            scaleAnim.animateTo(
                targetValue = 1.5f,
                animationSpec = tween(durationMillis = (duration * 500).roundToInt())
            ) {
                scale = value
            }
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = (duration * 500).roundToInt())
            ) {
                scale = value
            }
        }
    }

    fun showFromBottomToTop(duration: Double = 0.35) {
        scope.launch {
            isVisible = true
            val translateAnim = Animatable(100f)
            translateAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = (duration * 1000).roundToInt())
            ) {
                translateY = value
            }
        }
    }

    fun showFromTopToBottom(duration: Double = 0.35) {
        scope.launch {
            isVisible = true
            val translateAnim = Animatable(-100f)
            translateAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = (duration * 1000).roundToInt())
            ) {
                translateY = value
            }
        }
    }

    fun hideSlowlyFromTopToBottom(duration: Double = 0.35) {
        scope.launch {
            val translateAnim = Animatable(0f)
            translateAnim.animateTo(
                targetValue = 100f,
                animationSpec = tween(durationMillis = (duration * 1000).roundToInt())
            ) {
                translateY = value
            }
            isVisible = false
        }
    }

    fun hideSlowlyFromBottomToTop(duration: Double = 0.35) {
        scope.launch {
            val translateAnim = Animatable(0f)
            translateAnim.animateTo(
                targetValue = -100f,
                animationSpec = tween(durationMillis = (duration * 1000).roundToInt())
            ) {
                translateY = value
            }
            isVisible = false
        }
    }

    fun moveToSpecificPosition(x: Float, y: Float) {
        translateX = x
        translateY = y
    }
}

private class DiamondShape(
    private val cornerRadius: Double = 0.0,
    private val cornerRadiusTop: Double = 0.0,
    private val cornerRadiusRight: Double = 0.0,
    private val cornerRadiusBottom: Double = 0.0,
    private val cornerRadiusLeft: Double = 0.0
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline = Outline.Generic(
        Path().apply {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radiusTop = (cornerRadiusTop * density.density).toFloat()
            val radiusRight = (cornerRadiusRight * density.density).toFloat()
            val radiusBottom = (cornerRadiusBottom * density.density).toFloat()
            val radiusLeft = (cornerRadiusLeft * density.density).toFloat()
            
            // Top point with rounded corner
            moveTo(centerX - radiusTop, radiusTop)
            quadraticBezierTo(centerX, 0f, centerX + radiusTop, radiusTop)
            
            // Right point with rounded corner
            lineTo(size.width - radiusRight, centerY - radiusRight)
            quadraticBezierTo(size.width, centerY, size.width - radiusRight, centerY + radiusRight)
            
            // Bottom point with rounded corner
            lineTo(centerX + radiusBottom, size.height - radiusBottom)
            quadraticBezierTo(centerX, size.height, centerX - radiusBottom, size.height - radiusBottom)
            
            // Left point with rounded corner
            lineTo(radiusLeft, centerY + radiusLeft)
            quadraticBezierTo(0f, centerY, radiusLeft, centerY - radiusLeft)
            
            close()
        }
    )
}