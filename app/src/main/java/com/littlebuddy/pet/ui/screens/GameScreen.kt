package com.littlebuddy.pet.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.littlebuddy.pet.pet.PetActionState
import com.littlebuddy.pet.pet.PetCanvas
import com.littlebuddy.pet.pet.PetMood
import com.littlebuddy.pet.pet.PetState
import com.littlebuddy.pet.pet.rememberPetAnimationState
import com.littlebuddy.pet.ui.theme.BlushPetal
import com.littlebuddy.pet.ui.theme.MidnightLagoon
import com.littlebuddy.pet.ui.theme.Rosewood
import com.littlebuddy.pet.ui.theme.SageLeaf
import com.littlebuddy.pet.ui.theme.SoftWhite
import com.littlebuddy.pet.ui.theme.VanillaCream
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

// ═══════════════════════════════════════════════════════════════════
// BẢNG CHỌN TRÒ CHƠI
// ═══════════════════════════════════════════════════════════════════

/** Số lần bắt cần thiết để kết thúc 1 mini-game và hiện Victory Card */
private const val GAME_MAX_CATCH = 3

/**
 * Dialog chọn trò chơi — hiện 2 lựa chọn: Cuộn len & Lông vũ
 */
@Composable
fun GamePickerDialog(
    petState: PetState,
    onSelectYarn: () -> Unit,
    onSelectFeather: () -> Unit,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = tween(280),
        label = "gamePickerScale"
    )

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .scale(scale)
                .shadow(16.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(VanillaCream)
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🎮 Chơi cùng ${petState.petName}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MidnightLagoon,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Năng lượng: ${petState.energy.toInt()}%  •  Độ no: ${petState.hunger.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MidnightLagoon.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.height(18.dp))

                // Trò chơi A: Cuộn len
                GameCard(
                    emoji = "🧶",
                    title = "Đuổi bắt cuộn len",
                    description = "Chạm để lăn len nảy, ${petState.petName} sẽ nhún người vồ bắt!",
                    reward = "+25% Niềm vui  •  +15 EXP  •  -18% Năng lượng",
                    color = Color(0xFFF5E6D3),
                    accentColor = Rosewood,
                    onClick = onSelectYarn
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Trò chơi B: Lông vũ
                GameCard(
                    emoji = "🪶",
                    title = "Săn lông vũ đung đưa",
                    description = "Rê cần câu nhử lông vũ, ${petState.petName} bật nhảy trên không!",
                    reward = "+20% Niềm vui  •  +15 EXP  •  -15% Năng lượng",
                    color = Color(0xFFD8EBE4),
                    accentColor = SageLeaf,
                    onClick = onSelectFeather
                )
                Spacer(modifier = Modifier.height(18.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(BlushPetal.copy(alpha = 0.4f))
                        .clickable { onDismiss() }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Để sau",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MidnightLagoon.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun GameCard(
    emoji: String,
    title: String,
    description: String,
    reward: String,
    color: Color,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .background(color)
            .border(1.5.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MidnightLagoon
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MidnightLagoon.copy(alpha = 0.65f)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = reward,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = accentColor
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// BẢNG TỔNG KẾT CHIẾN THẮNG (VICTORY POPUP)
// ═══════════════════════════════════════════════════════════════════

@Composable
fun GameVictoryCard(
    petState: PetState,
    gameTitle: String,
    gameEmoji: String,
    happinessGain: Int,
    expGain: Int,
    energyCost: Int,
    onConfirm: () -> Unit
) {
    var star1Visible by remember { mutableStateOf(false) }
    var star2Visible by remember { mutableStateOf(false) }
    var star3Visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(120L)
        star1Visible = true
        delay(140L)
        star2Visible = true
        delay(140L)
        star3Visible = true
    }

    val s1 by animateFloatAsState(
        targetValue = if (star1Visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "star1"
    )
    val s2 by animateFloatAsState(
        targetValue = if (star2Visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "star2"
    )
    val s3 by animateFloatAsState(
        targetValue = if (star3Visible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "star3"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(VanillaCream)
            .border(2.dp, BlushPetal, RoundedCornerShape(26.dp))
            .padding(22.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 3 Sao vàng nảy lên lần lượt
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "⭐", fontSize = 34.sp, modifier = Modifier.scale(s1))
                Text(text = "⭐", fontSize = 44.sp, modifier = Modifier.scale(s2).offset(y = (-4).dp))
                Text(text = "⭐", fontSize = 34.sp, modifier = Modifier.scale(s3))
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "🎉 Bắt trúng xuất sắc!",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MidnightLagoon,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${petState.petName} vừa có những cú vồ mồi siêu đỉnh và đang cực kỳ hạnh phúc! ♡🐾",
                style = MaterialTheme.typography.bodyMedium,
                color = MidnightLagoon.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Thống kê phần thưởng
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SoftWhite)
                    .border(1.2.dp, BlushPetal, RoundedCornerShape(16.dp))
                    .padding(vertical = 12.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RewardBadge(emoji = "💖", value = "+$happinessGain%", label = "Niềm vui", color = Rosewood)
                Box(modifier = Modifier.width(1.dp).height(28.dp).background(BlushPetal))
                RewardBadge(emoji = "⭐", value = "+$expGain", label = "EXP", color = Color(0xFFE67E22))
                Box(modifier = Modifier.width(1.dp).height(28.dp).background(BlushPetal))
                RewardBadge(emoji = "⚡", value = "-$energyCost%", label = "Năng lượng", color = Color(0xFF5B8CA3))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Rosewood),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "🐾 Nhận thưởng & Về phòng",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = SoftWhite
                )
            }
        }
    }
}

@Composable
private fun RewardBadge(
    emoji: String,
    value: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(text = emoji, fontSize = 13.sp)
            Text(text = value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = color)
        }
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MidnightLagoon.copy(alpha = 0.55f))
    }
}

// ═══════════════════════════════════════════════════════════════════
// TRÒ CHƠI A: ĐUỔI BẮT CUỘN LEN (YARN CHASE)
// ═══════════════════════════════════════════════════════════════════

/**
 * Màn hình mini-game Đuổi bắt cuộn len.
 * Đồng bộ nhân vật Mèo Chibi Canvas thật của bé mèo (giữ Skin + phụ kiện mũ/nơ).
 * Cơ chế hoạt họa vồ bắt sống động:
 * 1. Nhún người lấy đà (Crouch / Squash)
 * 2. Phóng vọt parabol theo đường cong không trung (Leap Arch)
 * 3. Tiếp đất vồ trúng cuộn len nảy tưng, bung tỏa sao vàng & tim bay!
 */
@Composable
fun YarnChaseGame(
    petState: PetState,
    onFinish: (happinessGain: Float, energyCost: Float, hungerCost: Float) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var catchCount by remember { mutableIntStateOf(0) }
    val maxCatch = GAME_MAX_CATCH
    var showVictory by remember { mutableStateOf(false) }

    // Vị trí cuộn len
    var yarnX by remember { mutableFloatStateOf(240f) }
    var yarnY by remember { mutableFloatStateOf(160f) }
    val yarnRotation = remember { Animatable(0f) }
    val yarnTrail = remember { mutableStateListOf<Offset>() }

    // Trạng thái hoạt họa Mèo
    val catPosX = remember { Animatable(80f) }
    val catPosY = remember { Animatable(220f) }
    val catScaleX = remember { Animatable(1f) }
    val catScaleY = remember { Animatable(1f) }
    val catRotation = remember { Animatable(0f) }
    val jumpHeight = remember { Animatable(0f) }
    var facingDir by remember { mutableFloatStateOf(1f) }

    // Trạng thái vồ bắt
    var isPouncing by remember { mutableStateOf(false) }
    var isCatchSuccess by remember { mutableStateOf(false) }
    val burstProgress = remember { Animatable(0f) }

    val catBoxW = with(density) { 95.dp.toPx() }
    val catBoxH = with(density) { 110.dp.toPx() }

    // Hàm kích hoạt cú vồ bắt
    fun triggerPounce(targetX: Float, targetY: Float, arenaW: Float, arenaH: Float) {
        if (isPouncing || showVictory) return
        isPouncing = true
        isCatchSuccess = false

        coroutineScope.launch {
            // Cập nhật vệt chỉ len
            if (yarnTrail.size > 5) yarnTrail.removeAt(0)
            yarnTrail.add(Offset(targetX, targetY))

            // Xoay cuộn len
            launch {
                yarnRotation.animateTo(yarnRotation.value + 180f, tween(350, easing = FastOutSlowInEasing))
            }

            // Hướng mặt mèo quay về cuộn len
            facingDir = if (targetX >= catPosX.value) 1f else -1f

            // ── BƯỚC 1: Crouch (Nhún người lấy đà) ──
            catScaleY.animateTo(0.78f, tween(150, easing = FastOutSlowInEasing))
            catScaleX.animateTo(1.18f, tween(150, easing = FastOutSlowInEasing))

            // ── BƯỚC 2: Parabolic Leap (Phóng vọt parabol) ──
            val startX = catPosX.value
            val startY = catPosY.value
            val landX = (targetX - (facingDir * 32f)).coerceIn(40f, arenaW - 40f)
            val landY = targetY.coerceIn(50f, arenaH - 50f)
            val jumpTilt = facingDir * 12f

            launch {
                catScaleY.animateTo(1.18f, tween(230))
                catScaleY.animateTo(1.0f, tween(230))
            }
            launch {
                catScaleX.animateTo(0.86f, tween(230))
                catScaleX.animateTo(1.0f, tween(230))
            }
            launch {
                catRotation.animateTo(jumpTilt, tween(230))
                catRotation.animateTo(0f, tween(230))
            }
            launch {
                jumpHeight.animateTo(48f, tween(230, easing = LinearOutSlowInEasing))
                jumpHeight.animateTo(0f, tween(230, easing = FastOutSlowInEasing))
            }

            val steps = 18
            val stepDelay = 460L / steps
            for (i in 1..steps) {
                val progress = i.toFloat() / steps
                catPosX.snapTo(startX + (landX - startX) * progress)
                catPosY.snapTo(startY + (landY - startY) * progress)
                delay(stepDelay)
            }

            // ── BƯỚC 3: Land & Catch (Tiếp đất vồ trúng) ──
            isCatchSuccess = true
            catchCount++

            launch {
                burstProgress.snapTo(0f)
                burstProgress.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
            }

            catScaleY.animateTo(0.85f, tween(110))
            catScaleX.animateTo(1.15f, tween(110))
            catScaleY.animateTo(1.0f, tween(160))
            catScaleX.animateTo(1.0f, tween(160))

            delay(650L)
            isCatchSuccess = false
            isPouncing = false

            if (catchCount >= maxCatch) {
                delay(200L)
                showVictory = true
            } else {
                // Cuộn len tự động lăn nảy sang vị trí ngẫu nhiên mới
                val nextX = (arenaW * (0.18f + kotlin.random.Random.nextFloat() * 0.64f))
                val nextY = (arenaH * (0.22f + kotlin.random.Random.nextFloat() * 0.56f))
                yarnX = nextX
                yarnY = nextY
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .shadow(24.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFFFFF7ED))
        ) {
            Column {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "🧶", fontSize = 20.sp)
                        Text(
                            text = "Đuổi bắt cuộn len  •  $catchCount/$maxCatch",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MidnightLagoon
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(BlushPetal)
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Đóng", tint = Rosewood, modifier = Modifier.size(16.dp))
                    }
                }

                if (showVictory) {
                    GameVictoryCard(
                        petState = petState,
                        gameTitle = "Đuổi bắt cuộn len",
                        gameEmoji = "🧶",
                        happinessGain = 25,
                        expGain = 15,
                        energyCost = 18,
                        onConfirm = {
                            onFinish(25f, 18f, 12f)
                        }
                    )
                } else {
                    Text(
                        text = if (isCatchSuccess) "🎉 ${petState.petName} vồ trúng rồi! Meo~ ♡" else "Chạm vào sàn để lăn cuộn len, mèo sẽ nhún người vồ!",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCatchSuccess) Rosewood else MidnightLagoon.copy(alpha = 0.65f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Khu vực sàn chơi Canvas
                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(310.dp)
                            .padding(horizontal = 10.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFFF8EDE0))
                            .border(1.5.dp, Color(0xFFE4D2BF), RoundedCornerShape(22.dp))
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val tapX = offset.x.coerceIn(40f, size.width - 40f)
                                    val tapY = offset.y.coerceIn(40f, size.height - 40f)
                                    yarnX = tapX
                                    yarnY = tapY
                                    triggerPounce(tapX, tapY, size.width.toFloat(), size.height.toFloat())
                                }
                            }
                    ) {
                        val arenaW = constraints.maxWidth.toFloat()
                        val arenaH = constraints.maxHeight.toFloat()

                        // Khởi tạo vị trí ban đầu
                        LaunchedEffect(Unit) {
                            catPosX.snapTo(arenaW * 0.22f)
                            catPosY.snapTo(arenaH * 0.65f)
                            yarnX = arenaW * 0.72f
                            yarnY = arenaH * 0.38f
                        }

                        // Lớp vẽ Canvas: Thảm trải sàn + Vệt len + Bóng đổ + Cuộn len + Hạt sao
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // 1. Thảm trải sàn ấm áp có họa tiết viền
                            drawRoundRect(
                                color = Color(0xFFF3E3D1),
                                topLeft = Offset(10f, 10f),
                                size = Size(size.width - 20f, size.height - 20f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
                            )
                            drawRoundRect(
                                color = Color(0xFFE8D4BE),
                                topLeft = Offset(10f, 10f),
                                size = Size(size.width - 20f, size.height - 20f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f),
                                style = Stroke(width = 2f)
                            )

                            // 2. Vệt chỉ len uốn lượn phía sau
                            if (yarnTrail.size > 1) {
                                val trailPath = Path().apply {
                                    moveTo(yarnTrail.first().x, yarnTrail.first().y)
                                    for (i in 1 until yarnTrail.size) {
                                        val p0 = yarnTrail[i - 1]
                                        val p1 = yarnTrail[i]
                                        quadraticTo(p0.x, p0.y, (p0.x + p1.x) / 2f, (p0.y + p1.y) / 2f)
                                    }
                                    lineTo(yarnX, yarnY)
                                }
                                drawPath(
                                    trailPath,
                                    color = Color(0xFFE57373).copy(alpha = 0.5f),
                                    style = Stroke(width = 3.2f, cap = StrokeCap.Round)
                                )
                            }

                            // 3. Bóng đổ chân mèo trên sàn (co lại và nhạt đi khi mèo nhảy lên không trung)
                            val shadowScale = (1f - (jumpHeight.value / 85f)).coerceIn(0.55f, 1f)
                            val shadowAlpha = (0.28f - (jumpHeight.value / 180f)).coerceIn(0.08f, 0.28f)
                            drawOval(
                                color = Color(0x353B2F2F).copy(alpha = shadowAlpha),
                                topLeft = Offset(catPosX.value - (catBoxW * 0.36f * shadowScale), catPosY.value + (catBoxH * 0.38f)),
                                size = Size(catBoxW * 0.72f * shadowScale, catBoxH * 0.16f * shadowScale)
                            )

                            // 4. Cuộn len lăn tròn
                            drawYarnBall(yarnX, yarnY, 22f, yarnRotation.value)

                            // 5. Hiệu ứng sao vàng và tim nở bung khi vồ trúng
                            if (burstProgress.value > 0f && burstProgress.value < 1f) {
                                drawCatchBurst(Offset(yarnX, yarnY), burstProgress.value)
                            }
                        }

                        val yarnGameActionState = if (isPouncing) PetActionState.PLAYING else PetActionState.IDLE
                        val yarnGamePetState = if (isCatchSuccess) petState.copy(temporaryMood = PetMood.HAPPY) else petState
                        val yarnGameAnimState = rememberPetAnimationState(petState = yarnGamePetState, actionState = yarnGameActionState)

                        // Lớp vẽ bé Mèo Chibi Canvas thật (Đúng Skin + Phụ kiện Tủ đồ)
                        Box(
                            modifier = Modifier
                                .size(95.dp, 110.dp)
                                .offset {
                                    IntOffset(
                                        x = (catPosX.value - catBoxW / 2f).toInt(),
                                        y = (catPosY.value - catBoxH / 2f - jumpHeight.value).toInt()
                                    )
                                }
                                .graphicsLayer {
                                    scaleX = catScaleX.value * facingDir
                                    scaleY = catScaleY.value
                                    rotationZ = catRotation.value
                                }
                        ) {
                            PetCanvas(
                                petState = yarnGamePetState,
                                modifier = Modifier.fillMaxSize(),
                                actionState = yarnGameActionState,
                                animState = yarnGameAnimState,
                                facingDirection = 1f,
                                lookAt = Offset((yarnX - catPosX.value) * facingDir / 150f, (yarnY - catPosY.value) / 150f),
                                actionProgress = if (isPouncing) (jumpHeight.value / 60f).coerceIn(0f, 1f) else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// TRÒ CHƠI B: SĂN LÔNG VŨ (FEATHER HUNT)
// ═══════════════════════════════════════════════════════════════════

/**
 * Màn hình mini-game Săn lông vũ.
 * Cần câu que gỗ uốn cong với chùm lông vũ đung đưa vật lý (Pendulum sway).
 * Người chơi rê ngón tay nhử lông vũ, Mèo Chibi bật nhảy cao vồ bắt trên không trung!
 */
@Composable
fun FeatherHuntGame(
    petState: PetState,
    onFinish: (happinessGain: Float, energyCost: Float, hungerCost: Float) -> Unit,
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    var catchCount by remember { mutableIntStateOf(0) }
    val maxCatch = GAME_MAX_CATCH
    var showVictory by remember { mutableStateOf(false) }

    // Vị trí lông vũ và cần câu
    var featherX by remember { mutableFloatStateOf(220f) }
    var featherY by remember { mutableFloatStateOf(100f) }
    var wandTipX by remember { mutableFloatStateOf(260f) }
    var wandTipY by remember { mutableFloatStateOf(50f) }
    val featherSway = remember { Animatable(0f) }

    // Trạng thái hoạt họa Mèo
    val catPosX = remember { Animatable(100f) }
    val catPosY = remember { Animatable(210f) }
    val catScaleX = remember { Animatable(1f) }
    val catScaleY = remember { Animatable(1f) }
    val catRotation = remember { Animatable(0f) }
    val jumpHeight = remember { Animatable(0f) }
    var facingDir by remember { mutableFloatStateOf(1f) }

    var isJumping by remember { mutableStateOf(false) }
    var isCatchSuccess by remember { mutableStateOf(false) }
    val burstProgress = remember { Animatable(0f) }

    val catBoxW = with(density) { 95.dp.toPx() }
    val catBoxH = with(density) { 110.dp.toPx() }

    // Lắc lư lông vũ
    fun triggerSway() {
        coroutineScope.launch {
            featherSway.animateTo(14f, tween(180))
            featherSway.animateTo(-14f, tween(260))
            featherSway.animateTo(8f, tween(220))
            featherSway.animateTo(0f, tween(200))
        }
    }

    // Kích hoạt cú nhảy vồ lông vũ
    fun triggerHighJump(targetX: Float, targetY: Float, arenaW: Float, arenaH: Float) {
        if (isJumping || showVictory) return
        isJumping = true
        isCatchSuccess = false
        triggerSway()

        coroutineScope.launch {
            facingDir = if (targetX >= catPosX.value) 1f else -1f

            // 1. Nhún sâu lấy đà
            catScaleY.animateTo(0.74f, tween(160, easing = FastOutSlowInEasing))
            catScaleX.animateTo(1.22f, tween(160, easing = FastOutSlowInEasing))

            // 2. Bật nhảy cao lên không trung
            val startX = catPosX.value
            val startY = catPosY.value
            val landX = (targetX - (facingDir * 28f)).coerceIn(40f, arenaW - 40f)
            val landY = (targetY + 45f).coerceIn(60f, arenaH - 50f)
            val jumpTilt = facingDir * 15f

            launch {
                catScaleY.animateTo(1.22f, tween(220))
                catScaleY.animateTo(1.0f, tween(220))
            }
            launch {
                catScaleX.animateTo(0.84f, tween(220))
                catScaleX.animateTo(1.0f, tween(220))
            }
            launch {
                catRotation.animateTo(jumpTilt, tween(220))
                catRotation.animateTo(0f, tween(220))
            }
            launch {
                // Nhảy cao hơn hẳn cuộn len vì bắt trên không!
                jumpHeight.animateTo(65f, tween(220, easing = LinearOutSlowInEasing))
                jumpHeight.animateTo(0f, tween(220, easing = FastOutSlowInEasing))
            }

            val steps = 18
            val stepDelay = 440L / steps
            for (i in 1..steps) {
                val progress = i.toFloat() / steps
                catPosX.snapTo(startX + (landX - startX) * progress)
                catPosY.snapTo(startY + (landY - startY) * progress)
                delay(stepDelay)
            }

            // 3. Vồ trúng lông vũ
            isCatchSuccess = true
            catchCount++

            launch {
                burstProgress.snapTo(0f)
                burstProgress.animateTo(1f, tween(500, easing = FastOutSlowInEasing))
            }

            catScaleY.animateTo(0.86f, tween(110))
            catScaleX.animateTo(1.14f, tween(110))
            catScaleY.animateTo(1.0f, tween(160))
            catScaleX.animateTo(1.0f, tween(160))

            delay(650L)
            isCatchSuccess = false
            isJumping = false

            if (catchCount >= maxCatch) {
                delay(200L)
                showVictory = true
            } else {
                // Lông vũ bay sang vị trí ngẫu nhiên khác
                val nextX = (arenaW * (0.20f + kotlin.random.Random.nextFloat() * 0.60f))
                val nextY = (arenaH * (0.15f + kotlin.random.Random.nextFloat() * 0.40f))
                featherX = nextX
                featherY = nextY
                wandTipX = (nextX + 35f).coerceAtMost(arenaW - 10f)
                wandTipY = (nextY - 35f).coerceAtLeast(15f)
                triggerSway()
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .shadow(24.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFFF0FDF4))
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(text = "🪶", fontSize = 20.sp)
                        Text(
                            text = "Săn lông vũ đung đưa  •  $catchCount/$maxCatch",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MidnightLagoon
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFBFDDD8))
                            .clickable { onDismiss() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Đóng", tint = SageLeaf, modifier = Modifier.size(16.dp))
                    }
                }

                if (showVictory) {
                    GameVictoryCard(
                        petState = petState,
                        gameTitle = "Săn lông vũ đung đưa",
                        gameEmoji = "🪶",
                        happinessGain = 20,
                        expGain = 15,
                        energyCost = 15,
                        onConfirm = {
                            onFinish(20f, 15f, 10f)
                        }
                    )
                } else {
                    Text(
                        text = if (isCatchSuccess) "✨ ${petState.petName} chụp trúng lông vũ rồi! Giỏi quá~" else "Chạm hoặc rê ngón tay để nhử lông vũ, mèo sẽ bật nhảy vồ!",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCatchSuccess) SageLeaf else MidnightLagoon.copy(alpha = 0.65f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    BoxWithConstraints(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(310.dp)
                            .padding(horizontal = 10.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(Color(0xFFE2F0E8))
                            .border(1.5.dp, Color(0xFFC7E2D3), RoundedCornerShape(22.dp))
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    val tapX = offset.x.coerceIn(35f, size.width - 35f)
                                    val tapY = offset.y.coerceIn(30f, size.height * 0.65f)
                                    featherX = tapX
                                    featherY = tapY
                                    wandTipX = (tapX + 35f).coerceAtMost(size.width - 10f)
                                    wandTipY = (tapY - 35f).coerceAtLeast(15f)
                                    triggerHighJump(tapX, tapY, size.width.toFloat(), size.height.toFloat())
                                }
                            }
                    ) {
                        val arenaW = constraints.maxWidth.toFloat()
                        val arenaH = constraints.maxHeight.toFloat()

                        LaunchedEffect(Unit) {
                            catPosX.snapTo(arenaW * 0.22f)
                            catPosY.snapTo(arenaH * 0.68f)
                            featherX = arenaW * 0.68f
                            featherY = arenaH * 0.32f
                            wandTipX = arenaW * 0.82f
                            wandTipY = arenaH * 0.16f
                        }

                        // Lớp vẽ Canvas: Sàn cỏ pastel + Dây cần câu + Lông vũ + Hạt
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Viền thảm
                            drawRoundRect(
                                color = Color(0xFFD7ECE0),
                                topLeft = Offset(10f, 10f),
                                size = Size(size.width - 20f, size.height - 20f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(18f, 18f)
                            )

                            // Bóng đổ chân mèo (co nhỏ lại khi nhảy cao)
                            val shadowScale = (1f - (jumpHeight.value / 95f)).coerceIn(0.50f, 1f)
                            val shadowAlpha = (0.28f - (jumpHeight.value / 200f)).coerceIn(0.06f, 0.28f)
                            drawOval(
                                color = Color(0x352B3B30).copy(alpha = shadowAlpha),
                                topLeft = Offset(catPosX.value - (catBoxW * 0.36f * shadowScale), catPosY.value + (catBoxH * 0.38f)),
                                size = Size(catBoxW * 0.72f * shadowScale, catBoxH * 0.16f * shadowScale)
                            )

                            // Cần câu que gỗ + Dây cước + Chùm lông vũ
                            drawFeatherWand(
                                wandTip = Offset(wandTipX, wandTipY),
                                featherPos = Offset(featherX, featherY),
                                swayAngle = featherSway.value,
                                w = size.width
                            )

                            // Hạt lấp lánh khi vồ trúng
                            if (burstProgress.value > 0f && burstProgress.value < 1f) {
                                drawCatchBurst(Offset(featherX, featherY), burstProgress.value)
                            }
                        }

                        val featherGameActionState = if (isJumping) PetActionState.PLAYING else PetActionState.IDLE
                        val featherGamePetState = if (isCatchSuccess) petState.copy(temporaryMood = PetMood.HAPPY) else petState
                        val featherGameAnimState = rememberPetAnimationState(petState = featherGamePetState, actionState = featherGameActionState)

                        // Lớp vẽ bé Mèo Chibi Canvas thật (Đúng Skin + Phụ kiện Tủ đồ)
                        Box(
                            modifier = Modifier
                                .size(95.dp, 110.dp)
                                .offset {
                                    IntOffset(
                                        x = (catPosX.value - catBoxW / 2f).toInt(),
                                        y = (catPosY.value - catBoxH / 2f - jumpHeight.value).toInt()
                                    )
                                }
                                .graphicsLayer {
                                    scaleX = catScaleX.value * facingDir
                                    scaleY = catScaleY.value
                                    rotationZ = catRotation.value
                                }
                        ) {
                            PetCanvas(
                                petState = featherGamePetState,
                                modifier = Modifier.fillMaxSize(),
                                actionState = featherGameActionState,
                                animState = featherGameAnimState,
                                facingDirection = 1f,
                                lookAt = Offset((featherX - catPosX.value) * facingDir / 150f, (featherY - catPosY.value) / 150f),
                                actionProgress = if (isJumping) (jumpHeight.value / 120f).coerceIn(0f, 1f) else null
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════
// CÁC HÀM VẼ CANVAS VECTOR CHO MINI-GAME
// ═══════════════════════════════════════════════════════════════════

/** Vẽ cuộn len có vân chỉ cuộn tròn và đuôi len uốn lượn */
private fun DrawScope.drawYarnBall(
    cx: Float,
    cy: Float,
    r: Float,
    rotationDeg: Float
) {
    // Bóng đổ dưới cuộn len
    drawOval(
        color = Color(0x304A3525),
        topLeft = Offset(cx - r * 0.9f, cy + r * 0.65f),
        size = Size(r * 1.8f, r * 0.55f)
    )

    withTransform({
        rotate(degrees = rotationDeg, pivot = Offset(cx, cy))
    }) {
        // Nền bóng len tròn
        drawCircle(color = Color(0xFFF07A60), radius = r, center = Offset(cx, cy))
        drawCircle(color = Color(0xFFD3543A), radius = r, center = Offset(cx, cy), style = Stroke(width = 2.8f))

        // Các sợi chỉ cuộn chéo nhau
        for (i in 0..3) {
            val angle = i * 45f
            val rad = Math.toRadians(angle.toDouble())
            val dx = (r * 0.75f * cos(rad)).toFloat()
            val dy = (r * 0.75f * sin(rad)).toFloat()
            drawLine(
                color = Color(0xFFFF9E88),
                start = Offset(cx - dx, cy - dy),
                end = Offset(cx + dx, cy + dy),
                strokeWidth = 3.2f,
                cap = StrokeCap.Round
            )
        }
    }

    // Đuôi sợi len xoắn lượn ra ngoài
    val tailPath = Path().apply {
        moveTo(cx + r * 0.8f, cy + r * 0.2f)
        quadraticTo(cx + r * 1.5f, cy + r * 0.1f, cx + r * 1.3f, cy - r * 0.5f)
        quadraticTo(cx + r * 1.8f, cy - r * 0.2f, cx + r * 2.1f, cy - r * 0.6f)
    }
    drawPath(tailPath, color = Color(0xFFD3543A), style = Stroke(width = 3.2f, cap = StrokeCap.Round))
}

/** Vẽ cần câu que gỗ + dây cước + chùm lông vũ đung đưa */
private fun DrawScope.drawFeatherWand(
    wandTip: Offset,
    featherPos: Offset,
    swayAngle: Float,
    w: Float
) {
    val wandBase = Offset(w * 0.96f, 15f)

    // Cần câu que gỗ
    drawLine(
        color = Color(0xFF8D6E63),
        start = wandBase,
        end = wandTip,
        strokeWidth = 6.5f,
        cap = StrokeCap.Round
    )
    // Khớp tay cầm
    drawLine(
        color = Color(0xFF5D4037),
        start = wandBase,
        end = Offset(wandBase.x - 25f, wandBase.y + 20f),
        strokeWidth = 8f,
        cap = StrokeCap.Round
    )

    // Dây cước mảnh uốn cong nhẹ từ đầu cần xuống chùm lông vũ
    val stringPath = Path().apply {
        moveTo(wandTip.x, wandTip.y)
        val midX = (wandTip.x + featherPos.x) / 2f + (swayAngle * 0.8f)
        val midY = (wandTip.y + featherPos.y) / 2f
        quadraticTo(midX, midY, featherPos.x, featherPos.y)
    }
    drawPath(
        path = stringPath,
        color = Color(0x904E342E),
        style = Stroke(width = 1.8f, cap = StrokeCap.Round)
    )

    // Chùm lông vũ đa sắc đung đưa theo góc swayAngle
    withTransform({
        rotate(degrees = swayAngle, pivot = featherPos)
    }) {
        val colors = listOf(
            Pair(Color(0xFFE57373), -22f), // Hồng san hô
            Pair(Color(0xFF4DB6AC), 0f),   // Xanh ngọc
            Pair(Color(0xFFBA68C8), 22f)   // Tím pastel
        )
        colors.forEach { (color, angleOffset) ->
            withTransform({
                rotate(degrees = angleOffset, pivot = featherPos)
            }) {
                drawSingleFeather(featherPos.x, featherPos.y, 28f, color)
            }
        }
        // Chuông nhỏ leng keng ở cuống lông vũ
        drawCircle(color = Color(0xFFFFD54F), radius = 5.5f, center = featherPos)
        drawCircle(color = Color(0xFFFFA000), radius = 5.5f, center = featherPos, style = Stroke(width = 1.5f))
    }
}

/** Vẽ từng nhánh lông vũ mềm mại */
private fun DrawScope.drawSingleFeather(cx: Float, cy: Float, len: Float, mainColor: Color) {
    // Sống lông vũ
    drawLine(
        color = mainColor.copy(alpha = 0.9f),
        start = Offset(cx, cy),
        end = Offset(cx, cy + len * 1.5f),
        strokeWidth = 2.5f,
        cap = StrokeCap.Round
    )
    // Các sợi lông 2 bên
    listOf(-1f, 1f).forEach { side ->
        for (i in 0..5) {
            val fy = cy + i * (len * 0.24f)
            val fLen = (len * 0.65f) * (1f - (i * 0.12f))
            drawLine(
                color = mainColor,
                start = Offset(cx, fy),
                end = Offset(cx + side * fLen, fy + len * 0.15f),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }
    }
}

/** Hiệu ứng hạt sao vàng bung nở khi bắt trúng */
private fun DrawScope.drawCatchBurst(center: Offset, progress: Float) {
    if (progress <= 0f || progress >= 1f) return
    val count = 8
    for (i in 0 until count) {
        val angle = (i * (360f / count)) + (progress * 45f)
        val rad = Math.toRadians(angle.toDouble())
        val dist = 24f + progress * 55f
        val px = (center.x + dist * cos(rad)).toFloat()
        val py = (center.y + dist * sin(rad)).toFloat()
        val particleSize = (1f - progress) * 7.5f

        if (i % 2 == 0) {
            drawCircle(
                color = Color(0xFFFFD54F).copy(alpha = (1f - progress).coerceIn(0f, 1f)),
                radius = particleSize,
                center = Offset(px, py)
            )
        } else {
            drawCircle(
                color = Color(0xFFFF8A80).copy(alpha = (1f - progress).coerceIn(0f, 1f)),
                radius = particleSize * 0.8f,
                center = Offset(px, py)
            )
        }
    }
}
