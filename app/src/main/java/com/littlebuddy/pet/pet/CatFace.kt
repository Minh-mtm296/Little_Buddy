package com.littlebuddy.pet.pet

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.littlebuddy.pet.wardrobe.CatColorPalette
import com.littlebuddy.pet.wardrobe.WardrobeRegistry


internal fun DrawScope.drawChibiFace(
    cx: Float,
    cy: Float,
    u: Float,
    mood: PetMood,
    temporaryMood: PetMood?,
    actionState: PetActionState,
    eyeScaleY: Float,
    chewOffset: Float,
    strokeW: Float,
    palette: CatColorPalette = WardrobeRegistry.FUR_SIAMESE.palette!!
) {
    val maskPath = Path().apply {
        moveTo(cx, cy - u * 0.70f)
        cubicTo(
            cx + u * 0.16f, cy - u * 0.70f,
            cx + u * 0.28f, cy - u * 0.58f,
            cx + u * 0.30f, cy - u * 0.44f
        )
        cubicTo(
            cx + u * 0.28f, cy - u * 0.30f,
            cx + u * 0.18f, cy - u * 0.16f,
            cx, cy - u * 0.16f
        )
        cubicTo(
            cx - u * 0.18f, cy - u * 0.16f,
            cx - u * 0.28f, cy - u * 0.30f,
            cx - u * 0.30f, cy - u * 0.44f
        )
        cubicTo(
            cx - u * 0.28f, cy - u * 0.58f,
            cx - u * 0.16f, cy - u * 0.70f,
            cx, cy - u * 0.70f
        )
        close()
    }
    drawPath(maskPath, color = palette.patchColor)

    palette.secondaryPatchColor?.let { secColor ->
        val calicoRightPatch = Path().apply {
            moveTo(cx, cy - u * 0.70f)
            cubicTo(
                cx + u * 0.16f, cy - u * 0.70f,
                cx + u * 0.28f, cy - u * 0.58f,
                cx + u * 0.30f, cy - u * 0.44f
            )
            cubicTo(
                cx + u * 0.28f, cy - u * 0.30f,
                cx + u * 0.18f, cy - u * 0.16f,
                cx, cy - u * 0.16f
            )
            close()
        }
        drawPath(calicoRightPatch, color = secColor)
    }

    val hasDarkSide = palette.secondaryPatchColor != null
    fun eyeColorAt(ox: Float): Color =
        if (hasDarkSide && ox > 0f) Color(0xFFF2C14E) else palette.eyeColor

    val eyeY = cy - u * 0.44f
    val eyeOffsetX = u * 0.25f
    val eyeW = u * 0.070f
    val eyeH = u * 0.160f

    val isPetting = actionState == PetActionState.PETTING || temporaryMood == PetMood.HAPPY
    val isBlinking = eyeScaleY < 0.2f

    if (isPetting) {
        listOf(-eyeOffsetX to -6f, eyeOffsetX to 6f).forEach { (ox, rot) ->
            withTransform({
                rotate(rot, pivot = Offset(cx + ox, eyeY))
            }) {
                drawArc(
                    color = eyeColorAt(ox),
                    startAngle = 195f,
                    sweepAngle = 150f,
                    useCenter = false,
                    topLeft = Offset(cx + ox - eyeW * 0.85f, eyeY - eyeH * 0.25f),
                    size = Size(eyeW * 1.7f, eyeH * 0.75f),
                    style = Stroke(width = strokeW * 0.95f, cap = StrokeCap.Round)
                )
            }
        }
    } else if (isBlinking || mood == PetMood.SLEEPY) {
        listOf(-eyeOffsetX, eyeOffsetX).forEach { ox ->
            drawArc(
                color = eyeColorAt(ox),
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(cx + ox - eyeW * 0.8f, eyeY - eyeH * 0.25f),
                size = Size(eyeW * 1.6f, eyeH * 0.55f),
                style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round)
            )
        }
    } else {
        val currentEyeH = (eyeH * eyeScaleY).coerceAtLeast(eyeW * 0.35f)
        listOf(-eyeOffsetX to -6f, eyeOffsetX to 6f).forEach { (ox, rot) ->
            withTransform({
                rotate(rot, pivot = Offset(cx + ox, eyeY))
            }) {
                drawOval(
                    color = eyeColorAt(ox),
                    topLeft = Offset(cx + ox - eyeW / 2f, eyeY - currentEyeH / 2f),
                    size = Size(eyeW, currentEyeH)
                )
                if (hasDarkSide && ox > 0f) {
                    drawOval(
                        color = Color(0xFF2B2024),
                        topLeft = Offset(cx + ox - eyeW * 0.25f, eyeY - currentEyeH * 0.30f),
                        size = Size(eyeW * 0.5f, currentEyeH * 0.6f)
                    )
                }
            }
        }
    }

    val noseY = cy - u * 0.43f
    val noseW = u * 0.085f
    val noseH = u * 0.065f
    val nosePath = Path().apply {
        moveTo(cx - noseW * 0.5f, noseY - noseH * 0.3f)
        quadraticTo(cx, noseY - noseH * 0.45f, cx + noseW * 0.5f, noseY - noseH * 0.3f)
        quadraticTo(cx + noseW * 0.25f, noseY + noseH * 0.55f, cx, noseY + noseH * 0.55f)
        quadraticTo(cx - noseW * 0.25f, noseY + noseH * 0.55f, cx - noseW * 0.5f, noseY - noseH * 0.3f)
        close()
    }
    drawPath(nosePath, color = palette.noseColor)

    val mouthY = cy - u * 0.28f + (chewOffset * 0.3f)
    if (actionState == PetActionState.EATING || mood == PetMood.EATING) {
        drawOval(
            color = palette.eyeColor,
            topLeft = Offset(cx - u * 0.035f, mouthY - u * 0.015f),
            size = Size(u * 0.070f, u * 0.055f)
        )
    } else {
        val mouthPath = Path().apply {
            moveTo(cx - u * 0.025f, mouthY + u * 0.018f)
            lineTo(cx, mouthY - u * 0.002f)
            lineTo(cx + u * 0.025f, mouthY + u * 0.018f)
        }
        drawPath(mouthPath, color = palette.outlineColor, style = Stroke(width = strokeW * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }

    val blushAlpha = if (isPetting) 0.65f else 0.35f
    val blushRadius = u * 0.055f
    val blushY = cy - u * 0.35f
    listOf(-1f, 1f).forEach { dir ->
        drawCircle(
            color = Color(0xFFFFB4A2).copy(alpha = blushAlpha),
            radius = blushRadius,
            center = Offset(cx + dir * u * 0.30f, blushY)
        )
    }


    val whiskerColor = palette.outlineColor.copy(alpha = 0.60f)
    val whiskerStroke = Stroke(width = strokeW * 0.52f, cap = StrokeCap.Round)
    val whiskerBaseY = cy - u * 0.35f
    listOf(-1f, 1f).forEach { dir ->
        val startX = cx + dir * u * 0.31f
        val topWhisker = Path().apply {
            moveTo(startX, whiskerBaseY - u * 0.038f)
            quadraticTo(
                startX + dir * u * 0.08f, whiskerBaseY - u * 0.055f,
                startX + dir * u * 0.15f, whiskerBaseY - u * 0.048f
            )
        }
        val midWhisker = Path().apply {
            moveTo(startX, whiskerBaseY)
            quadraticTo(
                startX + dir * u * 0.09f, whiskerBaseY - u * 0.005f,
                startX + dir * u * 0.165f, whiskerBaseY + u * 0.002f
            )
        }
        val btmWhisker = Path().apply {
            moveTo(startX, whiskerBaseY + u * 0.038f)
            quadraticTo(
                startX + dir * u * 0.08f, whiskerBaseY + u * 0.035f,
                startX + dir * u * 0.145f, whiskerBaseY + u * 0.052f
            )
        }
        drawPath(topWhisker, color = whiskerColor, style = whiskerStroke)
        drawPath(midWhisker, color = whiskerColor, style = whiskerStroke)
        drawPath(btmWhisker, color = whiskerColor, style = whiskerStroke)
    }
}
