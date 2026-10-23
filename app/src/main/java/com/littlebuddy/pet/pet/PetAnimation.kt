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
