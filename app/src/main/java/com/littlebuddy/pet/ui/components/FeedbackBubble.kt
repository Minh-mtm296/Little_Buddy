package com.littlebuddy.pet.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littlebuddy.pet.ui.theme.MidnightLagoon
import com.littlebuddy.pet.ui.theme.Rosewood
import com.littlebuddy.pet.ui.theme.SoftWhite


@Composable
fun FeedbackBubble(
    message: String,
    modifier: Modifier = Modifier,
    isSleeping: Boolean = false,
    tailPositionFraction: Float = 0.5f,
    onBubbleClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bubbleFloat")
    val hoverY by infiniteTransition.animateFloat(
        initialValue = -2.2f,
        targetValue = 2.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bubbleHoverY"
    )

    var lastNonBlankMessage by remember { mutableStateOf(message) }
    if (message.isNotBlank()) {
        lastNonBlankMessage = message
    }
    val displayMessage = if (message.isNotBlank()) message else lastNonBlankMessage

    AnimatedVisibility(
        visible = message.isNotBlank(),
        enter = fadeIn(tween(200)) + scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialScale = 0.65f
        ),
        exit = fadeOut(tween(180)) + scaleOut(
            animationSpec = tween(180, easing = FastOutSlowInEasing),
            targetScale = 0.75f
        ),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .graphicsLayer { translationY = hoverY.dp.toPx() }
                .drawBehind {
                    if (isSleeping) {
                        drawDreamCloudBubble(
                            w = size.width,
                            h = size.height,
                            tailFraction = tailPositionFraction
                        )
                    } else {
                        drawSpeechBubble(
                            w = size.width,
                            h = size.height,
                            tailFraction = tailPositionFraction
                        )
                    }
                }
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onBubbleClick() }
                .padding(
                    start = 14.dp,
                    top = 8.dp,
                    end = 14.dp,
                    bottom = if (isSleeping) 20.dp else 16.dp
                )
                .widthIn(min = 60.dp, max = 260.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayMessage,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 17.sp
                ),
                color = MidnightLagoon,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Vẽ bong bóng thoại
 */
private fun DrawScope.drawSpeechBubble(
    w: Float,
    h: Float,
    tailFraction: Float
) {
    val tailH = 8.5.dp.toPx()
    val tailW = 12.dp.toPx()
    val bodyH = h - tailH
    val r = 15.dp.toPx()
    val tailX = (w * tailFraction).coerceIn(r + tailW * 0.6f, w - r - tailW * 0.6f)

    val path = Path().apply {
        moveTo(r, 0f)
        lineTo(w - r, 0f)
        quadraticTo(w, 0f, w, r)
        lineTo(w, bodyH - r)
        quadraticTo(w, bodyH, w - r, bodyH)
        lineTo(tailX + tailW * 0.5f, bodyH)
        lineTo(tailX, h)
        lineTo(tailX - tailW * 0.5f, bodyH)
        lineTo(r, bodyH)
        quadraticTo(0f, bodyH, 0f, bodyH - r)
        lineTo(0f, r)
        quadraticTo(0f, 0f, r, 0f)
        close()
    }

    val shadowOffset = 2.2.dp.toPx()
    val shadowPath = Path().apply {
        addPath(path, Offset(0f, shadowOffset))
    }
    drawPath(shadowPath, color = Color(0x184A2820))
    drawPath(path, color = SoftWhite.copy(alpha = 0.97f))
    drawPath(
        path = path,
        color = Rosewood.copy(alpha = 0.32f),
        style = Stroke(width = 1.4.dp.toPx())
    )
}


private fun DrawScope.drawDreamCloudBubble(
    w: Float,
    h: Float,
    tailFraction: Float
) {
    val cloudBottomMargin = 14.dp.toPx()
    val bodyH = h - cloudBottomMargin

    val cloudPath = Path().apply {
        moveTo(w * 0.18f, bodyH * 0.12f)
        cubicTo(w * 0.22f, -bodyH * 0.08f, w * 0.38f, -bodyH * 0.08f, w * 0.44f, bodyH * 0.06f)
        cubicTo(w * 0.50f, -bodyH * 0.14f, w * 0.70f, -bodyH * 0.14f, w * 0.76f, bodyH * 0.06f)
        cubicTo(w * 0.86f, -bodyH * 0.02f, w * 1.02f, bodyH * 0.10f, w * 0.98f, bodyH * 0.35f)
        cubicTo(w * 1.05f, bodyH * 0.55f, w * 0.96f, bodyH * 0.86f, w * 0.82f, bodyH * 0.90f)
        cubicTo(w * 0.74f, bodyH * 1.08f, w * 0.56f, bodyH * 1.08f, w * 0.48f, bodyH * 0.94f)
        cubicTo(w * 0.40f, bodyH * 1.07f, w * 0.20f, bodyH * 1.06f, w * 0.14f, bodyH * 0.86f)
        cubicTo(-w * 0.05f, bodyH * 0.68f, -w * 0.04f, bodyH * 0.28f, w * 0.18f, bodyH * 0.12f)
        close()
    }

    val shadowOffset = 2.5.dp.toPx()
    val shadowPath = Path().apply {
        addPath(cloudPath, Offset(0f, shadowOffset))
    }
    drawPath(shadowPath, color = Color(0x1A2A2845))

    val dreamCloudBg = Color(0xFFF7F6FC).copy(alpha = 0.96f)
    val dreamBorder = Color(0xFF8B80B6).copy(alpha = 0.38f)
    val strokeW = 1.4.dp.toPx()

    drawPath(cloudPath, color = dreamCloudBg)
    drawPath(cloudPath, color = dreamBorder, style = Stroke(width = strokeW))

    val baseDotX = (w * tailFraction).coerceIn(w * 0.25f, w * 0.75f)

    val dot1Center = Offset(baseDotX, bodyH + 3.8.dp.toPx())
    val dot1R = 3.6.dp.toPx()
    drawCircle(color = dreamCloudBg, radius = dot1R, center = dot1Center)
    drawCircle(color = dreamBorder, radius = dot1R, center = dot1Center, style = Stroke(width = strokeW))

    val shift2 = if (tailFraction < 0.5f) -2.6.dp.toPx() else 2.6.dp.toPx()
    val dot2Center = Offset(baseDotX + shift2, bodyH + 9.0.dp.toPx())
    val dot2R = 2.4.dp.toPx()
    drawCircle(color = dreamCloudBg, radius = dot2R, center = dot2Center)
    drawCircle(color = dreamBorder, radius = dot2R, center = dot2Center, style = Stroke(width = strokeW))

    val shift3 = shift2 + (if (tailFraction < 0.5f) -2.0.dp.toPx() else 2.0.dp.toPx())
    val dot3Center = Offset(baseDotX + shift3, bodyH + 13.0.dp.toPx())
    val dot3R = 1.4.dp.toPx()
    drawCircle(color = dreamCloudBg, radius = dot3R, center = dot3Center)
    drawCircle(color = dreamBorder, radius = dot3R, center = dot3Center, style = Stroke(width = strokeW * 0.85f))
}
