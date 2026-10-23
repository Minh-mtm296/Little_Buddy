package com.littlebuddy.pet.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.littlebuddy.pet.R
import com.littlebuddy.pet.pet.BowlFood
import com.littlebuddy.pet.pet.PetActionState
import com.littlebuddy.pet.pet.PetCanvas
import com.littlebuddy.pet.pet.PetState
import com.littlebuddy.pet.pet.PetAnimationState
import com.littlebuddy.pet.pet.rememberPetAnimationState
import com.littlebuddy.pet.ui.components.CompactStatsRow
import com.littlebuddy.pet.ui.components.CozyRoomBackground
import com.littlebuddy.pet.ui.components.FeedbackBubble
import com.littlebuddy.pet.ui.components.PetActionButtons
import com.littlebuddy.pet.ui.components.PetProfileDialog
import com.littlebuddy.pet.ui.theme.BlushPetal
import com.littlebuddy.pet.ui.theme.MidnightLagoon
import com.littlebuddy.pet.ui.theme.Rosewood
import com.littlebuddy.pet.ui.theme.SoftWhite
import com.littlebuddy.pet.ui.theme.VanillaCream
import com.littlebuddy.pet.ui.viewmodel.PetViewModel

private val RoomFrameBrown = Color(0xFF6E5240)

/**
 * Màn hình chính Little Buddy — Virtual Pet
 *
 * Tích hợp hệ thống Hoạt họa Đa Khớp Xương (Procedural Animation) bằng Jetpack Compose Canvas:
 * - 100% Canvas vector mượt mà 60 FPS, không sử dụng ảnh PNG tĩnh gây cứng nhắc.
 * - Mèo quay đầu theo chiều di chuyển (quay phải khi tới bát ăn, quay trái khi về đệm).
 * - Bước chân so le, thân lắc lư nhấp nhô nhịp bước khi đi bộ.
 * - Nằm cuộn tròn ngủ thực sự trên đệm với nhịp thở sâu chậm rãi.
 * - Chuỗi động tác vươn vai (stretching) duỗi 2 chân trước trước khi thức dậy đi ra phòng.
 * - Phản ứng vuốt ve gầm gừ purr vibration, híp mắt cười hạnh phúc ^ ^ và trái tim bay lượn.
 */
@Composable
fun PetHomeScreen(
    viewModel: PetViewModel,
    modifier: Modifier = Modifier
) {
    val petState by viewModel.petState.collectAsState()
    val actionState by viewModel.actionState.collectAsState()
    val catPositionX by viewModel.catPositionX.collectAsState()
    val facingDirection by viewModel.facingDirection.collectAsState()
    val bowlFood by viewModel.bowlFood.collectAsState()
    val showWardrobe by viewModel.showWardrobe.collectAsState()
    val previewWardrobe by viewModel.previewWardrobe.collectAsState()
    val showFoodPicker by viewModel.showFoodPicker.collectAsState()
    val showGamePicker by viewModel.showGamePicker.collectAsState()
    var showProfileDialog by remember { mutableStateOf(false) }
    var showYarnGame by remember { mutableStateOf(false) }
    var showFeatherGame by remember { mutableStateOf(false) }

    // Quản lý animation đa tầng thời gian thực
    val animValues = rememberPetAnimationState(petState = petState, actionState = actionState)

    // Animation nội suy vị trí di chuyển ngang mượt mà
    val animatedCatX by animateFloatAsState(
        targetValue = catPositionX,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "catWalkSmooth"
    )

    // Feedback tự ẩn đã được xử lý bởi PetViewModel.showFeedback(autoDismissMs) —
    // không cần LaunchedEffect tại đây để tránh cancel kép.

    if (showWardrobe) {
        WardrobeScreen(
            petState = petState,
            previewWardrobe = previewWardrobe,
            onSelectItem = { item -> viewModel.selectPreviewItem(item) },
            onUnequipCategory = { category -> viewModel.unequipCategoryInPreview(category) },
            onResetDefault = { viewModel.resetPreviewToDefault() },
            onConfirm = { viewModel.confirmWardrobe() },
            onBack = { viewModel.closeWardrobe() },
            modifier = modifier
        )
    } else {
        Surface(modifier = modifier.fillMaxSize(), color = VanillaCream) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                val wide = maxWidth > maxHeight
                if (wide) {
                    // Bố cục ngang (Landscape): Phòng chiếm 1.7f và toàn bộ chiều cao, điều khiển bên phải
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1.7f)
                                .fillMaxHeight()
                        ) {
                            PetRoomSection(
                                petState = petState,
                                actionState = actionState,
                                animValues = animValues,
                                animatedCatX = animatedCatX,
                                facingDirection = facingDirection,
                                bowlFood = bowlFood,
                                onPet = { viewModel.pet() },
                                onScratchPost = { viewModel.scratchPost() },
                                onPlayYarn = { viewModel.playWithYarn() },
                                onDrinkWater = { viewModel.drinkWater() },
                                onBowlClick = { viewModel.openFoodPicker() },
                                onClearFeedback = { viewModel.clearFeedback() },
                                onShowFeedback = { msg, dur -> viewModel.showFeedback(msg, dur) },
                                isWideMode = true,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            verticalArrangement = Arrangement.SpaceBetween,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            PetHeaderSection(
                                petState = petState,
                                onEditClick = { showProfileDialog = true }
                            )

                            CompactStatsRow(
                                petState = petState,
                                modifier = Modifier.fillMaxWidth()
                            )

                            PetActionButtons(
                                petState = petState,
                                onFeed = { viewModel.openFoodPicker() },
                                onPlay = { viewModel.openGamePicker() },
                                onWardrobe = { viewModel.openWardrobe() },
                                onToggleSleep = { viewModel.toggleSleep() },
                                modifier = Modifier.fillMaxWidth(),
                                isGrid = true
                            )
                        }
                    }
                } else {
                    // Bố cục dọc (Portrait)
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // ═══ A. HEADER THÔNG TIN ═════════════════════════════════════════
                        PetHeaderSection(
                            petState = petState,
                            onEditClick = { showProfileDialog = true }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // ═══ B. KHU VỰC PHÒNG & NHÂN VẬT 2D CANVAS COZY ROOM ═══════════
                        PetRoomSection(
                            petState = petState,
                            actionState = actionState,
                            animValues = animValues,
                            animatedCatX = animatedCatX,
                            facingDirection = facingDirection,
                            bowlFood = bowlFood,
                            onPet = { viewModel.pet() },
                            onScratchPost = { viewModel.scratchPost() },
                            onPlayYarn = { viewModel.playWithYarn() },
                            onDrinkWater = { viewModel.drinkWater() },
                            onBowlClick = { viewModel.openFoodPicker() },
                            onClearFeedback = { viewModel.clearFeedback() },
                            onShowFeedback = { msg, dur -> viewModel.showFeedback(msg, dur) },
                            isWideMode = false,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1.3f)
                                .heightIn(min = 280.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // ═══ C. THANH CHỈ SỐ THU GỌN (1 HÀNG NGANG) ═══════════════════
                        CompactStatsRow(
                            petState = petState,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // ═══ D. CỤM PHÍM HÀNH ĐỘNG ═══════════════════════════════════════
                        PetActionButtons(
                            petState = petState,
                            onFeed = { viewModel.openFoodPicker() },
                            onPlay = { viewModel.openGamePicker() },
                            onWardrobe = { viewModel.openWardrobe() },
                            onToggleSleep = { viewModel.toggleSleep() },
                            modifier = Modifier.fillMaxWidth(),
                            isGrid = false
                        )
                    }
                }
            }
        }
    }

    // ── DIALOGS ──────────────────────────────────────────────────────────────

    if (showProfileDialog) {
        PetProfileDialog(
            petState = petState,
            onDismiss = { showProfileDialog = false },
            onSaveName = { viewModel.renamePet(it) },
            onSelectType = { viewModel.switchPetType(it) },
            onResetPet = { viewModel.resetPet() }
        )
    }

    if (showFoodPicker) {
        FoodPickerDialog(
            petState = petState,
            onSelectFood = { food -> viewModel.selectFood(food) },
            onDismiss = { viewModel.closeFoodPicker() }
        )
    }

    if (showGamePicker) {
        GamePickerDialog(
            petState = petState,
            onSelectYarn = {
                viewModel.closeGamePicker()
                showYarnGame = true
            },
            onSelectFeather = {
                viewModel.closeGamePicker()
                showFeatherGame = true
            },
            onDismiss = { viewModel.closeGamePicker() }
        )
    }

    if (showYarnGame) {
        YarnChaseGame(
            petState = petState,
            onFinish = { h, e, n ->
                showYarnGame = false
                viewModel.finishPlaying(h, e, n)
            },
            onDismiss = { showYarnGame = false }
        )
    }

    if (showFeatherGame) {
        FeatherHuntGame(
            petState = petState,
            onFinish = { h, e, n ->
                showFeatherGame = false
                viewModel.finishPlaying(h, e, n)
            },
            onDismiss = { showFeatherGame = false }
        )
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// KHU VỰC PHÒNG & NHÂN VẬT 2D CANVAS COZY ROOM
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Khu vực phòng và nhân vật 2D Cozy Room:
 * - Kích thước nhân vật mèo và các vật thể (bát, cuộn len, cột cào, cây) được tính toán theo tỉ lệ chiều cao và rộng của phòng.
 * - Tự động thích ứng mượt mà giữa chế độ đứng (Portrait) và ngang (Landscape).
 */
@Composable
private fun PetRoomSection(
    petState: PetState,
    actionState: PetActionState,
    animValues: PetAnimationState,
    animatedCatX: Float,
    facingDirection: Float,
    bowlFood: BowlFood,
    onPet: () -> Unit,
    onScratchPost: () -> Unit,
    onPlayYarn: () -> Unit,
    onDrinkWater: () -> Unit,
    onBowlClick: () -> Unit,
    onClearFeedback: () -> Unit,
    onShowFeedback: (String, Long) -> Unit,
    isWideMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier
            .shadow(6.dp, RoundedCornerShape(26.dp))
            .clip(RoundedCornerShape(26.dp))
            .border(3.5.dp, RoomFrameBrown, RoundedCornerShape(26.dp)),
        contentAlignment = Alignment.BottomCenter
    ) {
        val roomWidth = maxWidth
        val roomHeight = maxHeight

        // 1. Background căn phòng pastel ấm cúng
        CozyRoomBackground(modifier = Modifier.fillMaxSize())

        // Lớp phủ ánh sáng đêm ấm áp khi mèo đi ngủ
        val nightAlpha by animateFloatAsState(
            targetValue = if (petState.isSleeping || actionState == PetActionState.SLEEPING) 0.30f else 0f,
            animationSpec = tween(900),
            label = "nightTint"
        )
        if (nightAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1F3B).copy(alpha = nightAlpha))
            )
        }

        // 2. Điểm chạm tương tác: Cột cào móng mèo ở góc phải phòng (X=0.88w, Y=0.58h)
        val scratchWidth = (roomWidth * 0.12f).coerceAtLeast(36.dp)
        val scratchHeight = (roomHeight * 0.20f).coerceAtLeast(46.dp)
        val scratchOffsetX = (roomWidth * 0.88f) - (scratchWidth / 2f)
        val scratchOffsetY = (roomHeight * 0.58f)

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = scratchOffsetX, y = scratchOffsetY)
                .size(width = scratchWidth, height = scratchHeight)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onScratchPost() }
        )

        // 3. Điểm chạm tương tác: Chậu cây cảnh góc phòng (X=0.05w, Y=0.74h)
        val plantWidth = (roomWidth * 0.12f).coerceAtLeast(34.dp)
        val plantHeight = (roomHeight * 0.14f).coerceAtLeast(36.dp)
        val plantOffsetX = ((roomWidth * 0.05f) - (plantWidth / 2f)).coerceAtLeast(0.dp)
        val plantOffsetY = (roomHeight * 0.74f) - (plantHeight * 0.5f)

        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = plantOffsetX, y = plantOffsetY)
                .size(width = plantWidth, height = plantHeight)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    onShowFeedback("🌿 Cây xanh đón nắng thu ấm áp, tươi tốt quá! ✨", 3000L)
                }
        )

        // 4. Cuộn len chơi đùa trên sàn (roomHeight * 0.12f, padding end = roomWidth * 0.20f)
        val yarnSize = (roomHeight * 0.12f).coerceIn(22.dp, 38.dp)
        InteractiveYarnBall(
            ballSize = yarnSize,
            onPlayYarn = onPlayYarn,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = roomWidth * 0.20f, bottom = (roomHeight * 0.05f).coerceAtLeast(10.dp))
        )

        // 5. Cụm trạm ăn & uống (Bát nước gốm sứ + Bát thức ăn)
        val foodBowlH = (roomHeight * 0.14f).coerceIn(26.dp, 46.dp)
        val foodBowlW = foodBowlH * 1.5f
        val waterBowlH = (roomHeight * 0.12f).coerceIn(22.dp, 38.dp)
        val waterBowlW = waterBowlH * 1.22f

        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = (roomHeight * 0.04f).coerceAtLeast(8.dp)),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            WaterBowlInRoom(
                width = waterBowlW,
                height = waterBowlH,
                onDrinkWater = onDrinkWater
            )
            FoodBowlInRoom(
                width = foodBowlW,
                height = foodBowlH,
                bowlFood = bowlFood,
                onBowlClick = onBowlClick
            )
        }

        // 6. Chú mèo chuyển động mượt mà (KÍCH THƯỚC TÍNH THEO PHÒNG)
        val isSleepingMode = petState.isSleeping || actionState == PetActionState.SLEEPING
        val catHeight = (roomHeight * 0.85f).coerceAtMost(290.dp)
        val catWidth = if (isSleepingMode) catHeight * 1.29f else catHeight * 0.97f
        val floorPad = roomHeight * 0.12f

        val maxTravelWidth = (roomWidth - catWidth).coerceAtLeast(0.dp)
        val currentOffsetX = maxTravelWidth * animatedCatX.coerceIn(0f, 1f)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = floorPad),
            contentAlignment = Alignment.BottomStart
        ) {
            Box(
                modifier = Modifier
                    .offset(x = currentOffsetX, y = 0.dp)
                    .size(width = catWidth, height = catHeight)
                    .pointerInput(petState.isSleeping, actionState) {
                        if (actionState != PetActionState.WALKING &&
                            actionState != PetActionState.EATING &&
                            actionState != PetActionState.STRETCHING
                        ) {
                            detectTapGestures(onTap = { onPet() })
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                PetCanvas(
                    petState = petState,
                    actionState = actionState,
                    animState = animValues,
                    facingDirection = facingDirection,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 7. Bong bóng lời thoại tương tác (Comic Speech & Thought Cloud Bubble)
        if (petState.feedbackMessage.isNotBlank()) {
            val catCenterX = currentOffsetX + catWidth * 0.5f
            val catCenterRatio = if (roomWidth > 0.dp) (catCenterX / roomWidth).coerceIn(0f, 1f) else 0.5f

            // Vị trí đầu mèo khi ngủ: đầu mèo nằm ở khoảng 35% từ mép trái thân mèo
            val catHeadX = currentOffsetX + catWidth * 0.35f
            val catHeadFraction = if (roomWidth > 0.dp) (catHeadX / roomWidth).coerceIn(0.15f, 0.85f) else 0.25f

            val (bubbleHorizontalBias, tailPositionFraction, bubbleTopPadding) = if (isWideMode) {
                // Ở chế độ ngang: Đặt bong bóng sang phía đối diện mèo để không che khuất
                if (catCenterRatio < 0.5f) {
                    Triple(0.55f, 0.25f, (roomHeight * 0.10f).coerceAtLeast(8.dp))
                } else {
                    Triple(-0.55f, 0.75f, (roomHeight * 0.10f).coerceAtLeast(8.dp))
                }
            } else {
                // Ở chế độ dọc
                if (isSleepingMode) {
                    val bias = (catHeadFraction - 0.5f) * 1.5f
                    val tail = (0.5f + bias * 0.25f).coerceIn(0.20f, 0.80f)
                    val topPad = (roomHeight * 0.10f).coerceAtLeast(8.dp)
                    Triple(bias, tail, topPad)
                } else {
                    val bias = (catCenterRatio - 0.5f) * 1.5f
                    val tail = (0.5f + bias * 0.22f).coerceIn(0.25f, 0.75f)
                    val topPad = (roomHeight * 0.035f).coerceAtLeast(8.dp)
                    Triple(bias, tail, topPad)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = bubbleTopPadding, start = 12.dp, end = 12.dp),
                contentAlignment = BiasAlignment(horizontalBias = bubbleHorizontalBias, verticalBias = 0f)
            ) {
                FeedbackBubble(
                    message = petState.feedbackMessage,
                    isSleeping = isSleepingMode,
                    tailPositionFraction = tailPositionFraction,
                    onBubbleClick = onClearFeedback
                )
            }
        }

        // 8. Nhãn trạng thái / gợi ý tương tác
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = (roomHeight * 0.02f).coerceAtLeast(4.dp))
                .padding(horizontal = 8.dp)
                .clip(RoundedCornerShape(50))
                .background(VanillaCream.copy(alpha = 0.90f))
                .border(1.dp, BlushPetal, RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 3.dp)
        ) {
            Text(
                text = when (actionState) {
                    PetActionState.WALKING -> "🐾 ${petState.petName} đang bước đi..."
                    PetActionState.EATING -> "🍽️ ${petState.petName} đang thưởng thức món ngon~"
                    PetActionState.SLEEPING -> "💤 ${petState.petName} đang cuộn tròn ngủ ngoan trên đệm..."
                    PetActionState.WAKING_UP -> "✨ ${petState.petName} khẽ chớp mắt tỉnh giấc..."
                    PetActionState.STRETCHING -> "🧘 ${petState.petName} đang vươn vai sảng khoái... Meo~"
                    PetActionState.PETTING -> "💕 ${petState.petName} đang gầm gừ hạnh phúc vì được vuốt ve ♡"
                    else -> "Chạm vào ${petState.petName} để vuốt ve ♡"
                },
                style = MaterialTheme.typography.labelSmall,
                color = MidnightLagoon,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// BÁT THỨC ĂN TRONG PHÒNG (DRAWABLE: R.drawable.bat_do_an)
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Bát thức ăn hiển thị hình ảnh từ drawable (R.drawable.bat_do_an).
 * Khi đã chọn món, hiển thị đồ ăn trong lòng bát và nhãn tên món ăn.
 * Người dùng có thể chạm trực tiếp vào bát để mở bảng chọn thức ăn.
 */
@Composable
private fun FoodBowlInRoom(
    bowlFood: BowlFood,
    width: Dp = 66.dp,
    height: Dp = 44.dp,
    onBowlClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onBowlClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(width = width, height = height),
            contentAlignment = Alignment.Center
        ) {
            // Bóng đổ nhẹ dưới đáy bát trên sàn
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.92f)
                    .height(height * 0.20f)
            ) {
                drawOval(
                    color = Color(0x35543D36),
                    size = size
                )
            }

            // Hình ảnh chiếc bát lấy trực tiếp từ drawable tương ứng với món ăn (bat_do_an, bat_hat, bat_pate, bat_ca_kho)
            Image(
                painter = painterResource(id = bowlFood.drawableRes),
                contentDescription = bowlFood.displayName,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }

        if (bowlFood != BowlFood.EMPTY) {
            Spacer(modifier = Modifier.height(2.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(VanillaCream.copy(alpha = 0.95f))
                    .border(1.dp, Color(0xFF8B6914).copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
                Text(
                    text = bowlFood.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MidnightLagoon,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// BÁT NƯỚC UỐNG TRONG PHÒNG
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Bát nước uống cho mèo đặt cạnh bát thức ăn.
 * Khi chạm vào, mèo được uống nước mát và hồi phục năng lượng/tâm trạng.
 */
@Composable
private fun WaterBowlInRoom(
    onDrinkWater: () -> Unit,
    width: Dp = 44.dp,
    height: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onDrinkWater() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(width = width, height = height),
            contentAlignment = Alignment.Center
        ) {
            // Bóng đổ đáy bát nước trên sàn
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.90f)
                    .height((height * 0.20f).coerceAtLeast(4.dp))
            ) {
                drawOval(
                    color = Color(0x35543D36),
                    size = size
                )
            }

            // Bát nước vẽ vector: viền gốm pastel + mặt nước trong xanh có ánh sáng phản chiếu
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val bw = size.width
                val bh = size.height
                // Vành ngoài bát gốm sứ màu xanh ngọc nhạt
                drawOval(
                    color = Color(0xFFC4E0DC),
                    topLeft = Offset(bw * 0.08f, bh * 0.16f),
                    size = Size(bw * 0.84f, bh * 0.68f)
                )
                drawOval(
                    color = Color(0xFF6E9993),
                    topLeft = Offset(bw * 0.08f, bh * 0.16f),
                    size = Size(bw * 0.84f, bh * 0.68f),
                    style = Stroke(width = (bh * 0.06f).coerceAtLeast(1.5f))
                )
                // Lòng nước trong xanh
                drawOval(
                    color = Color(0xFF75BED7),
                    topLeft = Offset(bw * 0.16f, bh * 0.24f),
                    size = Size(bw * 0.68f, bh * 0.52f)
                )
                // Vệt sóng nước / phản quang lấp lánh
                drawOval(
                    color = Color(0xFFBBE5F4),
                    topLeft = Offset(bw * 0.24f, bh * 0.30f),
                    size = Size(bw * 0.38f, bh * 0.24f)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.90f),
                    radius = (bh * 0.07f).coerceAtLeast(1.5f),
                    center = Offset(bw * 0.64f, bh * 0.40f)
                )
            }

            Text(
                text = "💧",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.Center).offset(y = (-2).dp)
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CUỘN LEN CHƠI ĐÙA TRÊN SÀN (YARN BALL)
// ═══════════════════════════════════════════════════════════════════════════════

/**
 * Cuộn len sắc màu đặt trên thảm - điểm chạm chơi đùa của mèo khi người dùng nhấn vào.
 */
@Composable
private fun InteractiveYarnBall(
    onPlayYarn: () -> Unit,
    ballSize: Dp = 36.dp,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) { onPlayYarn() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(width = ballSize * 1.12f, height = ballSize),
            contentAlignment = Alignment.Center
        ) {
            // Bóng đổ đáy cuộn len
            androidx.compose.foundation.Canvas(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(0.85f)
                    .height((ballSize * 0.18f).coerceAtLeast(4.dp))
            ) {
                drawOval(
                    color = Color(0x35543D36),
                    size = size
                )
            }

            // Quả cầu len màu hồng đào ấm áp với vân sợi cuộn tròn
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width * 0.48f
                val cy = size.height * 0.46f
                val r = size.minDimension * 0.36f

                // Thân quả cầu
                drawCircle(color = Color(0xFFE88A7A), radius = r, center = Offset(cx, cy))
                drawCircle(color = Color(0xFF8C4237), radius = r, center = Offset(cx, cy), style = Stroke(width = (r * 0.14f).coerceAtLeast(1.2f)))

                // Vân sợi len đan chéo
                drawArc(
                    color = Color(0xFFF7B5A8),
                    startAngle = 25f,
                    sweepAngle = 130f,
                    useCenter = false,
                    topLeft = Offset(cx - r * 0.72f, cy - r * 0.72f),
                    size = Size(r * 1.44f, r * 1.44f),
                    style = Stroke(width = (r * 0.15f).coerceAtLeast(1.2f))
                )
                drawArc(
                    color = Color(0xFF8C4237),
                    startAngle = 165f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(cx - r * 0.8f, cy - r * 0.6f),
                    size = Size(r * 1.6f, r * 1.2f),
                    style = Stroke(width = (r * 0.12f).coerceAtLeast(1.2f))
                )

                // Đuôi sợi len uốn lượn lăn dài trên sàn
                val yarnThread = Path().apply {
                    moveTo(cx + r * 0.65f, cy + r * 0.5f)
                    quadraticTo(cx + r * 1.15f, cy + r * 0.75f, cx + r * 1.55f, cy + r * 0.65f)
                }
                drawPath(
                    path = yarnThread,
                    color = Color(0xFFE88A7A),
                    style = Stroke(width = (r * 0.18f).coerceAtLeast(1.5f), cap = StrokeCap.Round)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// HEADER
// ═══════════════════════════════════════════════════════════════════════════════

@Composable
private fun PetHeaderSection(
    petState: PetState,
    onEditClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f, fill = false)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onEditClick() }
                .padding(vertical = 2.dp, horizontal = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${petState.petName} ♡",
                    style = MaterialTheme.typography.titleLarge,
                    color = MidnightLagoon,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                petState.accessory?.let { acc ->
                    Text(text = acc.emoji, style = MaterialTheme.typography.bodyMedium)
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(BlushPetal.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Đổi tên",
                        tint = Rosewood,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            val formattedLv = petState.level.toString().padStart(2, '0')
            Text(
                text = "Lv.$formattedLv · ${petState.levelTitle}",
                style = MaterialTheme.typography.labelMedium,
                color = MidnightLagoon.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(3.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(text = "EXP", style = MaterialTheme.typography.labelSmall, color = Rosewood)
                LinearProgressIndicator(
                    progress = { petState.expProgress },
                    modifier = Modifier
                        .width(90.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50)),
                    color = Rosewood,
                    trackColor = BlushPetal.copy(alpha = 0.45f)
                )
                Text(
                    text = "${petState.exp}/${petState.expToNextLevel}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MidnightLagoon.copy(alpha = 0.55f)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(SoftWhite)
                .border(1.2.dp, Rosewood.copy(alpha = 0.35f), RoundedCornerShape(50))
                .clickable { onEditClick() }
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = "Đổi tên bé mèo",
                tint = Rosewood,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = "Đổi tên",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                color = Rosewood
            )
        }
    }
}
