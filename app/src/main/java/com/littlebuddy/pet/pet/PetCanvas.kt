package com.littlebuddy.pet.pet

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import kotlin.math.min

/**
 * Composable vẽ nhân vật mèo Little Buddy trên Jetpack Compose Canvas.
 *
 * TỐI ƯU HIỆU NĂNG:
 * Sử dụng CatPose và CatAnimator kết hợp rememberCatClock:
 * Toàn bộ việc tính toán và đọc giá trị hoạt ảnh đa khớp diễn ra bên trong khối Canvas { } (Draw Phase).
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
    wardrobeOverride: EquippedWardrobe? = null,
    lookAt: Offset? = null,
    actionProgress: Float? = null
) {
    val isIdleOrSleeping = actionState == PetActionState.IDLE || actionState == PetActionState.SLEEPING
    val clock = rememberCatClock(
        active = true,
        minDtNanos = if (isIdleOrSleeping) 33_000_000L else 0L
    )
    val animator = remember { CatAnimator() }

    Canvas(modifier = modifier) {
        val nowNanos = System.nanoTime()
        val pose = animator.pose(
            action = actionState,
            t = clock.floatValue,
            nowNanos = nowNanos,
            lookAt = lookAt,
            actionProgress = actionProgress
        )

        // Đọc giá trị animation trong Draw phase (DrawScope)
        val curBounceOffsetY = animState?.bounceOffsetY?.invoke() ?: bounceOffsetY
        val curEyeScaleY = animState?.eyeScaleY?.invoke() ?: eyeScaleY
        val curChewOffset = animState?.chewOffset?.invoke() ?: chewOffset
        val curEffectProgress = animState?.effectProgress?.invoke() ?: effectProgress
        val curPurrVibrationY = animState?.purrVibrationY?.invoke() ?: purrVibrationY
        val curSleepingBreathScale = animState?.sleepingBreathScale?.invoke() ?: pose.bodyScaleY
        val curTailWagAngle = if (animState != null) animState.tailWagAngle() else pose.tailAngle

        val width = size.width
        val height = size.height
        val centerX = width / 2f
        val centerY = (height * 0.51f) + curBounceOffsetY + curPurrVibrationY
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
            // TƯ THẾ ĐỨNG THẲNG MEERKAT CHUẨN MẪU (HỆ THỐNG ĐA KHỚP CatPose)
            // ══════════════════════════════════════════════════════════════════
            withTransform({
                scale(scaleX = facingDirection, scaleY = 1f, pivot = Offset(centerX, centerY))
            }) {
                drawStandingChibiCat(
                    cx = centerX,
                    cy = centerY,
                    u = u,
                    baseU = u,
                    mood = petState.currentMood,
                    temporaryMood = petState.temporaryMood,
                    actionState = actionState,
                    pose = pose,
                    eyeScaleY = curEyeScaleY,
                    chewOffset = curChewOffset,
                    accessory = petState.accessory,
                    palette = palette,
                    wardrobe = activeWardrobe
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
 * Vẽ chú mèo đứng 2 chân dáng chuông ú tròn chuẩn ảnh mẫu kết hợp các khớp xương CatPose.
 */
private fun DrawScope.drawStandingChibiCat(
    cx: Float,
    cy: Float,
    u: Float,
    baseU: Float,
    mood: PetMood,
    temporaryMood: PetMood?,
    actionState: PetActionState,
    pose: CatPose,
    eyeScaleY: Float,
    chewOffset: Float,
    accessory: PetAccessory?,
    palette: CatColorPalette = WardrobeRegistry.FUR_SIAMESE.palette!!,
    wardrobe: EquippedWardrobe = EquippedWardrobe()
) {
    val strokeW = (baseU * 0.038f).coerceAtLeast(4.8f)

    // ── BƯỚC 1: Bóng đổ elip dưới sàn ──
    val shadowW = if (pose.pawReach > 0.1f) baseU * 1.50f else baseU * 1.35f
    drawOval(
        color = FloorShadowColor,
        topLeft = Offset(cx - shadowW / 2f, cy + baseU * 1.04f),
        size = Size(shadowW, baseU * 0.16f)
    )

    // ── BƯỚC 2: Đuôi mèo xoay quanh gốc đuôi (cx + 0.50u, cy + 0.68u) ──
    withTransform({
        rotate(pose.tailAngle, pivot = Offset(cx + u * 0.50f, cy + u * 0.68f))
    }) {
        drawCuteCatTail(cx, cy, u, tailWag = 0f, strokeW = strokeW, palette = palette)
    }

    // ── BƯỚC 3: Hai tai mèo vòm tròn xoay quanh gốc tai ──
    drawCuteCatEars(
        cx = cx,
        cy = cy,
        u = u,
        earWiggle = 0f,
        strokeW = strokeW,
        palette = palette,
        earL = pose.earL,
        earR = pose.earR
    )

    // ── BƯỚC 4: Thân hình dáng chuông ú mập (bọc trong withTransform: co giãn quanh chân + nhấp nhô) ──
    withTransform({
        scale(pose.bodyScaleX, pose.bodyScaleY, pivot = Offset(cx, cy + u))
        translate(0f, pose.bodyBob * u)
    }) {
        val bodyFillPath = createStandingBodyFillPath(cx, cy, u, 1f, 1f)

        clipPath(bodyFillPath) {
            // Nền lông theo Skin được chọn
            drawPath(bodyFillPath, color = palette.bodyColor)

            // Các mảng màu đặc trưng tam thể (Calico) trên thân
            if (wardrobe.furSkinId == WardrobeRegistry.ID_FUR_CALICO) {
                drawCalicoBodyPatches(cx, cy, u, 1f, 1f, palette)
            }

            // Bóng mềm ở đáy bụng
            val bottomShade = Path().apply {
                moveTo(cx - u * 0.65f, cy + u * 0.82f)
                quadraticTo(cx, cy + u * 1.05f, cx + u * 0.65f, cy + u * 0.82f)
                lineTo(cx + u * 0.65f, cy + u * 1.15f)
                lineTo(cx - u * 0.65f, cy + u * 1.15f)
                close()
            }
            drawPath(bottomShade, color = palette.bellyColor.copy(alpha = 0.55f))
        }

        // Viền thân màu viền theo Skin
        drawStandingBodyOutline(cx, cy, u, strokeW, palette.outlineColor, 1f, 1f)

        // Trang phục thân người (Bodywear: Áo len, yếm...)
        drawBodywearOnChibiCat(wardrobe.getBodywear()?.id, cx, cy, u, strokeW, 1f, 1f)
    }

    // ── BƯỚC 5: Hai chân sau ngắn đỡ ở đáy sàn (nhấc chân theo hindLift) ──
    drawShortHindFeet(
        cx = cx,
        cy = cy,
        u = u,
        strokeW = strokeW,
        walkCycle = 0f,
        isWalking = actionState == PetActionState.WALKING,
        palette = palette,
        hindLiftL = pose.hindLiftL,
        hindLiftR = pose.hindLiftR
    )

    // ── BƯỚC 6: Hai bàn tay trước co tròn hoặc vươn/vồ/nhấc trước ngực ──
    drawCurledFrontPaws(
        cx = cx,
        cy = cy,
        u = u,
        strokeW = strokeW,
        palette = palette,
        liftL = pose.pawLiftL,
        liftR = pose.pawLiftR,
        reach = pose.pawReach
    )

    // ── BƯỚC 7: Đầu và khuôn mặt (Cúi nhịp headDip và nghiêng headTilt quanh cổ) ──
    withTransform({
        translate(0f, pose.headDip * u)
        rotate(pose.headTilt, pivot = Offset(cx, cy - u * 0.25f))
    }) {
        // Khuôn mặt ngây thơ chuẩn mẫu (Mặt nạ, mắt bean, mũi, miệng ^)
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

        // Vòng cổ & Khăn quàng (Neckwear)
        drawNeckwearOnChibiCat(wardrobe.getNeckwear()?.id, cx, cy, u, strokeW)

        // Nơ & Mũ cài đầu (Headwear)
        drawHeadwearOnChibiCat(wardrobe.getHeadwear()?.id, cx, cy, u, strokeW)

        // Phụ kiện legacy nếu chưa có trang phục trong wardrobe
        if (wardrobe.getHeadwear() == null && wardrobe.getNeckwear() == null && wardrobe.getBodywear() == null) {
            accessory?.let { acc ->
                drawAccessoryOnChibiCat(acc, cx, cy, u)
            }
        }
    }
}
