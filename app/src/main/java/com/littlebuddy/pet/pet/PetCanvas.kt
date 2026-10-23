package com.littlebuddy.pet.pet

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.withTransform
import com.littlebuddy.pet.wardrobe.CatColorPalette
import com.littlebuddy.pet.wardrobe.EquippedWardrobe
import com.littlebuddy.pet.wardrobe.WardrobeRegistry
import kotlin.math.PI
import kotlin.math.min
import kotlin.math.sin

/**
 * Composable vẽ nhân vật mèo Little Buddy trên Jetpack Compose Canvas.
 *
 * TỐI ƯU HIỆU NĂNG:
 * Khi truyền [animState], toàn bộ việc đọc giá trị hoạt ảnh diễn ra bên trong khối Canvas { } (Draw Phase).
 * Nhờ đó, việc cập nhật chuyển động liên tục ở 60 FPS không gây Recomposition cho màn hình chứa nó.
 */
@Composable
fun PetCanvas(
    petState: PetState,
    modifier: Modifier = Modifier,
    actionState: PetActionState = PetActionState.IDLE,
    animState: PetAnimationState? = null,
    // Giá trị dự phòng khi không truyền animState
    bounceOffsetY: Float = 0f,
    breatheScale: Float = 1f,
    eyeScaleY: Float = 1f,
    earWiggleAngle: Float = 0f,
    tailWagAngle: Float = 0f,
    chewOffset: Float = 0f,
    effectProgress: Float = 0f,
    walkCycle: Float = 0f,
    walkSwayAngle: Float = 0f,
    purrVibrationY: Float = 0f,
    stretchProgress: Float = 0f,
    sleepingBreathScale: Float = 1f,
    facingDirection: Float = 1f,
    wardrobeOverride: EquippedWardrobe? = null
) {
    Canvas(modifier = modifier) {
        // Đọc giá trị animation trong Draw phase (DrawScope)
        val curBounceOffsetY = animState?.bounceOffsetY?.invoke() ?: bounceOffsetY
        val curBreatheScale = animState?.breatheScale?.invoke() ?: breatheScale
        val curEyeScaleY = animState?.eyeScaleY?.invoke() ?: eyeScaleY
        val curEarWiggleAngle = animState?.earWiggleAngle?.invoke() ?: earWiggleAngle
        val curTailWagAngle = animState?.tailWagAngle?.invoke() ?: tailWagAngle
        val curChewOffset = animState?.chewOffset?.invoke() ?: chewOffset
        val curEffectProgress = animState?.effectProgress?.invoke() ?: effectProgress
        val curWalkCycle = animState?.walkCycle?.invoke() ?: walkCycle
        val curWalkSwayAngle = animState?.walkSwayAngle?.invoke() ?: walkSwayAngle
        val curPurrVibrationY = animState?.purrVibrationY?.invoke() ?: purrVibrationY
        val curStretchProgress = animState?.stretchProgress?.invoke() ?: stretchProgress
        val curSleepingBreathScale = animState?.sleepingBreathScale?.invoke() ?: sleepingBreathScale

        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val isWalking = actionState == PetActionState.WALKING

        // Nhấp nhô bước chân khi đi bộ
        val walkBob = if (isWalking) sin(curWalkCycle * 4f * PI.toFloat()) * 4.5f else 0f
        val centerY = (height * 0.51f) + curBounceOffsetY + walkBob + curPurrVibrationY
        val u = min(width, height) * 0.40f

        val activeWardrobe = wardrobeOverride ?: petState.wardrobe
        val palette = activeWardrobe.getFurSkin().palette
            ?: WardrobeRegistry.FUR_SIAMESE.palette!!

        if (actionState == PetActionState.SLEEPING) {
            // ══════════════════════════════════════════════════════════════════
            // TƯ THẾ NẰM CUỘN TRÒN NGỦ TRÊN ĐỆM (100% VECTOR CANVAS)
            // ══════════════════════════════════════════════════════════════════
            drawSleepingChibiCat(
                cx = centerX,
                cy = centerY + u * 0.08f,
                u = u,
                baseU = min(width, height) * 0.40f,
                tailWag = curTailWagAngle,
                effectProgress = curEffectProgress,
                sleepingBreathScale = curSleepingBreathScale,
                palette = palette,
                wardrobe = activeWardrobe,
                legacyAccessory = petState.accessory
            )
        } else {
            // ══════════════════════════════════════════════════════════════════
            // TƯ THẾ ĐỨNG THẲNG MEERKAT CHUẨN MẪU
            // ══════════════════════════════════════════════════════════════════
            withTransform({
                scale(scaleX = facingDirection, scaleY = 1f, pivot = Offset(centerX, centerY))
                if (isWalking) {
                    rotate(degrees = curWalkSwayAngle, pivot = Offset(centerX, centerY + u * 0.8f))
                }
            }) {
                drawStandingChibiCat(
                    cx = centerX,
                    cy = centerY,
                    u = u * curBreatheScale,
                    baseU = u,
                    mood = petState.currentMood,
                    temporaryMood = petState.temporaryMood,
                    actionState = actionState,
                    eyeScaleY = curEyeScaleY,
                    earWiggle = curEarWiggleAngle,
                    tailWag = curTailWagAngle,
                    chewOffset = curChewOffset,
                    walkCycle = curWalkCycle,
                    stretchProgress = curStretchProgress,
                    accessory = petState.accessory,
                    palette = palette,
                    wardrobe = activeWardrobe
                )
            }

            // Vẽ món ăn khi đang ăn
            if (actionState == PetActionState.EATING || petState.currentMood == PetMood.EATING) {
                drawEatingSnack(
                    centerX = centerX,
                    snackY = centerY - u * 0.15f + curChewOffset,
                    radius = u * 0.68f
                )
            }

            // Hiệu ứng hạt bay quanh (Trái tim, Sao)
            drawMoodEffects(
                centerX = centerX,
                centerY = centerY - u * 0.25f,
                radius = u * 0.85f,
                mood = if (actionState == PetActionState.PETTING) PetMood.HAPPY else petState.currentMood,
                progress = curEffectProgress
            )
        }
    }
}

/**
 * Vẽ chú mèo đứng 2 chân dáng chuông ú tròn chuẩn ảnh mẫu.
 */
private fun DrawScope.drawStandingChibiCat(
    cx: Float,
    cy: Float,
    u: Float,
    baseU: Float,
    mood: PetMood,
    temporaryMood: PetMood?,
    actionState: PetActionState,
    eyeScaleY: Float,
    earWiggle: Float,
    tailWag: Float,
    chewOffset: Float,
    walkCycle: Float,
    stretchProgress: Float,
    accessory: PetAccessory?,
    palette: CatColorPalette = WardrobeRegistry.FUR_SIAMESE.palette!!,
    wardrobe: EquippedWardrobe = EquippedWardrobe()
) {
    val strokeW = (baseU * 0.038f).coerceAtLeast(4.8f)
    val isStretching = stretchProgress > 0.01f

    // ── BƯỚC 1: Bóng đổ elip dưới sàn ──
    val shadowW = if (isStretching) baseU * 1.50f else baseU * 1.35f
    drawOval(
        color = FloorShadowColor,
        topLeft = Offset(cx - shadowW / 2f, cy + baseU * 1.04f),
        size = Size(shadowW, baseU * 0.16f)
    )

    // ── BƯỚC 2: Đuôi mèo ngúng nguẩy sau hông phải ──
    drawCuteCatTail(cx, cy, u, tailWag, strokeW, palette)

    // ── BƯỚC 3: Hai tai mèo vòm tròn ──
    drawCuteCatEars(cx, cy, u, earWiggle, strokeW, palette)

    // ── BƯỚC 4: Thân hình dáng chuông ú mập, không thắt eo ──
    val stretchScaleX = 1f + stretchProgress * 0.10f
    val stretchScaleY = 1f - stretchProgress * 0.08f
    val bodyFillPath = createStandingBodyFillPath(cx, cy, u, stretchScaleX, stretchScaleY)

    clipPath(bodyFillPath) {
        // Nền lông theo Skin được chọn
        drawPath(bodyFillPath, color = palette.bodyColor)

        // Bóng mềm ở đáy bụng
        val bottomShade = Path().apply {
            moveTo(cx - u * 0.65f * stretchScaleX, cy + u * 0.82f)
            quadraticTo(cx, cy + u * 1.05f, cx + u * 0.65f * stretchScaleX, cy + u * 0.82f)
            lineTo(cx + u * 0.65f * stretchScaleX, cy + u * 1.15f)
            lineTo(cx - u * 0.65f * stretchScaleX, cy + u * 1.15f)
            close()
        }
        drawPath(bottomShade, color = palette.bellyColor.copy(alpha = 0.55f))
    }

    // Viền thân màu viền theo Skin
    drawStandingBodyOutline(cx, cy, u, strokeW, palette.outlineColor, stretchScaleX, stretchScaleY)

    // ── BƯỚC 4: Hai chân sau ngắn đỡ ở đáy sàn ──
    drawShortHindFeet(cx, cy, u, strokeW, walkCycle, actionState == PetActionState.WALKING, palette)

    // ── BƯỚC 5: Trang phục thân người (Bodywear: Áo len, yếm...) ──
    drawBodywearOnChibiCat(wardrobe.getBodywear()?.id, cx, cy, u, strokeW, stretchScaleX, stretchScaleY)

    // ── BƯỚC 6: Hai bàn tay trước co tròn trước ngực ──
    drawCurledFrontPaws(cx, cy, u, strokeW, palette)

    // ── BƯỚC 7: Khuôn mặt ngây thơ chuẩn mẫu (Mặt nạ, mắt bean, mũi, miệng ^) ──
    drawChibiFace(
        cx = cx,
        cy = cy,
        u = u,
        mood = mood,
        temporaryMood = temporaryMood,
        actionState = actionState,
        eyeScaleY = eyeScaleY,
        chewOffset = chewOffset,
        strokeW = strokeW,
        palette = palette
    )

    // ── BƯỚC 8: Vòng cổ & Khăn quàng (Neckwear) ──
    drawNeckwearOnChibiCat(wardrobe.getNeckwear()?.id, cx, cy, u, strokeW)

    // ── BƯỚC 9: Nơ & Mũ cài đầu (Headwear) ──
    drawHeadwearOnChibiCat(wardrobe.getHeadwear()?.id, cx, cy, u, strokeW)

    // Phụ kiện legacy nếu chưa có trang phục trong wardrobe
    if (wardrobe.getHeadwear() == null && wardrobe.getNeckwear() == null && wardrobe.getBodywear() == null) {
        accessory?.let { acc ->
            drawAccessoryOnChibiCat(acc, cx, cy, u)
        }
    }
}
