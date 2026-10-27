package com.littlebuddy.pet.pet

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.littlebuddy.pet.ui.theme.BlushPetal
import com.littlebuddy.pet.ui.theme.Rosewood
import com.littlebuddy.pet.ui.theme.SoftWhite
import com.littlebuddy.pet.ui.theme.StarColor

internal fun DrawScope.drawMoodEffects(
    centerX: Float,
    centerY: Float,
    radius: Float,
    mood: PetMood,
    progress: Float
) {
    when (mood) {
        PetMood.HAPPY -> {
            val heart1 = Offset(centerX + radius * 0.55f, centerY - radius * 0.65f - (progress * radius * 0.25f))
            drawHeart(heart1, radius * 0.18f, Rosewood)
            val heart2 = Offset(centerX - radius * 0.55f, centerY - radius * 0.45f - (progress * radius * 0.22f))
            drawHeart(heart2, radius * 0.14f, BlushPetal)
        }
        PetMood.EATING -> {
            val heart = Offset(centerX + radius * 0.55f, centerY - radius * 0.65f - (progress * radius * 0.25f))
            drawHeart(heart, radius * 0.18f, Rosewood)
        }
        PetMood.PLAYFUL -> {
            val star1 = Offset(centerX - radius * 0.55f, centerY - radius * 0.65f - (progress * radius * 0.22f))
            drawStar(star1, radius * 0.20f)
            val star2 = Offset(centerX + radius * 0.58f, centerY - radius * 0.45f - (progress * radius * 0.18f))
            drawStar(star2, radius * 0.14f)
        }
        PetMood.SLEEPY -> {
            val bubbleOffset = progress * radius * 0.25f
            listOf(
                Triple(radius * 0.30f, radius * 0.25f, radius * 0.06f),
                Triple(radius * 0.45f, radius * 0.50f, radius * 0.09f),
                Triple(radius * 0.60f, radius * 0.78f, radius * 0.13f)
            ).forEach { (ox, oy, r) ->
                drawCircle(color = SoftWhite.copy(alpha = 0.85f), radius = r, center = Offset(centerX + ox, centerY - oy - bubbleOffset))
                drawCircle(color = CatOutline.copy(alpha = 0.45f), radius = r, center = Offset(centerX + ox, centerY - oy - bubbleOffset), style = Stroke(width = 2f))
            }
        }
        else -> Unit
    }
}

internal fun DrawScope.drawHeart(center: Offset, size: Float, color: Color) {
    val heartPath = Path().apply {
        moveTo(center.x, center.y + size * 0.35f)
        cubicTo(center.x - size * 0.8f, center.y - size * 0.5f, center.x - size * 0.6f, center.y - size * 0.9f, center.x, center.y - size * 0.4f)
        cubicTo(center.x + size * 0.6f, center.y - size * 0.9f, center.x + size * 0.8f, center.y - size * 0.5f, center.x, center.y + size * 0.35f)
        close()
    }
    drawPath(heartPath, color = color)
}

internal fun DrawScope.drawStar(center: Offset, size: Float) {
    val starPath = Path().apply {
        moveTo(center.x, center.y - size)
        quadraticTo(center.x, center.y, center.x + size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y + size)
        quadraticTo(center.x, center.y, center.x - size, center.y)
        quadraticTo(center.x, center.y, center.x, center.y - size)
        close()
    }
    drawPath(starPath, color = StarColor)
}
