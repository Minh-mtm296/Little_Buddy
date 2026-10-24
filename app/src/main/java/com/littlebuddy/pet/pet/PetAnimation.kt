package com.littlebuddy.pet.pet

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.geometry.Offset
import kotlin.math.abs
import kotlin.math.max
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

/**
 * Controller chứa các lambda cung cấp giá trị hoạt ảnh thời gian thực (Procedural Animation State).
 *
 * TỐI ƯU HIỆU NĂNG COMPOSE (Draw-Phase State Reading):
 * Bằng cách truyền lambda () -> Float thay vì giá trị Float thuần túy, Compose KHÔNG cần phải
 * Recompose toàn bộ cây giao diện (PetHomeScreen, phòng, các nút bấm, v.v.) mỗi 16ms (60fps).
 * Giá trị State chỉ được đọc bên trong DrawScope của Canvas { }, đảm bảo chỉ khối vẽ (Draw Phase)
 * được thực thi lại, tiết kiệm CPU và pin tối đa cho thiết bị di động.
 */
class PetAnimationState(
    val bounceOffsetY: () -> Float,
    val breatheScale: () -> Float,
    val eyeScaleY: () -> Float,
    val earWiggleAngle: () -> Float,
    val tailWagAngle: () -> Float,
    val chewOffset: () -> Float,
    val effectProgress: () -> Float,
    val walkCycle: () -> Float,
    val walkSwayAngle: () -> Float,
    val purrVibrationY: () -> Float,
    val stretchProgress: () -> Float,
    val sleepingBreathScale: () -> Float
)

/**
 * Quản lý và điều phối toàn bộ hệ thống Hoạt họa Đa Khớp Xương (Procedural Animation Controller).
 * Chỉ chạy các vòng lặp hoạt ảnh cần thiết khi nhân vật ở đúng trạng thái tương ứng.
 */
@Composable
fun rememberPetAnimationState(
    petState: PetState,
    actionState: PetActionState = PetActionState.IDLE
): PetAnimationState {
    val infiniteTransition = rememberInfiniteTransition(label = "petInfinite")
    val isSleeping = actionState == PetActionState.SLEEPING || petState.isSleeping
    val isWalking = actionState == PetActionState.WALKING
    val isPetting = actionState == PetActionState.PETTING
    val isStretching = actionState == PetActionState.STRETCHING
    val isEating = actionState == PetActionState.EATING || petState.temporaryMood == PetMood.EATING

    // ── 1. HƠI THỞ TỰ NHIÊN (IDLE BREATHING) ──
    val breatheScaleState = infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    // Hơi thở sâu khi ngủ (chỉ kích hoạt nhịp dài khi ngủ)
    val sleepingBreathScaleState = if (isSleeping) {
        infiniteTransition.animateFloat(
            initialValue = 0.96f,
            targetValue = 1.04f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "sleepBreathe"
        )
    } else null

    // ── 2. ĐUÔI NGOE NGUẨY (TAIL WAG) ──
    val tailSpeed = when {
        isSleeping -> 3200
        isWalking -> 650
        petState.currentMood == PetMood.PLAYFUL -> 500
        else -> 1300
    }
    val tailAmplitude = when {
        isSleeping -> 4f
        isWalking -> 15f
        petState.currentMood == PetMood.PLAYFUL -> 18f
        else -> 10f
    }
    val tailWagAngleState = infiniteTransition.animateFloat(
        initialValue = -tailAmplitude,
        targetValue = tailAmplitude,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = tailSpeed, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tailWag"
    )

    // ── 3. CHU KỲ BƯỚC ĐI BỘ (WALK CYCLE & SWAY - CHỈ CHẠY KHI ĐI BỘ) ──
    val walkCycleState = if (isWalking) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 650, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "walkCycle"
        )
    } else null

    // ── 4. RUNG RINH GẦM GỪ KHI VUỐT VE (PURR VIBRATION - CHỈ CHẠY KHI VUỐT VE) ──
    val purrWaveState = if (isPetting) {
        infiniteTransition.animateFloat(
            initialValue = -1.2f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 60, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "purrWave"
        )
    } else null

    // ── 5. VƯƠN VAI THỨC DẬY (STRETCHING) ──
    val stretchAnimatable = remember { Animatable(0f) }
    LaunchedEffect(isStretching) {
        if (isStretching) {
            stretchAnimatable.animateTo(1f, tween(800, easing = FastOutSlowInEasing))
            delay(400L)
            stretchAnimatable.animateTo(0f, tween(600, easing = FastOutSlowInEasing))
        } else {
            stretchAnimatable.snapTo(0f)
        }
    }

    // ── 6. HIỆU ỨNG HẠT BAY BỔNG (Trái tim, Sao, Zzz) ──
    val effectProgressState = infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "effectProgress"
    )

    // ── 7. CHỚP MẮT TỰ NHIÊN (NATURAL BLINK) ──
    var eyeBlinkScale by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(isSleeping) {
        if (!isSleeping) {
            while (true) {
                delay(3000L + (800..2600).random())
                eyeBlinkScale = 0.05f
                delay(120L)
                eyeBlinkScale = 1f
                if ((1..10).random() <= 3) {
                    delay(150L)
                    eyeBlinkScale = 0.05f
                    delay(100L)
                    eyeBlinkScale = 1f
                }
            }
        } else {
            eyeBlinkScale = 0f
        }
    }

    // ── 8. NHẢY TƯNG TƯNG KHI TƯƠNG TÁC (HOP / BOUNCE) ──
    val hopOffsetY = remember { Animatable(0f) }
    LaunchedEffect(petState.lastInteractionTimestamp) {
        if (!isSleeping && !isWalking) {
            when (petState.temporaryMood) {
                PetMood.HAPPY, PetMood.PLAYFUL -> {
                    repeat(2) {
                        hopOffsetY.animateTo(-24f, tween(150, easing = FastOutSlowInEasing))
                        hopOffsetY.animateTo(0f, tween(150, easing = FastOutSlowInEasing))
                    }
                }
                PetMood.EATING -> {
                    hopOffsetY.animateTo(-10f, tween(120, easing = FastOutSlowInEasing))
                    hopOffsetY.animateTo(0f, tween(120, easing = FastOutSlowInEasing))
                }
                else -> Unit
            }
        }
    }

    // ── 9. NHAI NHÓP NHÉP (CHEW OFFSET) ──
    val chewOffsetState = animateFloatAsState(
        targetValue = if (isEating) 6f else 0f,
        animationSpec = if (isEating) {
            infiniteRepeatable(
                animation = tween(200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(150)
        },
        label = "chew"
    )

    // ── 10. VẪY TAI NHẸ NHÀNG (EAR WIGGLE) ──
    var earWiggle by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(actionState) {
        if (actionState == PetActionState.PETTING || petState.currentMood == PetMood.PLAYFUL) {
            while (true) {
                earWiggle = 6f
                delay(200L)
                earWiggle = -6f
                delay(200L)
                earWiggle = 0f
                delay(1200L)
            }
        } else {
            earWiggle = 0f
        }
    }

    return remember(isSleeping, isWalking, isPetting) {
        PetAnimationState(
            bounceOffsetY = { hopOffsetY.value },
            breatheScale = { breatheScaleState.value },
            eyeScaleY = { if (isSleeping) 0f else eyeBlinkScale },
            earWiggleAngle = { earWiggle },
            tailWagAngle = { tailWagAngleState.value },
            chewOffset = { chewOffsetState.value },
            effectProgress = { effectProgressState.value },
            walkCycle = { walkCycleState?.value ?: 0f },
            walkSwayAngle = {
                walkCycleState?.let { sin(it.value * 2f * PI.toFloat()) * 3.5f } ?: 0f
            },
            purrVibrationY = { purrWaveState?.value ?: 0f },
            stretchProgress = { stretchAnimatable.value },
            sleepingBreathScale = { sleepingBreathScaleState?.value ?: 1f }
        )
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// HỆ THỐNG HOẠT HỌA ĐA KHỚP XƯƠNG (CatPose Procedural Animation Engine)
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Đồng hồ thời gian thực duy nhất dựa trên frame clock của Compose.
 * [minDtNanos]: Giới hạn frame rate (vd: ~33ms = 30fps khi rảnh/ngủ) để tiết kiệm pin.
 */
@Composable
fun rememberCatClock(active: Boolean, minDtNanos: Long = 0L): FloatState {
    val t = remember { mutableFloatStateOf(0f) }
    LaunchedEffect(active, minDtNanos) {
        if (!active) return@LaunchedEffect
        var last = withFrameNanos { it }
        while (true) {
            val now = withFrameNanos { it }
            if (now - last < minDtNanos) continue
            t.floatValue += (now - last) / 1_000_000_000f
            last = now
        }
    }
    return t
}

/**
 * Dữ liệu trạng thái tư thế hình học của mèo tại một thời điểm (Data-only struct).
 * Mọi thông số là số thực đã chuẩn hóa, không chứa logic hay state.
 */
data class CatPose(
    val bodyScaleX: Float = 1f,
    val bodyScaleY: Float = 1f,
    val bodyBob: Float = 0f,            // Đơn vị u, dương = nhấp nhô xuống
    val headTilt: Float = 0f,           // Góc nghiêng đầu (độ)
    val headDip: Float = 0f,            // Đơn vị u, dương = cúi gật đầu
    val earL: Float = 0f,
    val earR: Float = 0f,               // Góc vẫy tai trái/phải (độ)
    val tailAngle: Float = 0f,          // Góc vẫy đuôi quanh gốc (độ)
    val pawLiftL: Float = 0f,
    val pawLiftR: Float = 0f,           // Độ nhấc chân trước (0..1)
    val pawReach: Float = 0f,           // Độ vươn duỗi chân trước (0..1)
    val hindLiftL: Float = 0f,
    val hindLiftR: Float = 0f           // Độ nhấc chân sau (0..1)
)

/**
 * Nội suy tuyến tính (Linear Interpolation) giữa hai tư thế mèo.
 */
fun lerp(a: CatPose, b: CatPose, f: Float) = CatPose(
    bodyScaleX = a.bodyScaleX + (b.bodyScaleX - a.bodyScaleX) * f,
    bodyScaleY = a.bodyScaleY + (b.bodyScaleY - a.bodyScaleY) * f,
    bodyBob = a.bodyBob + (b.bodyBob - a.bodyBob) * f,
    headTilt = a.headTilt + (b.headTilt - a.headTilt) * f,
    headDip = a.headDip + (b.headDip - a.headDip) * f,
    earL = a.earL + (b.earL - a.earL) * f,
    earR = a.earR + (b.earR - a.earR) * f,
    tailAngle = a.tailAngle + (b.tailAngle - a.tailAngle) * f,
    pawLiftL = a.pawLiftL + (b.pawLiftL - a.pawLiftL) * f,
    pawLiftR = a.pawLiftR + (b.pawLiftR - a.pawLiftR) * f,
    pawReach = a.pawReach + (b.pawReach - a.pawReach) * f,
    hindLiftL = a.hindLiftL + (b.hindLiftL - a.hindLiftL) * f,
    hindLiftR = a.hindLiftR + (b.hindLiftR - a.hindLiftR) * f
)

/**
 * Bộ tạo cử động thời gian thực (Procedural Animation Evaluator).
 * Giữ trạng thái chuyển tiếp giữa các tư thế (250ms smooth transition), được gọi trong Draw Phase.
 */
class CatAnimator {
    private var lastAction: PetActionState? = null
    private var enteredAt = 0L
    private var from = CatPose()
    private var current = CatPose()
    private val tw = (2.0 * Math.PI).toFloat()

    fun pose(
        action: PetActionState,
        t: Float,
        nowNanos: Long,
        lookAt: Offset? = null,
        actionProgress: Float? = null
    ): CatPose {
        if (action != lastAction) {
            from = current
            enteredAt = nowNanos
            lastAction = action
        }
        val since = if (enteredAt == 0L) 0f else (nowNanos - enteredAt) / 1e9f
        val target = when (action) {
            PetActionState.WALKING -> walking(t)
            PetActionState.EATING -> eating(t)
            PetActionState.STRETCHING, PetActionState.WAKING_UP -> stretching(since)
            PetActionState.PLAYING -> playing(t, lookAt, actionProgress)
            else -> idle(t)
        }
        val k = (since / 0.25f).coerceIn(0f, 1f)
        current = lerp(from, target, k * k * (3f - 2f * k))   // Nội suy hermite smoothstep 250ms
        return current
    }

    private fun idle(t: Float): CatPose {
        val b = sin(tw * t / 3.2f)
        return CatPose(
            bodyScaleY = 1f + 0.015f * b,
            bodyScaleX = 1f - 0.008f * b,
            tailAngle = 10f * sin(tw * t / 2.6f),
            earL = 4f * max(0f, sin(tw * t / 7f))          // Thỉnh thoảng vẫy tai nhẹ
        )
    }

    private fun walking(t: Float): CatPose {
        val p = tw * t * 1.6f                               // Nhịp 1.6 bước/giây
        val a = sin(p)
        return CatPose(
            pawLiftL = max(0f, a),  pawLiftR = max(0f, -a), // Chân trước luân phiên
            hindLiftR = max(0f, a), hindLiftL = max(0f, -a),// Chéo góc: trước-trái cùng sau-phải
            bodyBob = 0.02f * abs(a),
            headTilt = -2f * a,
            tailAngle = 14f * sin(p * 0.5f)
        )
    }

    private fun eating(t: Float): CatPose {
        val bite = 0.5f + 0.5f * sin(tw * 1.8f * t)         // Nhịp gật đầu nhai 1.8 lần/giây
        return CatPose(
            headDip = 0.06f + 0.10f * bite,
            headTilt = 4f,
            bodyScaleY = 1f - 0.02f * bite,
            tailAngle = 6f * sin(tw * t / 2f)
        )
    }

    private fun stretching(since: Float): CatPose {
        val s = (since / 2.0f).coerceIn(0f, 1f)             // Chuỗi vươn vai 2 giây
        fun seg(a: Float, b: Float) = if (b == a) 0f else ((s - a) / (b - a)).coerceIn(0f, 1f)
        fun ease(x: Float) = x * x * (3f - 2f * x)
        val reach = ease(seg(0f, 0.35f)) * (1f - ease(seg(0.70f, 1f)))
        return CatPose(
            pawReach = reach,
            headDip = 0.14f * reach,
            bodyScaleX = 1f + 0.10f * reach,
            bodyScaleY = 1f - 0.10f * reach
        )
    }

    private fun playing(t: Float, lookAt: Offset?, actionProgress: Float?): CatPose {
        val lx = lookAt?.x ?: 0f
        val ly = lookAt?.y ?: 0f
        val headTilt = (lx * 12f).coerceIn(-12f, 12f)
        val headDip = (ly * 0.08f).coerceIn(-0.04f, 0.12f)
        val progress = actionProgress ?: (0.5f + 0.5f * sin(tw * 2f * t))
        val p = progress.coerceIn(0f, 1f)
        val pSwipe = sin(p * PI.toFloat())
        return CatPose(
            headTilt = headTilt,
            headDip = headDip,
            pawLiftR = pSwipe,
            pawReach = pSwipe * 0.5f,
            tailAngle = 16f * sin(tw * t * 2.5f),
            bodyScaleY = 1f - 0.03f * pSwipe
        )
    }
}
