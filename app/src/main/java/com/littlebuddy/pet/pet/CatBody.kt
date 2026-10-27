package com.littlebuddy.pet.pet

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import com.littlebuddy.pet.wardrobe.CatColorPalette
import com.littlebuddy.pet.wardrobe.WardrobeRegistry
import kotlin.math.PI
import kotlin.math.sin

/**
 * Thêm chu vi thân đứng (từ má phải, qua sườn, đáy bụng, sang má trái) vào một Path có sẵn.
 * Không gọi moveTo hay tạo contour mới để đảm bảo tính liên tục của Path.
 */
private fun Path.appendStandingBodyContour(
    cx: Float,
    cy: Float,
    u: Float,
    scaleX: Float,
    scaleY: Float
) {
    cubicTo(
        cx + u * 0.43f * scaleX, cy - u * 0.55f * scaleY,
        cx + u * 0.46f * scaleX, cy - u * 0.46f * scaleY,
        cx + u * 0.47f * scaleX, cy - u * 0.38f * scaleY
    )
    lineTo(cx + u * 0.50f * scaleX, cy - u * 0.35f * scaleY)
    lineTo(cx + u * 0.47f * scaleX, cy - u * 0.30f * scaleY)

    cubicTo(
        cx + u * 0.48f * scaleX, cy - u * 0.15f * scaleY,
        cx + u * 0.54f * scaleX, cy + u * 0.05f * scaleY,
        cx + u * 0.58f * scaleX, cy + u * 0.25f * scaleY
    )
    cubicTo(
        cx + u * 0.64f * scaleX, cy + u * 0.50f * scaleY,
        cx + u * 0.70f * scaleX, cy + u * 0.72f * scaleY,
        cx + u * 0.67f * scaleX, cy + u * 0.88f * scaleY
    )
    cubicTo(
        cx + u * 0.63f * scaleX, cy + u * 0.98f * scaleY,
        cx + u * 0.52f * scaleX, cy + u * 1.02f * scaleY,
        cx + u * 0.25f * scaleX, cy + u * 1.02f * scaleY
    )
    lineTo(cx - u * 0.25f * scaleX, cy + u * 1.02f * scaleY)

    cubicTo(
        cx - u * 0.52f * scaleX, cy + u * 1.02f * scaleY,
        cx - u * 0.63f * scaleX, cy + u * 0.98f * scaleY,
        cx - u * 0.67f * scaleX, cy + u * 0.88f * scaleY
    )
    cubicTo(
        cx - u * 0.70f * scaleX, cy + u * 0.72f * scaleY,
        cx - u * 0.64f * scaleX, cy + u * 0.50f * scaleY,
        cx - u * 0.58f * scaleX, cy + u * 0.25f * scaleY
    )
    cubicTo(
        cx - u * 0.54f * scaleX, cy + u * 0.05f * scaleY,
        cx - u * 0.48f * scaleX, cy - u * 0.15f * scaleY,
        cx - u * 0.47f * scaleX, cy - u * 0.30f * scaleY
    )
    lineTo(cx - u * 0.50f * scaleX, cy - u * 0.35f * scaleY)
    lineTo(cx - u * 0.47f * scaleX, cy - u * 0.38f * scaleY)

    cubicTo(
        cx - u * 0.46f * scaleX, cy - u * 0.46f * scaleY,
        cx - u * 0.43f * scaleX, cy - u * 0.55f * scaleY,
        cx - u * 0.39f * scaleX, cy - u * 0.65f * scaleY
    )
}

/**
 * Đường bao điền thân đứng dáng chuông liền mạch từ đầu đến bụng.
 */
internal fun createStandingBodyFillPath(
    cx: Float,
    cy: Float,
    u: Float,
    scaleX: Float = 1f,
    scaleY: Float = 1f
): Path = Path().apply {
    moveTo(cx - u * 0.16f * scaleX, cy - u * 0.86f * scaleY)
    quadraticTo(cx, cy - u * 0.88f * scaleY, cx + u * 0.16f * scaleX, cy - u * 0.86f * scaleY)

    lineTo(cx + u * 0.39f * scaleX, cy - u * 0.65f * scaleY)

    appendStandingBodyContour(cx, cy, u, scaleX, scaleY)

    close()
}

/**
 * Vẽ viền thân ngoài của mèo.
 */
internal fun DrawScope.drawStandingBodyOutline(
    cx: Float,
    cy: Float,
    u: Float,
    strokeW: Float,
    outlineColor: Color = CatOutline,
    scaleX: Float = 1f,
    scaleY: Float = 1f
) {
    val style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)

    val headTop = Path().apply {
        moveTo(cx - u * 0.16f * scaleX, cy - u * 0.86f * scaleY)
        quadraticTo(cx, cy - u * 0.88f * scaleY, cx + u * 0.16f * scaleX, cy - u * 0.86f * scaleY)
    }
    drawPath(headTop, color = outlineColor, style = style)

    val bodyPerimeter = Path().apply {
        moveTo(cx + u * 0.39f * scaleX, cy - u * 0.65f * scaleY)
        appendStandingBodyContour(cx, cy, u, scaleX, scaleY)
    }
    drawPath(bodyPerimeter, color = outlineColor, style = style)
}

/**
 * Đôi tai ngắn tròn mềm mại, đặt rộng ở góc trên đầu chuẩn mẫu ảnh.
 * Phần tô màu khép kín; phần viền KHÔNG khép kín (không vẽ nét qua gốc tai).
 */
internal fun DrawScope.drawCuteCatEars(
    cx: Float,
    cy: Float,
    u: Float,
    earWiggle: Float,
    strokeW: Float,
    palette: CatColorPalette = WardrobeRegistry.FUR_SIAMESE.palette!!,
    earL: Float = 0f,
    earR: Float = 0f
) {
    val style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)

    val effEarL = if (earL != 0f) earL else earWiggle
    withTransform({
        rotate(effEarL, pivot = Offset(cx - u * 0.28f, cy - u * 0.76f))
    }) {
        val leftEarFill = Path().apply {
            moveTo(cx - u * 0.16f, cy - u * 0.86f)
            cubicTo(
                cx - u * 0.20f, cy - u * 0.94f,
                cx - u * 0.26f, cy - u * 1.03f,
                cx - u * 0.32f, cy - u * 1.07f
            )
            cubicTo(
                cx - u * 0.35f, cy - u * 1.09f,
                cx - u * 0.38f, cy - u * 1.06f,
                cx - u * 0.39f, cy - u * 0.98f
            )
            cubicTo(
                cx - u * 0.41f, cy - u * 0.88f,
                cx - u * 0.41f, cy - u * 0.76f,
                cx - u * 0.39f, cy - u * 0.65f
            )
            close()
        }
        drawPath(leftEarFill, color = palette.patchColor)

        val leftEarStroke = Path().apply {
            moveTo(cx - u * 0.16f, cy - u * 0.86f)
            cubicTo(
                cx - u * 0.20f, cy - u * 0.94f,
                cx - u * 0.26f, cy - u * 1.03f,
                cx - u * 0.32f, cy - u * 1.07f
            )
            cubicTo(
                cx - u * 0.35f, cy - u * 1.09f,
                cx - u * 0.38f, cy - u * 1.06f,
                cx - u * 0.39f, cy - u * 0.98f
            )
            cubicTo(
                cx - u * 0.41f, cy - u * 0.88f,
                cx - u * 0.41f, cy - u * 0.76f,
                cx - u * 0.39f, cy - u * 0.65f
            )
        }
        drawPath(leftEarStroke, color = palette.outlineColor, style = style)
    }

    val effEarR = if (earR != 0f) earR else -earWiggle
    withTransform({
        rotate(effEarR, pivot = Offset(cx + u * 0.28f, cy - u * 0.76f))
    }) {
        val rightEarFill = Path().apply {
            moveTo(cx + u * 0.16f, cy - u * 0.86f)
            cubicTo(
                cx + u * 0.20f, cy - u * 0.94f,
                cx + u * 0.26f, cy - u * 1.03f,
                cx + u * 0.32f, cy - u * 1.07f
            )
            cubicTo(
                cx + u * 0.35f, cy - u * 1.09f,
                cx + u * 0.38f, cy - u * 1.06f,
                cx + u * 0.39f, cy - u * 0.98f
            )
            cubicTo(
                cx + u * 0.41f, cy - u * 0.88f,
                cx + u * 0.41f, cy - u * 0.76f,
                cx + u * 0.39f, cy - u * 0.65f
            )
            close()
        }
        val rightEarColor = palette.secondaryPatchColor ?: palette.patchColor
        drawPath(rightEarFill, color = rightEarColor)

        val rightEarStroke = Path().apply {
            moveTo(cx + u * 0.16f, cy - u * 0.86f)
            cubicTo(
                cx + u * 0.20f, cy - u * 0.94f,
                cx + u * 0.26f, cy - u * 1.03f,
                cx + u * 0.32f, cy - u * 1.07f
            )
            cubicTo(
                cx + u * 0.35f, cy - u * 1.09f,
                cx + u * 0.38f, cy - u * 1.06f,
                cx + u * 0.39f, cy - u * 0.98f
            )
            cubicTo(
                cx + u * 0.41f, cy - u * 0.88f,
                cx + u * 0.41f, cy - u * 0.76f,
                cx + u * 0.39f, cy - u * 0.65f
            )
        }
        drawPath(rightEarStroke, color = palette.outlineColor, style = style)
    }
}


/**
 * Đuôi mèo ngúng nguẩy sau hông phải theo nhịp hoạt ảnh.
 *
 * @param tailWag Độ lệch góc dao động của đuôi theo độ (swing amplitude in degrees, thường dao động trong khoảng -18f..+18f khi thức).
 */
internal fun DrawScope.drawCuteCatTail(
    cx: Float,
    cy: Float,
    u: Float,
    tailWag: Float,
    strokeW: Float,
    palette: CatColorPalette = WardrobeRegistry.FUR_SIAMESE.palette!!
) {
    val wag = tailWag * u * 0.005f
    val tailBaseX = cx + u * 0.50f
    val tailBaseY = cy + u * 0.68f

    val tailPath = Path().apply {
        moveTo(tailBaseX, tailBaseY)
        cubicTo(
            tailBaseX + u * 0.16f, tailBaseY + u * 0.02f,
            tailBaseX + u * 0.30f + wag, tailBaseY - u * 0.08f,
            tailBaseX + u * 0.32f + wag * 1.3f, tailBaseY - u * 0.28f
        )
        cubicTo(
            tailBaseX + u * 0.32f + wag * 1.3f, tailBaseY - u * 0.38f,
            tailBaseX + u * 0.24f + wag * 1.3f, tailBaseY - u * 0.40f,
            tailBaseX + u * 0.20f + wag, tailBaseY - u * 0.34f
        )
        cubicTo(
            tailBaseX + u * 0.18f + wag, tailBaseY - u * 0.22f,
            tailBaseX + u * 0.12f, tailBaseY - u * 0.12f,
            tailBaseX - u * 0.02f, tailBaseY - u * 0.02f
        )
        close()
    }
    drawPath(tailPath, color = palette.patchColor)
    drawPath(tailPath, color = palette.outlineColor, style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

/**
 * Hai bàn tay trước co tròn mềm mại ở 2 bên ngực (chuẩn 100% mẫu).
 * Vẽ một bàn tay trái rồi lật đối xứng sang bàn tay phải bằng withTransform.
 */
internal fun DrawScope.drawCurledFrontPaws(
    cx: Float,
    cy: Float,
    u: Float,
    strokeW: Float,
    palette: CatColorPalette = WardrobeRegistry.FUR_SIAMESE.palette!!,
    liftL: Float = 0f,
    liftR: Float = 0f,
    reach: Float = 0f
) {
    val paw = Path().apply {
        moveTo(cx - u * 0.52f, cy + u * 0.10f)
        cubicTo(
            cx - u * 0.46f, cy + u * 0.06f,
            cx - u * 0.34f, cy + u * 0.08f,
            cx - u * 0.25f, cy + u * 0.16f
        )
        cubicTo(
            cx - u * 0.20f, cy + u * 0.22f,
            cx - u * 0.22f, cy + u * 0.30f,
            cx - u * 0.30f, cy + u * 0.34f
        )
        cubicTo(
            cx - u * 0.38f, cy + u * 0.36f,
            cx - u * 0.48f, cy + u * 0.32f,
            cx - u * 0.54f, cy + u * 0.26f
        )
        close()
    }
    val strokeStyle = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)

    // Tay trước trái
    withTransform({
        translate(-reach * 0.14f * u, -liftL * 0.10f * u + reach * 0.20f * u)
    }) {
        drawPath(paw, color = palette.patchColor)
        drawPath(paw, color = palette.outlineColor, style = strokeStyle)
    }

    // Tay trước phải đối xứng qua trục tâm cx
    withTransform({
        scale(-1f, 1f, pivot = Offset(cx, cy))
        translate(-reach * 0.14f * u, -liftR * 0.10f * u + reach * 0.20f * u)
    }) {
        drawPath(paw, color = palette.patchColor)
        drawPath(paw, color = palette.outlineColor, style = strokeStyle)
    }
}

/**
 * Hai chân sau ngắn đỡ ở đáy sàn chuẩn ảnh mẫu.
 * Tham số bắt buộc (walkCycle, isWalking) đặt trước tham số mặc định (palette).
 */
internal fun DrawScope.drawShortHindFeet(
    cx: Float,
    cy: Float,
    u: Float,
    strokeW: Float,
    walkCycle: Float,
    isWalking: Boolean,
    palette: CatColorPalette = WardrobeRegistry.FUR_SIAMESE.palette!!,
    hindLiftL: Float = 0f,
    hindLiftR: Float = 0f
) {
    val baseFeetY = cy + u * 0.98f
    val leftPhase = if (hindLiftL != 0f || hindLiftR != 0f) {
        -hindLiftL * u * 0.06f
    } else if (isWalking) {
        sin(walkCycle * 2f * PI.toFloat()) * u * 0.05f
    } else 0f

    val rightPhase = if (hindLiftL != 0f || hindLiftR != 0f) {
        -hindLiftR * u * 0.06f
    } else {
        -leftPhase
    }

    // Chân sau trái
    val leftFoot = Path().apply {
        moveTo(cx - u * 0.46f, baseFeetY + leftPhase)
        lineTo(cx - u * 0.46f, baseFeetY + u * 0.09f + leftPhase)
        quadraticTo(cx - u * 0.36f, baseFeetY + u * 0.10f + leftPhase, cx - u * 0.26f, baseFeetY + u * 0.09f + leftPhase)
        lineTo(cx - u * 0.26f, baseFeetY + leftPhase)
    }
    drawPath(leftFoot, color = palette.bodyColor)
    drawPath(leftFoot, color = palette.outlineColor, style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round))

    // Chân sau phải
    val rightFoot = Path().apply {
        moveTo(cx + u * 0.26f, baseFeetY + rightPhase)
        lineTo(cx + u * 0.26f, baseFeetY + u * 0.09f + rightPhase)
        quadraticTo(cx + u * 0.36f, baseFeetY + u * 0.10f + rightPhase, cx + u * 0.46f, baseFeetY + u * 0.09f + rightPhase)
        lineTo(cx + u * 0.46f, baseFeetY + rightPhase)
    }
    drawPath(rightFoot, color = palette.bodyColor)
    drawPath(rightFoot, color = palette.outlineColor, style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round))
}
