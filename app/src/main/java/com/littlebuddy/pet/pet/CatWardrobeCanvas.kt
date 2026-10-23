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
import com.littlebuddy.pet.ui.theme.Rosewood
import com.littlebuddy.pet.wardrobe.WardrobeRegistry
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ═════════════════════════════════════════════════════════════════════════════
// VẼ PHỤ KIỆN VÀ TRANG PHỤC KHI ĐỨNG (STANDING WARDROBE)
// ═════════════════════════════════════════════════════════════════════════════

internal fun DrawScope.drawHeadwearOnChibiCat(
    headwearId: String?,
    cx: Float,
    cy: Float,
    u: Float,
    strokeW: Float
) {
    if (headwearId == null) return
    when (headwearId) {
        "head_ribbon" -> {
            val rx = cx - u * 0.24f
            val ry = cy - u * 0.86f
            val rw = u * 0.15f
            val rh = u * 0.10f
            listOf(-1f, 1f).forEach { side ->
                val wing = Path().apply {
                    moveTo(rx, ry)
                    quadraticTo(rx + side * rw, ry - rh, rx + side * rw * 1.05f, ry)
                    quadraticTo(rx + side * rw, ry + rh, rx, ry)
                    close()
                }
                drawPath(wing, color = Color(0xFFFF8FAB))
                drawPath(wing, color = Color(0xFF6B4247), style = Stroke(width = strokeW * 0.7f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            drawCircle(color = Color(0xFFFF6584), radius = rw * 0.25f, center = Offset(rx, ry))
            drawCircle(color = Color(0xFF6B4247), radius = rw * 0.25f, center = Offset(rx, ry), style = Stroke(width = strokeW * 0.6f))
        }
        "head_straw_hat" -> {
            val hy = cy - u * 0.86f
            val brimW = u * 0.64f
            val brimH = u * 0.14f
            drawOval(color = Color(0xFFEEDAA2), topLeft = Offset(cx - brimW / 2f, hy - brimH / 2f), size = Size(brimW, brimH))
            drawOval(color = Color(0xFF6B4247), topLeft = Offset(cx - brimW / 2f, hy - brimH / 2f), size = Size(brimW, brimH), style = Stroke(width = strokeW * 0.75f))

            val domePath = Path().apply {
                moveTo(cx - u * 0.22f, hy)
                cubicTo(cx - u * 0.20f, hy - u * 0.28f, cx + u * 0.20f, hy - u * 0.28f, cx + u * 0.22f, hy)
                close()
            }
            drawPath(domePath, color = Color(0xFFEEDAA2))
            drawPath(domePath, color = Color(0xFF6B4247), style = Stroke(width = strokeW * 0.75f, cap = StrokeCap.Round, join = StrokeJoin.Round))

            val ribbonPath = Path().apply {
                moveTo(cx - u * 0.22f, hy - u * 0.02f)
                quadraticTo(cx, hy + u * 0.02f, cx + u * 0.22f, hy - u * 0.02f)
                lineTo(cx + u * 0.21f, hy - u * 0.08f)
                quadraticTo(cx, hy - u * 0.04f, cx - u * 0.21f, hy - u * 0.08f)
                close()
            }
            drawPath(ribbonPath, color = Color(0xFF8B5E3C))
        }
        "head_beret" -> {
            val bx = cx - u * 0.08f
            val by = cy - u * 0.88f
            withTransform({
                rotate(-12f, pivot = Offset(bx, by))
            }) {
                val beretPath = Path().apply {
                    moveTo(bx - u * 0.32f, by + u * 0.04f)
                    cubicTo(bx - u * 0.36f, by - u * 0.22f, bx + u * 0.32f, by - u * 0.24f, bx + u * 0.32f, by + u * 0.04f)
                    cubicTo(bx + u * 0.20f, by + u * 0.12f, bx - u * 0.20f, by + u * 0.12f, bx - u * 0.32f, by + u * 0.04f)
                    close()
                }
                drawPath(beretPath, color = Color(0xFF8E4A49))
                drawPath(beretPath, color = Color(0xFF4A2525), style = Stroke(width = strokeW * 0.75f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                val stemPath = Path().apply {
                    moveTo(bx, by - u * 0.20f)
                    quadraticTo(bx - u * 0.03f, by - u * 0.28f, bx - u * 0.02f, by - u * 0.30f)
                }
                drawPath(stemPath, color = Color(0xFF4A2525), style = Stroke(width = strokeW * 0.8f, cap = StrokeCap.Round))
            }
        }
        "head_flower_crown" -> {
            val fy = cy - u * 0.84f
            val vinePath = Path().apply {
                moveTo(cx - u * 0.32f, fy + u * 0.05f)
                quadraticTo(cx, fy - u * 0.05f, cx + u * 0.32f, fy + u * 0.05f)
            }
            drawPath(vinePath, color = Color(0xFF74A57F), style = Stroke(width = strokeW * 0.7f, cap = StrokeCap.Round))

            listOf(
                Offset(cx - u * 0.26f, fy + u * 0.04f),
                Offset(cx - u * 0.13f, fy - u * 0.01f),
                Offset(cx, fy - u * 0.04f),
                Offset(cx + u * 0.13f, fy - u * 0.01f),
                Offset(cx + u * 0.26f, fy + u * 0.04f)
            ).forEachIndexed { idx, pt ->
                val petalColor = if (idx % 2 == 0) Color.White else Color(0xFFFFD1DC)
                val flowerR = u * 0.045f
                drawCircle(color = petalColor, radius = flowerR, center = pt)
                drawCircle(color = Color(0xFFFFD166), radius = flowerR * 0.45f, center = pt)
                drawCircle(color = Color(0xFF5D3E44), radius = flowerR, center = pt, style = Stroke(width = strokeW * 0.45f))
            }
        }
        "head_crown" -> {
            val cw = u * 0.32f
            val ch = u * 0.20f
            val cBaseY = cy - u * 0.86f
            val crownPath = Path().apply {
                moveTo(cx - cw / 2f, cBaseY)
                lineTo(cx - cw / 2f * 1.15f, cBaseY - ch * 0.75f)
                lineTo(cx - cw * 0.20f, cBaseY - ch * 0.35f)
                lineTo(cx, cBaseY - ch)
                lineTo(cx + cw * 0.20f, cBaseY - ch * 0.35f)
                lineTo(cx + cw / 2f * 1.15f, cBaseY - ch * 0.75f)
                lineTo(cx + cw / 2f, cBaseY)
                close()
            }
            drawPath(crownPath, color = Color(0xFFFFC72C))
            drawPath(crownPath, color = Color(0xFF7A5800), style = Stroke(width = strokeW * 0.75f, cap = StrokeCap.Round, join = StrokeJoin.Round))

            listOf(
                Offset(cx - cw / 2f * 1.15f, cBaseY - ch * 0.75f),
                Offset(cx, cBaseY - ch),
                Offset(cx + cw / 2f * 1.15f, cBaseY - ch * 0.75f)
            ).forEach { pt ->
                drawCircle(color = Color(0xFFE63946), radius = u * 0.026f, center = pt)
                drawCircle(color = Color(0xFF5A1015), radius = u * 0.026f, center = pt, style = Stroke(width = strokeW * 0.4f))
            }
        }
    }
}

internal fun DrawScope.drawNeckwearOnChibiCat(
    neckwearId: String?,
    cx: Float,
    cy: Float,
    u: Float,
    strokeW: Float
) {
    if (neckwearId == null) return
    val neckY = cy - u * 0.12f
    when (neckwearId) {
        "neck_bell_collar" -> {
            val collarW = u * 0.84f
            val collarH = u * 0.12f
            val collarPath = Path().apply {
                moveTo(cx - collarW / 2f, neckY - u * 0.04f)
                quadraticTo(cx, neckY + u * 0.05f, cx + collarW / 2f, neckY - u * 0.04f)
                lineTo(cx + collarW / 2f, neckY + collarH - u * 0.04f)
                quadraticTo(cx, neckY + collarH + u * 0.05f, cx - collarW / 2f, neckY + collarH - u * 0.04f)
                close()
            }
            drawPath(collarPath, color = Color(0xFFE76F51))
            drawPath(collarPath, color = Color(0xFF5A2A20), style = Stroke(width = strokeW * 0.75f, cap = StrokeCap.Round, join = StrokeJoin.Round))

            val bellY = neckY + u * 0.10f
            val bellR = u * 0.056f
            drawCircle(color = Color(0xFFFFD166), radius = bellR, center = Offset(cx, bellY))
            drawCircle(color = Color(0xFF7A5800), radius = bellR, center = Offset(cx, bellY), style = Stroke(width = strokeW * 0.65f))
            drawCircle(color = Color(0xFF7A5800), radius = bellR * 0.28f, center = Offset(cx, bellY + bellR * 0.28f))
            drawLine(
                color = Color(0xFF7A5800),
                start = Offset(cx - bellR * 0.65f, bellY + bellR * 0.28f),
                end = Offset(cx + bellR * 0.65f, bellY + bellR * 0.28f),
                strokeWidth = strokeW * 0.45f
            )
        }
        "neck_warm_scarf" -> {
            val scarfW = u * 0.88f
            val scarfH = u * 0.18f
            val scarfPath = Path().apply {
                moveTo(cx - scarfW / 2f, neckY - u * 0.05f)
                quadraticTo(cx, neckY + u * 0.06f, cx + scarfW / 2f, neckY - u * 0.05f)
                quadraticTo(cx + scarfW / 2f * 0.95f, neckY + scarfH, cx, neckY + scarfH + u * 0.04f)
                quadraticTo(cx - scarfW / 2f * 0.95f, neckY + scarfH, cx - scarfW / 2f, neckY - u * 0.05f)
                close()
            }
            drawPath(scarfPath, color = Color(0xFFE07A5F))
            drawPath(scarfPath, color = Color(0xFF5A2A20), style = Stroke(width = strokeW * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round))

            val tailPath = Path().apply {
                moveTo(cx + u * 0.12f, neckY + u * 0.08f)
                lineTo(cx + u * 0.28f, neckY + u * 0.38f)
                lineTo(cx + u * 0.15f, neckY + u * 0.41f)
                lineTo(cx + u * 0.02f, neckY + u * 0.12f)
                close()
            }
            drawPath(tailPath, color = Color(0xFFD46246))
            drawPath(tailPath, color = Color(0xFF5A2A20), style = Stroke(width = strokeW * 0.75f, cap = StrokeCap.Round, join = StrokeJoin.Round))

            for (i in 0..3) {
                val tx = cx + u * (0.15f + i * 0.04f)
                val ty = neckY + u * 0.39f
                drawLine(
                    color = Color(0xFF5A2A20),
                    start = Offset(tx, ty),
                    end = Offset(tx + u * 0.02f, ty + u * 0.05f),
                    strokeWidth = strokeW * 0.5f,
                    cap = StrokeCap.Round
                )
            }
        }
        "neck_blue_bow" -> {
            val bandW = u * 0.80f
            val bandPath = Path().apply {
                moveTo(cx - bandW / 2f, neckY - u * 0.02f)
                quadraticTo(cx, neckY + u * 0.04f, cx + bandW / 2f, neckY - u * 0.02f)
                lineTo(cx + bandW / 2f, neckY + u * 0.04f)
                quadraticTo(cx, neckY + u * 0.10f, cx - bandW / 2f, neckY + u * 0.04f)
                close()
            }
            drawPath(bandPath, color = Color(0xFFC7E4E8))
            drawPath(bandPath, color = Color(0xFF33626C), style = Stroke(width = strokeW * 0.65f, cap = StrokeCap.Round, join = StrokeJoin.Round))

            val bw = u * 0.18f
            val bh = u * 0.11f
            val by = neckY + u * 0.04f
            listOf(-1f, 1f).forEach { side ->
                val wing = Path().apply {
                    moveTo(cx, by)
                    quadraticTo(cx + side * bw, by - bh, cx + side * bw * 1.05f, by)
                    quadraticTo(cx + side * bw, by + bh, cx, by)
                    close()
                }
                drawPath(wing, color = Color(0xFF88C9D4))
                drawPath(wing, color = Color(0xFF33626C), style = Stroke(width = strokeW * 0.7f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            drawCircle(color = Color(0xFF569DAA), radius = bw * 0.28f, center = Offset(cx, by))
            drawCircle(color = Color(0xFF33626C), radius = bw * 0.28f, center = Offset(cx, by), style = Stroke(width = strokeW * 0.6f))
        }
        "neck_pearl" -> {
            val count = 9
            for (i in 0 until count) {
                val t = (i - (count - 1) / 2f) / ((count - 1) / 2f)
                val px = cx + t * (u * 0.38f)
                val py = neckY + (1f - t * t * 0.45f) * (u * 0.12f)
                val pearlR = u * 0.036f
                drawCircle(color = Color(0xFFFFFDF9), radius = pearlR, center = Offset(px, py))
                drawCircle(color = Color(0xFFB8ADA0), radius = pearlR, center = Offset(px, py), style = Stroke(width = strokeW * 0.45f))
                drawCircle(color = Color.White, radius = pearlR * 0.35f, center = Offset(px - pearlR * 0.3f, py - pearlR * 0.3f))
            }
        }
    }
}

internal fun DrawScope.drawBodywearOnChibiCat(
    bodywearId: String?,
    cx: Float,
    cy: Float,
    u: Float,
    strokeW: Float,
    scaleX: Float = 1f,
    scaleY: Float = 1f
) {
    if (bodywearId == null) return
    when (bodywearId) {
        "body_sweater" -> {
            val sweaterColor = Color(0xFFB5D5C5)
            val sweaterOutline = Color(0xFF385E4E)
            val ribColor = Color(0xFF98C4AE)

            val sweaterBodyPath = Path().apply {
                moveTo(cx - u * 0.34f * scaleX, cy - u * 0.14f * scaleY)
                quadraticTo(cx, cy - u * 0.10f * scaleY, cx + u * 0.34f * scaleX, cy - u * 0.14f * scaleY)
                cubicTo(
                    cx + u * 0.50f * scaleX, cy - u * 0.08f * scaleY,
                    cx + u * 0.56f * scaleX, cy + u * 0.10f * scaleY,
                    cx + u * 0.60f * scaleX, cy + u * 0.28f * scaleY
                )
                cubicTo(
                    cx + u * 0.66f * scaleX, cy + u * 0.52f * scaleY,
                    cx + u * 0.72f * scaleX, cy + u * 0.72f * scaleY,
                    cx + u * 0.68f * scaleX, cy + u * 0.84f * scaleY
                )
                quadraticTo(cx, cy + u * 0.88f * scaleY, cx - u * 0.68f * scaleX, cy + u * 0.84f * scaleY)
                cubicTo(
                    cx - u * 0.72f * scaleX, cy + u * 0.72f * scaleY,
                    cx - u * 0.66f * scaleX, cy + u * 0.52f * scaleY,
                    cx - u * 0.60f * scaleX, cy + u * 0.28f * scaleY
                )
                cubicTo(
                    cx - u * 0.56f * scaleX, cy + u * 0.10f * scaleY,
                    cx - u * 0.50f * scaleX, cy - u * 0.08f * scaleY,
                    cx - u * 0.34f * scaleX, cy - u * 0.14f * scaleY
                )
                close()
            }
            drawPath(sweaterBodyPath, color = sweaterColor)
            drawPath(sweaterBodyPath, color = sweaterOutline, style = Stroke(width = strokeW * 0.85f, cap = StrokeCap.Round, join = StrokeJoin.Round))

            val ribLines = listOf(
                Pair(0.12f, 0.46f),
                Pair(0.32f, 0.54f),
                Pair(0.52f, 0.62f),
                Pair(0.70f, 0.65f)
            )
            ribLines.forEach { (yFactor, wFactor) ->
                val linePath = Path().apply {
                    moveTo(cx - u * wFactor * scaleX, cy + u * yFactor * scaleY)
                    quadraticTo(cx, cy + u * (yFactor + 0.05f) * scaleY, cx + u * wFactor * scaleX, cy + u * yFactor * scaleY)
                }
                drawPath(linePath, color = ribColor, style = Stroke(width = strokeW * 0.55f, cap = StrokeCap.Round))
            }

            val hemPath = Path().apply {
                moveTo(cx - u * 0.66f * scaleX, cy + u * 0.80f * scaleY)
                quadraticTo(cx, cy + u * 0.84f * scaleY, cx + u * 0.66f * scaleX, cy + u * 0.80f * scaleY)
                lineTo(cx + u * 0.68f * scaleX, cy + u * 0.84f * scaleY)
                quadraticTo(cx, cy + u * 0.88f * scaleY, cx - u * 0.68f * scaleX, cy + u * 0.84f * scaleY)
                close()
            }
            drawPath(hemPath, color = ribColor)

            val collarPath = Path().apply {
                moveTo(cx - u * 0.34f * scaleX, cy - u * 0.20f * scaleY)
                lineTo(cx + u * 0.34f * scaleX, cy - u * 0.20f * scaleY)
                quadraticTo(cx + u * 0.36f * scaleX, cy - u * 0.12f * scaleY, cx + u * 0.32f * scaleX, cy - u * 0.09f * scaleY)
                quadraticTo(cx, cy - u * 0.05f * scaleY, cx - u * 0.32f * scaleX, cy - u * 0.09f * scaleY)
                quadraticTo(cx - u * 0.36f * scaleX, cy - u * 0.12f * scaleY, cx - u * 0.34f * scaleX, cy - u * 0.20f * scaleY)
                close()
            }
            drawPath(collarPath, color = Color(0xFFA2CDB7))
            drawPath(collarPath, color = sweaterOutline, style = Stroke(width = strokeW * 0.75f, cap = StrokeCap.Round, join = StrokeJoin.Round))

            for (i in -2..2) {
                val gx = cx + i * (u * 0.11f * scaleX)
                drawLine(
                    color = sweaterOutline.copy(alpha = 0.5f),
                    start = Offset(gx, cy - u * 0.18f * scaleY),
                    end = Offset(gx, cy - u * 0.08f * scaleY),
                    strokeWidth = strokeW * 0.4f,
                    cap = StrokeCap.Round
                )
            }
        }
        "body_overalls" -> {
            val denimBlue = Color(0xFF5E8DBF)
            val denimDark = Color(0xFF223E63)
            val stitchGold = Color(0xFFE8B36E)

            val leftStrap = Path().apply {
                moveTo(cx - u * 0.36f * scaleX, cy - u * 0.38f * scaleY)
                quadraticTo(
                    cx - u * 0.30f * scaleX, cy - u * 0.12f * scaleY,
                    cx - u * 0.22f * scaleX, cy + u * 0.12f * scaleY
                )
            }
            val rightStrap = Path().apply {
                moveTo(cx + u * 0.36f * scaleX, cy - u * 0.38f * scaleY)
                quadraticTo(
                    cx + u * 0.30f * scaleX, cy - u * 0.12f * scaleY,
                    cx + u * 0.22f * scaleX, cy + u * 0.12f * scaleY
                )
            }
            drawPath(leftStrap, color = denimBlue, style = Stroke(width = u * 0.095f, cap = StrokeCap.Round))
            drawPath(leftStrap, color = denimDark, style = Stroke(width = strokeW * 0.75f, cap = StrokeCap.Round))
            drawPath(rightStrap, color = denimBlue, style = Stroke(width = u * 0.095f, cap = StrokeCap.Round))
            drawPath(rightStrap, color = denimDark, style = Stroke(width = strokeW * 0.75f, cap = StrokeCap.Round))

            val overallsBody = Path().apply {
                moveTo(cx - u * 0.32f * scaleX, cy + u * 0.06f * scaleY)
                lineTo(cx + u * 0.32f * scaleX, cy + u * 0.06f * scaleY)
                lineTo(cx + u * 0.52f * scaleX, cy + u * 0.24f * scaleY)
                cubicTo(
                    cx + u * 0.64f * scaleX, cy + u * 0.50f * scaleY,
                    cx + u * 0.72f * scaleX, cy + u * 0.72f * scaleY,
                    cx + u * 0.68f * scaleX, cy + u * 0.88f * scaleY
                )
                cubicTo(
                    cx + u * 0.63f * scaleX, cy + u * 0.99f * scaleY,
                    cx + u * 0.52f * scaleX, cy + u * 1.02f * scaleY,
                    cx + u * 0.18f * scaleX, cy + u * 1.02f * scaleY
                )
                lineTo(cx, cy + u * 0.88f * scaleY)
                lineTo(cx - u * 0.18f * scaleX, cy + u * 1.02f * scaleY)
                cubicTo(
                    cx - u * 0.52f * scaleX, cy + u * 1.02f * scaleY,
                    cx - u * 0.63f * scaleX, cy + u * 0.99f * scaleY,
                    cx - u * 0.68f * scaleX, cy + u * 0.88f * scaleY
                )
                cubicTo(
                    cx - u * 0.72f * scaleX, cy + u * 0.72f * scaleY,
                    cx - u * 0.64f * scaleX, cy + u * 0.50f * scaleY,
                    cx - u * 0.52f * scaleX, cy + u * 0.24f * scaleY
                )
                lineTo(cx - u * 0.32f * scaleX, cy + u * 0.06f * scaleY)
                close()
            }
            drawPath(overallsBody, color = denimBlue)
            drawPath(overallsBody, color = denimDark, style = Stroke(width = strokeW * 0.85f, cap = StrokeCap.Round, join = StrokeJoin.Round))

            drawLine(
                color = stitchGold,
                start = Offset(cx, cy + u * 0.64f * scaleY),
                end = Offset(cx, cy + u * 0.88f * scaleY),
                strokeWidth = strokeW * 0.45f,
                cap = StrokeCap.Round
            )

            val pocketW = u * 0.28f * scaleX
            val pocketH = u * 0.18f * scaleY
            val pocketY = cy + u * 0.28f * scaleY
            val pocketPath = Path().apply {
                moveTo(cx - pocketW / 2f, pocketY)
                lineTo(cx + pocketW / 2f, pocketY)
                lineTo(cx + pocketW / 2f, pocketY + pocketH * 0.7f)
                quadraticTo(cx, pocketY + pocketH * 1.15f, cx - pocketW / 2f, pocketY + pocketH * 0.7f)
                close()
            }
            drawPath(pocketPath, color = Color(0xFF4C75A3))
            drawPath(pocketPath, color = stitchGold, style = Stroke(width = strokeW * 0.45f, cap = StrokeCap.Round, join = StrokeJoin.Round))

            listOf(-1f, 1f).forEach { side ->
                val btnX = cx + side * u * 0.22f * scaleX
                val btnY = cy + u * 0.10f * scaleY
                drawCircle(color = Color(0xFFFFD166), radius = u * 0.034f, center = Offset(btnX, btnY))
                drawCircle(color = Color(0xFF7A5800), radius = u * 0.034f, center = Offset(btnX, btnY), style = Stroke(width = strokeW * 0.45f))
                drawCircle(color = Color(0xFF7A5800), radius = u * 0.010f, center = Offset(btnX, btnY))

                val sideBtnX = cx + side * u * 0.54f * scaleX
                val sideBtnY = cy + u * 0.38f * scaleY
                drawCircle(color = Color(0xFFFFD166), radius = u * 0.024f, center = Offset(sideBtnX, sideBtnY))
                drawCircle(color = Color(0xFF7A5800), radius = u * 0.024f, center = Offset(sideBtnX, sideBtnY), style = Stroke(width = strokeW * 0.4f))
            }
        }
        "body_royal_cape" -> {
            val velvetRed = Color(0xFF9E2A2B)
            val velvetDark = Color(0xFF480E10)
            val royalGold = Color(0xFFFFD166)

            val capePath = Path().apply {
                moveTo(cx - u * 0.38f * scaleX, cy - u * 0.18f * scaleY)
                quadraticTo(cx, cy - u * 0.12f * scaleY, cx + u * 0.38f * scaleX, cy - u * 0.18f * scaleY)
                cubicTo(
                    cx + u * 0.65f * scaleX, cy + u * 0.08f * scaleY,
                    cx + u * 0.80f * scaleX, cy + u * 0.50f * scaleY,
                    cx + u * 0.76f * scaleX, cy + u * 0.94f * scaleY
                )
                cubicTo(
                    cx + u * 0.40f * scaleX, cy + u * 0.88f * scaleY,
                    cx + u * 0.20f * scaleX, cy + u * 0.96f * scaleY,
                    cx, cy + u * 0.92f * scaleY
                )
                cubicTo(
                    cx - u * 0.20f * scaleX, cy + u * 0.96f * scaleY,
                    cx - u * 0.40f * scaleX, cy + u * 0.88f * scaleY,
                    cx - u * 0.76f * scaleX, cy + u * 0.94f * scaleY
                )
                cubicTo(
                    cx - u * 0.80f * scaleX, cy + u * 0.50f * scaleY,
                    cx - u * 0.65f * scaleX, cy + u * 0.08f * scaleY,
                    cx - u * 0.38f * scaleX, cy - u * 0.18f * scaleY
                )
                close()
            }
            drawPath(capePath, color = velvetRed)
            drawPath(capePath, color = velvetDark, style = Stroke(width = strokeW * 0.85f, cap = StrokeCap.Round, join = StrokeJoin.Round))

            val goldTrimLeft = Path().apply {
                moveTo(cx - u * 0.32f * scaleX, cy - u * 0.14f * scaleY)
                cubicTo(
                    cx - u * 0.58f * scaleX, cy + u * 0.12f * scaleY,
                    cx - u * 0.72f * scaleX, cy + u * 0.50f * scaleY,
                    cx - u * 0.68f * scaleX, cy + u * 0.90f * scaleY
                )
            }
            val goldTrimRight = Path().apply {
                moveTo(cx + u * 0.32f * scaleX, cy - u * 0.14f * scaleY)
                cubicTo(
                    cx + u * 0.58f * scaleX, cy + u * 0.12f * scaleY,
                    cx + u * 0.72f * scaleX, cy + u * 0.50f * scaleY,
                    cx + u * 0.68f * scaleX, cy + u * 0.90f * scaleY
                )
            }
            drawPath(goldTrimLeft, color = royalGold, style = Stroke(width = strokeW * 0.5f, cap = StrokeCap.Round))
            drawPath(goldTrimRight, color = royalGold, style = Stroke(width = strokeW * 0.5f, cap = StrokeCap.Round))

            val furCollar = Path().apply {
                moveTo(cx - u * 0.40f * scaleX, cy - u * 0.20f * scaleY)
                quadraticTo(cx, cy - u * 0.06f * scaleY, cx + u * 0.40f * scaleX, cy - u * 0.20f * scaleY)
                quadraticTo(cx + u * 0.36f * scaleX, cy - u * 0.04f * scaleY, cx + u * 0.24f * scaleX, cy + u * 0.02f * scaleY)
                quadraticTo(cx, cy + u * 0.08f * scaleY, cx - u * 0.24f * scaleX, cy + u * 0.02f * scaleY)
                quadraticTo(cx - u * 0.36f * scaleX, cy - u * 0.04f * scaleY, cx - u * 0.40f * scaleX, cy - u * 0.20f * scaleY)
                close()
            }
            drawPath(furCollar, color = Color(0xFFFFF9F0))
            drawPath(furCollar, color = velvetDark, style = Stroke(width = strokeW * 0.7f, cap = StrokeCap.Round, join = StrokeJoin.Round))

            listOf(-0.24f, -0.10f, 0.10f, 0.24f).forEach { fx ->
                drawCircle(color = Color(0xFF2C2424), radius = u * 0.016f, center = Offset(cx + fx * u * scaleX, cy - u * 0.08f * scaleY))
            }

            val broochY = cy - u * 0.04f * scaleY
            val broochR = u * 0.046f
            drawCircle(color = royalGold, radius = broochR, center = Offset(cx, broochY))
            drawCircle(color = Color(0xFF7A5800), radius = broochR, center = Offset(cx, broochY), style = Stroke(width = strokeW * 0.5f))
            drawCircle(color = Color(0xFFD90429), radius = broochR * 0.55f, center = Offset(cx, broochY))
        }
    }
}

// ═════════════════════════════════════════════════════════════════════════════
// VẼ PHỤ KIỆN VÀ TRANG PHỤC KHI NGỦ (SLEEPING WARDROBE)
// ═════════════════════════════════════════════════════════════════════════════

internal fun DrawScope.drawSleepingBodywear(
    bodywearId: String?,
    cx: Float,
    cy: Float,
    u: Float,
    strokeW: Float
) {
    if (bodywearId == null) return
    when (bodywearId) {
        "body_sweater" -> {
            val sweaterColor = Color(0xFFB5D5C5)
            val sweaterOutline = Color(0xFF385E4E)
            val ribColor = Color(0xFF98C4AE)

            val sweaterPath = Path().apply {
                moveTo(cx - u * 0.25f, cy - u * 0.68f)
                cubicTo(cx + u * 0.10f, cy - u * 0.75f, cx + u * 0.55f, cy - u * 0.62f, cx + u * 0.88f, cy - u * 0.28f)
                cubicTo(cx + u * 0.98f, cy - u * 0.02f, cx + u * 0.82f, cy + u * 0.32f, cx + u * 0.50f, cy + u * 0.34f)
                cubicTo(cx + u * 0.20f, cy + u * 0.25f, cx - u * 0.02f, cy - u * 0.08f, cx - u * 0.18f, cy - u * 0.40f)
                close()
            }
            drawPath(sweaterPath, color = sweaterColor)

            listOf(-0.05f, 0.15f, 0.38f, 0.60f).forEach { sx ->
                val stripePath = Path().apply {
                    moveTo(cx + u * sx, cy - u * 0.68f + (sx * sx) * u * 0.15f)
                    quadraticTo(cx + u * (sx + 0.04f), cy - u * 0.10f, cx + u * (sx - 0.05f), cy + u * 0.30f)
                }
                drawPath(
                    stripePath,
                    color = ribColor,
                    style = Stroke(width = strokeW * 0.85f, cap = StrokeCap.Round)
                )
            }
            drawPath(
                sweaterPath,
                color = sweaterOutline,
                style = Stroke(width = strokeW * 0.85f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        "body_overalls" -> {
            val overalls = Path().apply {
                moveTo(cx + u * 0.05f, cy - u * 0.68f)
                cubicTo(cx + u * 0.40f, cy - u * 0.65f, cx + u * 0.88f, cy - u * 0.35f, cx + u * 0.92f, cy + u * 0.05f)
                cubicTo(cx + u * 0.75f, cy + u * 0.32f, cx + u * 0.45f, cy + u * 0.35f, cx + u * 0.15f, cy + u * 0.25f)
                cubicTo(cx + u * 0.22f, cy - u * 0.05f, cx + u * 0.12f, cy - u * 0.38f, cx + u * 0.05f, cy - u * 0.68f)
                close()
            }
            drawPath(overalls, color = Color(0xFF6B9AC4))

            val strap = Path().apply {
                moveTo(cx + u * 0.08f, cy - u * 0.60f)
                quadraticTo(cx - u * 0.15f, cy - u * 0.40f, cx - u * 0.28f, cy - u * 0.15f)
            }
            drawPath(strap, color = Color(0xFF5A85AE), style = Stroke(width = strokeW * 1.4f, cap = StrokeCap.Round))
            drawPath(strap, color = Color(0xFF324D68), style = Stroke(width = strokeW * 0.45f, cap = StrokeCap.Round))
            drawCircle(color = Color(0xFFFFD152), radius = u * 0.026f, center = Offset(cx + u * 0.08f, cy - u * 0.58f))
            drawPath(
                overalls,
                color = Color(0xFF324D68),
                style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
        "body_royal_cape" -> {
            val capePath = Path().apply {
                moveTo(cx - u * 0.28f, cy - u * 0.66f)
                cubicTo(cx + u * 0.10f, cy - u * 0.74f, cx + u * 0.58f, cy - u * 0.60f, cx + u * 0.90f, cy - u * 0.26f)
                cubicTo(cx + u * 0.95f, cy + u * 0.08f, cx + u * 0.78f, cy + u * 0.34f, cx + u * 0.46f, cy + u * 0.32f)
                cubicTo(cx + u * 0.16f, cy + u * 0.18f, cx - u * 0.10f, cy - u * 0.12f, cx - u * 0.22f, cy - u * 0.42f)
                close()
            }
            drawPath(capePath, color = Color(0xFF9E2A2B))
            drawPath(
                capePath,
                color = Color(0xFFFFD700),
                style = Stroke(width = strokeW * 0.9f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            val collar = Path().apply {
                moveTo(cx - u * 0.35f, cy - u * 0.42f)
                quadraticTo(cx - u * 0.15f, cy - u * 0.58f, cx + u * 0.02f, cy - u * 0.50f)
                quadraticTo(cx - u * 0.10f, cy - u * 0.25f, cx - u * 0.35f, cy - u * 0.42f)
                close()
            }
            drawPath(collar, color = Color.White)
            drawPath(
                collar,
                color = Color(0xFF6B4247),
                style = Stroke(width = strokeW * 0.7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

internal fun DrawScope.drawSleepingNeckwear(
    neckwearId: String?,
    headCx: Float,
    headCy: Float,
    u: Float,
    strokeW: Float
) {
    if (neckwearId == null) return
    // Căn chuẩn vị trí rãnh cổ (neck groove) kết nối đầu nghiêng và ngực/sống lưng
    val nx = headCx + u * 0.34f
    val ny = headCy - u * 0.06f

    when (neckwearId) {
        WardrobeRegistry.ID_NECK_BELL_COLLAR, "neck_bell_collar" -> {
            val band = Path().apply {
                moveTo(nx - u * 0.20f, ny - u * 0.20f)
                quadraticTo(nx - u * 0.07f, ny - u * 0.09f, nx, ny)
                quadraticTo(nx - u * 0.01f, ny + u * 0.06f, nx - u * 0.06f, ny + u * 0.10f)
            }
            drawPath(band, color = Color(0xFFC84B31), style = Stroke(width = strokeW * 1.5f, cap = StrokeCap.Round))
            drawPath(band, color = Color(0xFF6B4247), style = Stroke(width = strokeW * 0.45f, cap = StrokeCap.Round))

            val bellCenter = Offset(nx, ny)
            drawCircle(color = Color(0xFFFFD700), radius = u * 0.038f, center = bellCenter)
            drawCircle(color = Color(0xFF6B4247), radius = u * 0.038f, center = bellCenter, style = Stroke(width = strokeW * 0.6f))
            drawCircle(color = Color(0xFFB8860B), radius = u * 0.012f, center = Offset(bellCenter.x, bellCenter.y + u * 0.012f))
        }
        WardrobeRegistry.ID_NECK_WARM_SCARF, "neck_warm_scarf" -> {
            val scarfBand = Path().apply {
                moveTo(nx - u * 0.21f, ny - u * 0.21f)
                quadraticTo(nx - u * 0.07f, ny - u * 0.10f, nx + u * 0.01f, ny)
                quadraticTo(nx, ny + u * 0.07f, nx - u * 0.06f, ny + u * 0.12f)
            }
            drawPath(scarfBand, color = Color(0xFFE07A5F), style = Stroke(width = strokeW * 2.4f, cap = StrokeCap.Round))
            drawPath(scarfBand, color = Color(0xFF6B4247), style = Stroke(width = strokeW * 0.55f, cap = StrokeCap.Round))

            val tailScarf = Path().apply {
                moveTo(nx + u * 0.01f, ny)
                lineTo(nx + u * 0.08f, ny + u * 0.10f)
                lineTo(nx + u * 0.13f, ny + u * 0.07f)
                lineTo(nx + u * 0.04f, ny - u * 0.04f)
                close()
            }
            drawPath(tailScarf, color = Color(0xFFE07A5F))
            drawPath(tailScarf, color = Color(0xFF6B4247), style = Stroke(width = strokeW * 0.65f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        "neck_blue_bow" -> {
            val band = Path().apply {
                moveTo(nx - u * 0.19f, ny - u * 0.18f)
                quadraticTo(nx - u * 0.07f, ny - u * 0.09f, nx, ny)
                quadraticTo(nx - u * 0.01f, ny + u * 0.06f, nx - u * 0.06f, ny + u * 0.10f)
            }
            drawPath(band, color = Color(0xFF76B9C4), style = Stroke(width = strokeW * 1.1f, cap = StrokeCap.Round))

            val bw = u * 0.09f
            val bh = u * 0.055f
            withTransform({
                rotate(35f, pivot = Offset(nx, ny))
            }) {
                listOf(-1f, 1f).forEach { side ->
                    val wing = Path().apply {
                        moveTo(nx, ny)
                        quadraticTo(nx + side * bw, ny - bh, nx + side * bw * 1.05f, ny)
                        quadraticTo(nx + side * bw, ny + bh, nx, ny)
                        close()
                    }
                    drawPath(wing, color = Color(0xFF76B9C4))
                    drawPath(wing, color = Color(0xFF6B4247), style = Stroke(width = strokeW * 0.65f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                }
                drawCircle(color = Color(0xFF569DAA), radius = bw * 0.28f, center = Offset(nx, ny))
                drawCircle(color = Color(0xFF6B4247), radius = bw * 0.28f, center = Offset(nx, ny), style = Stroke(width = strokeW * 0.55f))
            }
        }
        "neck_pearl" -> {
            val pearlPath = Path().apply {
                moveTo(nx - u * 0.18f, ny - u * 0.18f)
                quadraticTo(nx - u * 0.07f, ny - u * 0.09f, nx, ny)
                quadraticTo(nx - u * 0.01f, ny + u * 0.06f, nx - u * 0.07f, ny + u * 0.11f)
            }
            drawPath(pearlPath, color = Color(0xFFD6D2CB), style = Stroke(width = strokeW * 0.4f))
            listOf(
                Offset(nx - u * 0.18f, ny - u * 0.18f),
                Offset(nx - u * 0.11f, ny - u * 0.13f),
                Offset(nx - u * 0.04f, ny - u * 0.07f),
                Offset(nx + u * 0.01f, ny),
                Offset(nx - u * 0.02f, ny + u * 0.06f),
                Offset(nx - u * 0.07f, ny + u * 0.11f)
            ).forEach { beadCenter ->
                drawCircle(color = Color(0xFFFAF9F6), radius = u * 0.024f, center = beadCenter)
                drawCircle(color = Color(0xFF7E5657), radius = u * 0.024f, center = beadCenter, style = Stroke(width = strokeW * 0.45f))
                drawCircle(color = Color.White, radius = u * 0.008f, center = Offset(beadCenter.x - u * 0.006f, beadCenter.y - u * 0.006f))
            }
        }
        else -> {
            // ID phụ kiện cổ không xác định
        }
    }
}

internal fun DrawScope.drawSleepingHeadwear(
    headwearId: String?,
    headCx: Float,
    headCy: Float,
    headRadius: Float,
    u: Float,
    strokeW: Float
) {
    if (headwearId == null) return
    when (headwearId) {
        "head_ribbon" -> {
            val rx = headCx - headRadius * 0.62f
            val ry = headCy - headRadius * 0.82f
            val rw = u * 0.14f
            val rh = u * 0.09f
            listOf(-1f, 1f).forEach { side ->
                val wing = Path().apply {
                    moveTo(rx, ry)
                    quadraticTo(rx + side * rw, ry - rh, rx + side * rw * 1.05f, ry)
                    quadraticTo(rx + side * rw, ry + rh, rx, ry)
                    close()
                }
                drawPath(wing, color = Color(0xFFFF8FAB))
                drawPath(wing, color = Color(0xFF6B4247), style = Stroke(width = strokeW * 0.65f, cap = StrokeCap.Round, join = StrokeJoin.Round))
            }
            drawCircle(color = Color(0xFFFF6584), radius = rw * 0.24f, center = Offset(rx, ry))
            drawCircle(color = Color(0xFF6B4247), radius = rw * 0.24f, center = Offset(rx, ry), style = Stroke(width = strokeW * 0.55f))
        }
        "head_straw_hat" -> {
            val hx = headCx + headRadius * 0.05f
            val hy = headCy - headRadius * 0.88f
            withTransform({
                rotate(-18f, pivot = Offset(hx, hy))
            }) {
                val brimW = u * 0.60f
                val brimH = u * 0.13f
                drawOval(color = Color(0xFFEEDAA2), topLeft = Offset(hx - brimW / 2f, hy - brimH / 2f), size = Size(brimW, brimH))
                drawOval(color = Color(0xFF6B4247), topLeft = Offset(hx - brimW / 2f, hy - brimH / 2f), size = Size(brimW, brimH), style = Stroke(width = strokeW * 0.70f))

                val dome = Path().apply {
                    moveTo(hx - u * 0.20f, hy)
                    cubicTo(hx - u * 0.18f, hy - u * 0.24f, hx + u * 0.18f, hy - u * 0.24f, hx + u * 0.20f, hy)
                    close()
                }
                drawPath(dome, color = Color(0xFFEEDAA2))
                drawPath(dome, color = Color(0xFF6B4247), style = Stroke(width = strokeW * 0.70f, cap = StrokeCap.Round, join = StrokeJoin.Round))

                val ribbonPath = Path().apply {
                    moveTo(hx - u * 0.20f, hy - u * 0.02f)
                    quadraticTo(hx, hy + u * 0.02f, hx + u * 0.20f, hy - u * 0.02f)
                    lineTo(hx + u * 0.19f, hy - u * 0.07f)
                    quadraticTo(hx, hy - u * 0.03f, hx - u * 0.19f, hy - u * 0.07f)
                    close()
                }
                drawPath(ribbonPath, color = Color(0xFF8B5E3C))
            }
        }
        "head_beret" -> {
            val bx = headCx - headRadius * 0.10f
            val by = headCy - headRadius * 0.90f
            withTransform({
                rotate(-14f, pivot = Offset(bx, by))
            }) {
                val beretPath = Path().apply {
                    moveTo(bx - u * 0.26f, by + u * 0.06f)
                    cubicTo(bx - u * 0.28f, by - u * 0.18f, bx + u * 0.28f, by - u * 0.18f, bx + u * 0.26f, by + u * 0.06f)
                    cubicTo(bx + u * 0.15f, by + u * 0.09f, bx - u * 0.15f, by + u * 0.09f, bx - u * 0.26f, by + u * 0.06f)
                    close()
                }
                drawPath(beretPath, color = Color(0xFFE5B03B))
                drawPath(beretPath, color = Color(0xFF6B4247), style = Stroke(width = strokeW * 0.75f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawLine(
                    color = Color(0xFF6B4247),
                    start = Offset(bx, by - u * 0.16f),
                    end = Offset(bx, by - u * 0.22f),
                    strokeWidth = strokeW * 0.8f,
                    cap = StrokeCap.Round
                )
            }
        }
        "head_flower_crown" -> {
            val hy = headCy - headRadius * 0.82f
            listOf(-0.35f, -0.15f, 0.08f, 0.30f).forEach { fx ->
                val fc = Offset(headCx + headRadius * fx, hy - fx * u * 0.04f)
                val flowerR = u * 0.036f
                for (a in 0 until 6) {
                    val angle = a * (PI.toFloat() / 3f)
                    val petalX = fc.x + cos(angle) * (flowerR * 0.8f)
                    val petalY = fc.y + sin(angle) * (flowerR * 0.8f)
                    drawCircle(color = Color.White, radius = flowerR * 0.55f, center = Offset(petalX, petalY))
                    drawCircle(color = Color(0xFF6B4247), radius = flowerR * 0.55f, center = Offset(petalX, petalY), style = Stroke(width = strokeW * 0.35f))
                }
                drawCircle(color = Color(0xFFF9D162), radius = flowerR * 0.45f, center = fc)
            }
        }
        "head_crown" -> {
            val cxCenter = headCx + headRadius * 0.02f
            val cyTop = headCy - headRadius * 0.88f
            withTransform({
                rotate(10f, pivot = Offset(cxCenter, cyTop))
            }) {
                val crownPath = Path().apply {
                    moveTo(cxCenter - u * 0.15f, cyTop)
                    lineTo(cxCenter - u * 0.16f, cyTop - u * 0.15f)
                    lineTo(cxCenter - u * 0.08f, cyTop - u * 0.08f)
                    lineTo(cxCenter, cyTop - u * 0.18f)
                    lineTo(cxCenter + u * 0.08f, cyTop - u * 0.08f)
                    lineTo(cxCenter + u * 0.16f, cyTop - u * 0.15f)
                    lineTo(cxCenter + u * 0.15f, cyTop)
                    close()
                }
                drawPath(crownPath, color = Color(0xFFFFD700))
                drawPath(crownPath, color = Color(0xFF6B4247), style = Stroke(width = strokeW * 0.70f, cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawCircle(color = Color(0xFFE63946), radius = u * 0.022f, center = Offset(cxCenter, cyTop - u * 0.06f))
            }
        }
        else -> {
            // ID phụ kiện đầu không xác định
        }
    }
}

internal fun DrawScope.drawAccessoryOnChibiCat(accessory: PetAccessory, cx: Float, cy: Float, u: Float) {
    when (accessory) {
        PetAccessory.RIBBON -> {
            val rx = cx - u * 0.24f
            val ry = cy - u * 0.84f
            val rw = u * 0.15f
            val rh = u * 0.10f
            listOf(-1f, 1f).forEach { side ->
                val wing = Path().apply {
                    moveTo(rx, ry)
                    quadraticTo(rx + side * rw, ry - rh, rx + side * rw * 1.05f, ry)
                    quadraticTo(rx + side * rw, ry + rh, rx, ry)
                    close()
                }
                drawPath(wing, color = Color(0xFFFF8FAB))
                drawPath(wing, color = Rosewood, style = Stroke(width = 3f))
            }
            drawCircle(color = Rosewood, radius = rw * 0.24f, center = Offset(rx, ry))
        }
        PetAccessory.HAT -> {
            val hy = cy - u * 0.86f
            drawOval(color = Color(0xFFE6BE8A), topLeft = Offset(cx - u * 0.28f, hy - u * 0.05f), size = Size(u * 0.56f, u * 0.11f))
            drawOval(color = CatOutline, topLeft = Offset(cx - u * 0.28f, hy - u * 0.05f), size = Size(u * 0.56f, u * 0.11f), style = Stroke(width = 3f))
            val dome = Path().apply {
                moveTo(cx - u * 0.18f, hy)
                quadraticTo(cx, hy - u * 0.30f, cx + u * 0.18f, hy)
                close()
            }
            drawPath(dome, color = Color(0xFFE6BE8A))
            drawPath(dome, color = CatOutline, style = Stroke(width = 3f))
        }
        PetAccessory.COLLAR -> {
            val colY = cy - u * 0.14f
            drawOval(color = Rosewood, topLeft = Offset(cx - u * 0.24f, colY - u * 0.04f), size = Size(u * 0.48f, u * 0.08f))
            drawCircle(color = Color(0xFFFFD700), radius = u * 0.050f, center = Offset(cx, colY + u * 0.04f))
        }
        PetAccessory.SCARF -> {
            val sy = cy - u * 0.14f
            drawOval(color = Color(0xFFE07A5F), topLeft = Offset(cx - u * 0.26f, sy - u * 0.05f), size = Size(u * 0.52f, u * 0.12f))
            drawOval(color = CatOutline, topLeft = Offset(cx - u * 0.26f, sy - u * 0.05f), size = Size(u * 0.52f, u * 0.12f), style = Stroke(width = 3f))
        }
        PetAccessory.BOW_TIE -> {
            val by = cy - u * 0.10f
            val bw = u * 0.13f
            val bh = u * 0.08f
            listOf(-1f, 1f).forEach { side ->
                val wing = Path().apply {
                    moveTo(cx, by)
                    quadraticTo(cx + side * bw, by - bh, cx + side * bw * 1.05f, by)
                    quadraticTo(cx + side * bw, by + bh, cx, by)
                    close()
                }
                drawPath(wing, color = Color(0xFF88C9D4))
                drawPath(wing, color = CatOutline, style = Stroke(width = 2.5f))
            }
            drawCircle(color = Color(0xFF569DAA), radius = bw * 0.22f, center = Offset(cx, by))
        }
    }
}
