package com.littlebuddy.pet.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.littlebuddy.pet.pet.PetActionState
import com.littlebuddy.pet.pet.PetCanvas
import com.littlebuddy.pet.pet.PetState
import com.littlebuddy.pet.ui.theme.CardBorderColor
import com.littlebuddy.pet.ui.theme.MidnightLagoon
import com.littlebuddy.pet.ui.theme.Rosewood
import com.littlebuddy.pet.ui.theme.SageLeaf
import com.littlebuddy.pet.ui.theme.SoftWhite
import com.littlebuddy.pet.ui.theme.SurfaceCream
import com.littlebuddy.pet.ui.theme.VanillaCream
import com.littlebuddy.pet.wardrobe.CatColorPalette
import com.littlebuddy.pet.wardrobe.EquippedWardrobe
import com.littlebuddy.pet.wardrobe.WardrobeCategory
import com.littlebuddy.pet.wardrobe.WardrobeItem
import com.littlebuddy.pet.wardrobe.WardrobeRegistry


@Composable
fun WardrobeScreen(
    petState: PetState,
    previewWardrobe: EquippedWardrobe,
    onSelectItem: (WardrobeItem) -> Unit,
    onUnequipCategory: (WardrobeCategory) -> Unit,
    onResetDefault: () -> Unit,
    onConfirm: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(WardrobeCategory.FUR_SKIN) }
    var inspectedItem by remember { mutableStateOf<WardrobeItem?>(null) }

        val infiniteTransition = rememberInfiniteTransition(label = "wardrobe_anim")
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.015f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    val tailWagAngle by infiniteTransition.animateFloat(
        initialValue = -7f,
        targetValue = 7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tail_wag"
    )

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(VanillaCream)
            .safeDrawingPadding()
    ) {
        val isLandscape = maxWidth > maxHeight

        if (isLandscape) {

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Cột trái (42%): Sân khấu thử đồ + Khay phụ kiện đang mặc
                Column(
                    modifier = Modifier
                        .weight(0.42f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    WardrobeTopBar(
                        petLevel = petState.level,
                        onBack = onBack,
                        onResetDefault = onResetDefault,
                        compact = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    WardrobeStageCard(
                        petState = petState,
                        previewWardrobe = previewWardrobe,
                        breatheScale = breatheScale,
                        tailWagAngle = tailWagAngle,
                        onUnequipCategory = onUnequipCategory,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                }

                // Cột phải (58%): Danh mục Tabs + Lưới vật phẩm + Nút Lưu
                Column(
                    modifier = Modifier
                        .weight(0.58f)
                        .fillMaxHeight()
                ) {
                    WardrobeCategoryFloatingTabs(
                        selectedCategory = selectedCategory,
                        onSelectCategory = {
                            selectedCategory = it
                            inspectedItem = null
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    WardrobeItemsGrid(
                        category = selectedCategory,
                        petLevel = petState.level,
                        previewWardrobe = previewWardrobe,
                        actualWardrobe = petState.wardrobe,
                        onSelectItem = { item ->
                            inspectedItem = item
                            onSelectItem(item)
                        },
                        columns = 4,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    WardrobeBottomActions(
                        onConfirm = onConfirm
                    )
                }
            }
        } else {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                WardrobeTopBar(
                    petLevel = petState.level,
                    onBack = onBack,
                    onResetDefault = onResetDefault,
                    compact = false
                )

                Spacer(modifier = Modifier.height(8.dp))

                WardrobeStageCard(
                    petState = petState,
                    previewWardrobe = previewWardrobe,
                    breatheScale = breatheScale,
                    tailWagAngle = tailWagAngle,
                    onUnequipCategory = onUnequipCategory,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                WardrobeCategoryFloatingTabs(
                    selectedCategory = selectedCategory,
                    onSelectCategory = {
                        selectedCategory = it
                        inspectedItem = null
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Lưới vật phẩm trang phục
                WardrobeItemsGrid(
                    category = selectedCategory,
                    petLevel = petState.level,
                    previewWardrobe = previewWardrobe,
                    actualWardrobe = petState.wardrobe,
                    onSelectItem = { item ->
                        inspectedItem = item
                        onSelectItem(item)
                    },
                    columns = 3,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 5. Thanh nút Lưu thay đổi cố định đáy
                WardrobeBottomActions(
                    onConfirm = onConfirm
                )
            }
        }
    }
}

@Composable
private fun WardrobeTopBar(
    petLevel: Int,
    onBack: () -> Unit,
    onResetDefault: () -> Unit,
    compact: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (compact) 2.dp else 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(SoftWhite)
                    .border(1.dp, CardBorderColor.copy(alpha = 0.8f), CircleShape)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Quay lại",
                    tint = MidnightLagoon,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Phòng Thử Đồ",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MidnightLagoon
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFF3D6),
                        border = BorderStroke(1.dp, Color(0xFFFFD166).copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "Lv.$petLevel",
                            color = Color(0xFFB45309),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                if (!compact) {
                    Text(
                        text = "Cá nhân hóa diện mạo cho bé mèo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MidnightLagoon.copy(alpha = 0.55f)
                    )
                }
            }
        }

        // Nút Đặt lại về mặc định
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = SoftWhite,
            border = BorderStroke(1.dp, CardBorderColor.copy(alpha = 0.8f)),
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .clickable { onResetDefault() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Mặc định",
                    tint = Rosewood,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Đặt lại",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Rosewood
                )
            }
        }
    }
}

@Composable
private fun WardrobeStageCard(
    petState: PetState,
    previewWardrobe: EquippedWardrobe,
    breatheScale: Float,
    tailWagAngle: Float,
    onUnequipCategory: (WardrobeCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = SoftWhite,
        border = BorderStroke(1.dp, CardBorderColor.copy(alpha = 0.6f)),
        shadowElevation = 2.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val podiumW = w * 0.52f
                    val podiumH = h * 0.22f
                    val podiumX = w / 2f
                    val podiumY = h * 0.83f

                    drawOval(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x35E8C8B0), Color(0x00E8C8B0)),
                            center = Offset(podiumX, podiumY),
                            radius = podiumW * 1.1f
                        ),
                        topLeft = Offset(podiumX - podiumW * 0.9f, podiumY - podiumH * 1.8f),
                        size = Size(podiumW * 1.8f, podiumH * 3.4f)
                    )

                    drawOval(
                        color = Color(0xFFDECDB9),
                        topLeft = Offset(podiumX - podiumW / 2f, podiumY - podiumH / 2f + 4.dp.toPx()),
                        size = Size(podiumW, podiumH)
                    )

                    drawOval(
                        color = Color(0xFFF3ECE0),
                        topLeft = Offset(podiumX - podiumW / 2f, podiumY - podiumH / 2f),
                        size = Size(podiumW, podiumH)
                    )
                    drawOval(
                        color = Color(0xFFDECDB9),
                        topLeft = Offset(podiumX - podiumW / 2f, podiumY - podiumH / 2f),
                        size = Size(podiumW, podiumH),
                        style = Stroke(width = 1.5f)
                    )

                    drawOval(
                        color = Color(0x354A3B32),
                        topLeft = Offset(podiumX - podiumW * 0.28f, podiumY - podiumH * 0.28f),
                        size = Size(podiumW * 0.56f, podiumH * 0.52f)
                    )
                }

                PetCanvas(
                    petState = petState,
                    actionState = PetActionState.IDLE,
                    breatheScale = breatheScale,
                    tailWagAngle = tailWagAngle,
                    wardrobeOverride = previewWardrobe,
                    modifier = Modifier.size(175.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chip Màu lông (luôn có)
                val furItem = previewWardrobe.getFurSkin()
                EquippedTrayChip(
                    icon = "🐱",
                    name = furItem.name,
                    canRemove = false,
                    onRemove = {}
                )

                // Mũ / Nơ
                previewWardrobe.getHeadwear()?.let { headItem ->
                    Spacer(modifier = Modifier.width(6.dp))
                    EquippedTrayChip(
                        icon = "👒",
                        name = headItem.name,
                        canRemove = true,
                        onRemove = { onUnequipCategory(WardrobeCategory.HEADWEAR) }
                    )
                }

                // Vòng / Khăn
                previewWardrobe.getNeckwear()?.let { neckItem ->
                    Spacer(modifier = Modifier.width(6.dp))
                    EquippedTrayChip(
                        icon = "🧣",
                        name = neckItem.name,
                        canRemove = true,
                        onRemove = { onUnequipCategory(WardrobeCategory.NECKWEAR) }
                    )
                }

                // Trang phục thân
                previewWardrobe.getBodywear()?.let { bodyItem ->
                    Spacer(modifier = Modifier.width(6.dp))
                    EquippedTrayChip(
                        icon = "👕",
                        name = bodyItem.name,
                        canRemove = true,
                        onRemove = { onUnequipCategory(WardrobeCategory.BODYWEAR) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EquippedTrayChip(
    icon: String,
    name: String,
    canRemove: Boolean,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (canRemove) Color(0xFFFFF0EC) else SurfaceCream,
        border = BorderStroke(1.dp, if (canRemove) Rosewood.copy(alpha = 0.35f) else CardBorderColor.copy(alpha = 0.6f))
    ) {
        Row(
            modifier = Modifier
                .clickable(enabled = canRemove, onClick = onRemove)
                .padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = icon, fontSize = 11.sp)
            Text(
                text = name,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (canRemove) Rosewood else MidnightLagoon
            )
            if (canRemove) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Tháo",
                    tint = Rosewood,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

/**
 * Thanh chuyển đổi danh mục
 */
@Composable
private fun WardrobeCategoryFloatingTabs(
    selectedCategory: WardrobeCategory,
    onSelectCategory: (WardrobeCategory) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = SoftWhite,
        border = BorderStroke(1.dp, CardBorderColor.copy(alpha = 0.6f)),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            WardrobeCategory.values().forEach { category ->
                val isSelected = category == selectedCategory
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            if (isSelected) {
                                Brush.horizontalGradient(listOf(Rosewood, Color(0xFFB55454)))
                            } else {
                                Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                            }
                        )
                        .clickable { onSelectCategory(category) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = category.icon, fontSize = 13.sp)
                        Text(
                            text = category.displayName,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SoftWhite else MidnightLagoon.copy(alpha = 0.75f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Lưới hiển thị các vật phẩm theo danh mục
 */
@Composable
private fun WardrobeItemsGrid(
    category: WardrobeCategory,
    petLevel: Int,
    previewWardrobe: EquippedWardrobe,
    actualWardrobe: EquippedWardrobe,
    onSelectItem: (WardrobeItem) -> Unit,
    columns: Int,
    modifier: Modifier = Modifier
) {
    val itemsList = WardrobeRegistry.getItemsForCategory(category)

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        items(itemsList, key = { it.id }) { item ->
            val isLocked = petLevel < item.levelRequired
            val isPreviewEquipped = previewWardrobe.isEquipped(item.id)
            val isActuallyEquipped = actualWardrobe.isEquipped(item.id)

            WardrobeItemCard(
                item = item,
                isLocked = isLocked,
                isPreviewEquipped = isPreviewEquipped,
                isActuallyEquipped = isActuallyEquipped,
                onClick = { onSelectItem(item) }
            )
        }
    }
}

@Composable
private fun WardrobeItemCard(
    item: WardrobeItem,
    isLocked: Boolean,
    isPreviewEquipped: Boolean,
    isActuallyEquipped: Boolean,
    onClick: () -> Unit
) {
    val borderColor = when {
        isPreviewEquipped -> Rosewood
        isActuallyEquipped -> SageLeaf
        else -> CardBorderColor.copy(alpha = 0.8f)
    }
    val borderWidth = if (isPreviewEquipped) 2.dp else if (isActuallyEquipped) 1.5.dp else 1.dp
    val bgColor = when {
        isPreviewEquipped -> Color(0xFFFFF0EC)
        isActuallyEquipped -> Color(0xFFF2F8F5)
        isLocked -> SurfaceCream.copy(alpha = 0.7f)
        else -> SoftWhite
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = bgColor,
        border = BorderStroke(borderWidth, borderColor),
        shadowElevation = if (isPreviewEquipped) 3.dp else 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFBF6EE)),
                    contentAlignment = Alignment.Center
                ) {
                    WardrobeItemThumbnail(
                        item = item,
                        isLocked = isLocked,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Tên vật phẩm
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isLocked) MidnightLagoon.copy(alpha = 0.55f) else MidnightLagoon,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Nhãn trạng thái
                when {
                    isPreviewEquipped -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Rosewood
                        ) {
                            Text(
                                text = "Đang thử",
                                color = SoftWhite,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                    isActuallyEquipped -> {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SageLeaf
                        ) {
                            Text(
                                text = "Đang mặc",
                                color = SoftWhite,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                            )
                        }
                    }
                    isLocked -> {
                        Text(
                            text = "Cần Cấp ${item.levelRequired}",
                            color = Color(0xFF948177),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    else -> {
                        Text(
                            text = "Sẵn sàng",
                            color = SageLeaf.copy(alpha = 0.85f),
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (isLocked) {
                Surface(
                    shape = RoundedCornerShape(topEnd = 18.dp, bottomStart = 10.dp),
                    color = Color(0xCC3D3436),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = "Khóa",
                            tint = Color(0xFFFFD166),
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "Lv.${item.levelRequired}",
                            color = SoftWhite,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else if (isPreviewEquipped) {
                // Dấu kiểm tra đang thử
                Surface(
                    shape = CircleShape,
                    color = Rosewood,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Đang thử",
                            tint = SoftWhite,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            } else if (isActuallyEquipped) {
                // Dấu kiểm tra đang mặc
                Surface(
                    shape = CircleShape,
                    color = SageLeaf,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Đang mặc",
                            tint = SoftWhite,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun WardrobeBottomActions(
    onConfirm: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = SoftWhite,
        border = BorderStroke(1.dp, CardBorderColor.copy(alpha = 0.6f)),
        shadowElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Rosewood,
                    contentColor = SoftWhite
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
            ) {
                Text(
                    text = "Lưu thay đổi",
                    color = SoftWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun WardrobeItemThumbnail(
    item: WardrobeItem,
    isLocked: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        val contentAlpha = if (isLocked) 0.60f else 1.0f

        when (item.category) {
            WardrobeCategory.FUR_SKIN -> {
                drawChibiFurSkinThumbnail(item, cx, cy, w, h, contentAlpha)
            }
            WardrobeCategory.HEADWEAR -> {
                drawHeadwearThumbnail(item.id, cx, cy, w, h, contentAlpha)
            }
            WardrobeCategory.NECKWEAR -> {
                drawNeckwearThumbnail(item.id, cx, cy, w, h, contentAlpha)
            }
            WardrobeCategory.BODYWEAR -> {
                drawBodywearThumbnail(item.id, cx, cy, w, h, contentAlpha)
            }
        }
    }
}

/**
 * Vẽ đầu mèo chibi đại diện cho từng loại Skin màu lông
 */
private fun DrawScope.drawChibiFurSkinThumbnail(
    item: WardrobeItem,
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    alpha: Float
) {
    val p = item.palette ?: CatColorPalette(
        bodyColor = item.previewPrimaryColor,
        patchColor = item.previewSecondaryColor,
        outlineColor = Color(0xFF5D3E44),
        innerEarColor = Color(0xFFF7B5B5),
        bellyColor = item.previewPrimaryColor,
        eyeColor = Color(0xFF384E32),
        noseColor = Color(0xFFE07A7A)
    )

    val r = w * 0.32f
    val strokeWidth = 2.2f

    drawOval(
        color = Color(0x18302020).copy(alpha = alpha * 0.5f),
        topLeft = Offset(cx - r * 0.85f, cy + r * 0.70f),
        size = Size(r * 1.7f, r * 0.45f)
    )

    // 1. Hai tai
    val leftEarPath = Path().apply {
        moveTo(cx - r * 0.75f, cy - r * 0.35f)
        lineTo(cx - r * 0.82f, cy - r * 1.12f)
        lineTo(cx - r * 0.22f, cy - r * 0.72f)
        close()
    }
    val rightEarPath = Path().apply {
        moveTo(cx + r * 0.22f, cy - r * 0.72f)
        lineTo(cx + r * 0.82f, cy - r * 1.12f)
        lineTo(cx + r * 0.75f, cy - r * 0.35f)
        close()
    }

    val rightEarColor = p.secondaryPatchColor ?: p.patchColor
    drawPath(leftEarPath, color = p.patchColor.copy(alpha = alpha))
    drawPath(leftEarPath, color = p.outlineColor.copy(alpha = alpha), style = Stroke(width = strokeWidth))
    drawPath(rightEarPath, color = rightEarColor.copy(alpha = alpha))
    drawPath(rightEarPath, color = p.outlineColor.copy(alpha = alpha), style = Stroke(width = strokeWidth))

    val leftInner = Path().apply {
        moveTo(cx - r * 0.65f, cy - r * 0.42f)
        lineTo(cx - r * 0.72f, cy - r * 0.95f)
        lineTo(cx - r * 0.30f, cy - r * 0.68f)
        close()
    }
    val rightInner = Path().apply {
        moveTo(cx + r * 0.30f, cy - r * 0.68f)
        lineTo(cx + r * 0.72f, cy - r * 0.95f)
        lineTo(cx + r * 0.65f, cy - r * 0.42f)
        close()
    }
    drawPath(leftInner, color = p.innerEarColor.copy(alpha = alpha))
    drawPath(rightInner, color = p.innerEarColor.copy(alpha = alpha))

    // 2. Đầu
    drawCircle(color = p.bodyColor.copy(alpha = alpha), radius = r, center = Offset(cx, cy))
    drawCircle(color = p.outlineColor.copy(alpha = alpha), radius = r, center = Offset(cx, cy), style = Stroke(width = strokeWidth))

    // 3. Mảng hoa văn đ
    when (item.id) {
        WardrobeRegistry.ID_FUR_SIAMESE -> {
            // Mèo Xiêm
            drawOval(
                color = p.patchColor.copy(alpha = alpha * 0.9f),
                topLeft = Offset(cx - r * 0.50f, cy - r * 0.40f),
                size = Size(r * 1.0f, r * 0.80f)
            )
        }
        WardrobeRegistry.ID_FUR_GINGER -> {
            // Mèo Cam
            listOf(-r * 0.20f, 0f, r * 0.20f).forEach { ox ->
                drawLine(
                    color = Color(0xFFD46228).copy(alpha = alpha),
                    start = Offset(cx + ox, cy - r * 0.75f),
                    end = Offset(cx + ox * 0.6f, cy - r * 0.48f),
                    strokeWidth = 2.4f,
                    cap = StrokeCap.Round
                )
            }
        }
        WardrobeRegistry.ID_FUR_CALICO -> {
            val brownPatch = Path().apply {
                moveTo(cx, cy - r * 0.95f)
                cubicTo(cx - r * 0.50f, cy - r * 0.90f, cx - r * 0.90f, cy - r * 0.40f, cx - r * 0.75f, cy)
                cubicTo(cx - r * 0.60f, cy + r * 0.30f, cx - r * 0.20f, cy + r * 0.40f, cx, cy + r * 0.20f)
                close()
            }
            drawPath(brownPatch, color = p.patchColor.copy(alpha = alpha * 0.92f))


            val greyPatch = Path().apply {
                moveTo(cx, cy - r * 0.95f)
                cubicTo(cx + r * 0.50f, cy - r * 0.90f, cx + r * 0.90f, cy - r * 0.40f, cx + r * 0.75f, cy)
                cubicTo(cx + r * 0.60f, cy + r * 0.30f, cx + r * 0.20f, cy + r * 0.40f, cx, cy + r * 0.20f)
                close()
            }
            val secCol = p.secondaryPatchColor ?: Color(0xFF6B6E78)
            drawPath(greyPatch, color = secCol.copy(alpha = alpha * 0.92f))
        }
    }

    // 4. Hai mắt
    val eyeOx = r * 0.34f
    val eyeOy = cy - r * 0.08f
    val eyeRadius = r * 0.13f

    if (item.id == WardrobeRegistry.ID_FUR_CALICO) {
        drawCircle(color = p.eyeColor.copy(alpha = alpha), radius = eyeRadius, center = Offset(cx - eyeOx, eyeOy))
        drawCircle(color = Color(0xFFF2C14E).copy(alpha = alpha), radius = eyeRadius, center = Offset(cx + eyeOx, eyeOy))
        // Đồng tử mắt phải
        drawCircle(color = Color(0xFF2B2024).copy(alpha = alpha), radius = eyeRadius * 0.5f, center = Offset(cx + eyeOx, eyeOy))
    } else {
        drawCircle(color = p.eyeColor.copy(alpha = alpha), radius = eyeRadius, center = Offset(cx - eyeOx, eyeOy))
        drawCircle(color = p.eyeColor.copy(alpha = alpha), radius = eyeRadius, center = Offset(cx + eyeOx, eyeOy))
    }

    // Đốm sáng phản chiếu mắt
    drawCircle(color = Color.White.copy(alpha = alpha * 0.85f), radius = eyeRadius * 0.35f, center = Offset(cx - eyeOx - 1.2f, eyeOy - 1.2f))
    drawCircle(color = Color.White.copy(alpha = alpha * 0.85f), radius = eyeRadius * 0.35f, center = Offset(cx + eyeOx - 1.2f, eyeOy - 1.2f))

    // 5. Mũi
    drawCircle(color = p.noseColor.copy(alpha = alpha), radius = r * 0.09f, center = Offset(cx, cy + r * 0.16f))

    // 6. Má
    drawCircle(color = Color(0x55FFA8A4).copy(alpha = alpha), radius = r * 0.16f, center = Offset(cx - r * 0.52f, cy + r * 0.18f))
    drawCircle(color = Color(0x55FFA8A4).copy(alpha = alpha), radius = r * 0.16f, center = Offset(cx + r * 0.52f, cy + r * 0.18f))
}

private fun DrawScope.drawHeadwearThumbnail(id: String, cx: Float, cy: Float, w: Float, h: Float, alpha: Float) {
    // Bóng đổ đáy
    drawOval(
        color = Color(0x15302020).copy(alpha = alpha * 0.5f),
        topLeft = Offset(cx - w * 0.30f, cy + h * 0.28f),
        size = Size(w * 0.60f, h * 0.16f)
    )

    when (id) {
        WardrobeRegistry.ID_HEAD_RIBBON -> {
            val rw = w * 0.26f
            val rh = h * 0.16f
            listOf(-1f, 1f).forEach { side ->
                val wing = Path().apply {
                    moveTo(cx, cy)
                    quadraticTo(cx + side * rw, cy - rh, cx + side * rw * 1.05f, cy)
                    quadraticTo(cx + side * rw, cy + rh, cx, cy)
                    close()
                }
                drawPath(wing, color = Color(0xFFFF8FAB).copy(alpha = alpha))
                drawPath(wing, color = Color(0xFF6B4247).copy(alpha = alpha), style = Stroke(width = 2.2f))
            }
            drawCircle(color = Color(0xFFFF6584).copy(alpha = alpha), radius = rw * 0.38f, center = Offset(cx, cy))
            drawCircle(color = Color(0xFF6B4247).copy(alpha = alpha), radius = rw * 0.38f, center = Offset(cx, cy), style = Stroke(width = 2f))
        }
        WardrobeRegistry.ID_HEAD_STRAW_HAT -> {
            val bw = w * 0.72f
            val bh = h * 0.18f
            drawOval(color = Color(0xFFEEDAA2).copy(alpha = alpha), topLeft = Offset(cx - bw / 2f, cy + h * 0.05f), size = Size(bw, bh))
            drawOval(color = Color(0xFF6B4247).copy(alpha = alpha), topLeft = Offset(cx - bw / 2f, cy + h * 0.05f), size = Size(bw, bh), style = Stroke(width = 2.2f))

            val dome = Path().apply {
                moveTo(cx - w * 0.24f, cy + h * 0.10f)
                cubicTo(cx - w * 0.22f, cy - h * 0.26f, cx + w * 0.22f, cy - h * 0.26f, cx + w * 0.24f, cy + h * 0.10f)
                close()
            }
            drawPath(dome, color = Color(0xFFEEDAA2).copy(alpha = alpha))
            drawPath(dome, color = Color(0xFF6B4247).copy(alpha = alpha), style = Stroke(width = 2.2f))
            drawRect(color = Color(0xFF8B5E3C).copy(alpha = alpha), topLeft = Offset(cx - w * 0.24f, cy + h * 0.02f), size = Size(w * 0.48f, h * 0.08f))
        }
        WardrobeRegistry.ID_HEAD_BERET -> {
            val beretPath = Path().apply {
                moveTo(cx - w * 0.34f, cy + h * 0.05f)
                cubicTo(cx - w * 0.38f, cy - h * 0.25f, cx + w * 0.34f, cy - h * 0.27f, cx + w * 0.34f, cy + h * 0.05f)
                cubicTo(cx + w * 0.22f, cy + h * 0.16f, cx - w * 0.22f, cy + h * 0.16f, cx - w * 0.34f, cy + h * 0.05f)
                close()
            }
            drawPath(beretPath, color = Color(0xFFE5B03B).copy(alpha = alpha))
            drawPath(beretPath, color = Color(0xFF664800).copy(alpha = alpha), style = Stroke(width = 2.2f))
            drawLine(color = Color(0xFF664800).copy(alpha = alpha), start = Offset(cx, cy - h * 0.20f), end = Offset(cx, cy - h * 0.32f), strokeWidth = 3f, cap = StrokeCap.Round)
        }
        WardrobeRegistry.ID_HEAD_FLOWER_CROWN -> {
            val vine = Path().apply {
                moveTo(cx - w * 0.35f, cy + h * 0.05f)
                quadraticTo(cx, cy - h * 0.10f, cx + w * 0.35f, cy + h * 0.05f)
            }
            drawPath(vine, color = Color(0xFF74A57F).copy(alpha = alpha), style = Stroke(width = 3.5f, cap = StrokeCap.Round))
            listOf(-w * 0.26f, -w * 0.13f, 0f, w * 0.13f, w * 0.26f).forEachIndexed { i, ox ->
                val oy = cy - (1f - (ox / (w * 0.35f)) * (ox / (w * 0.35f))) * (h * 0.08f)
                val petalColor = if (i % 2 == 0) Color.White else Color(0xFFFFD1DC)
                drawCircle(color = petalColor.copy(alpha = alpha), radius = w * 0.075f, center = Offset(cx + ox, oy))
                drawCircle(color = Color(0xFFFFD166).copy(alpha = alpha), radius = w * 0.038f, center = Offset(cx + ox, oy))
                drawCircle(color = Color(0xFF5D3E44).copy(alpha = alpha), radius = w * 0.075f, center = Offset(cx + ox, oy), style = Stroke(width = 1.5f))
            }
        }
        WardrobeRegistry.ID_HEAD_CROWN -> {
            val cw = w * 0.52f
            val ch = h * 0.34f
            val crownPath = Path().apply {
                moveTo(cx - cw / 2f, cy + ch / 2f)
                lineTo(cx - cw / 2f * 1.1f, cy - ch / 2f * 0.8f)
                lineTo(cx - cw * 0.20f, cy)
                lineTo(cx, cy - ch / 2f * 1.2f)
                lineTo(cx + cw * 0.20f, cy)
                lineTo(cx + cw / 2f * 1.1f, cy - ch / 2f * 0.8f)
                lineTo(cx + cw / 2f, cy + ch / 2f)
                close()
            }
            drawPath(crownPath, color = Color(0xFFFFC72C).copy(alpha = alpha))
            drawPath(crownPath, color = Color(0xFF7A5800).copy(alpha = alpha), style = Stroke(width = 2.2f))
            drawCircle(color = Color(0xFFE63946).copy(alpha = alpha), radius = w * 0.05f, center = Offset(cx, cy - ch / 2f * 1.2f))
        }
    }
}


private fun DrawScope.drawNeckwearThumbnail(id: String, cx: Float, cy: Float, w: Float, h: Float, alpha: Float) {
    drawOval(
        color = Color(0x15302020).copy(alpha = alpha * 0.5f),
        topLeft = Offset(cx - w * 0.30f, cy + h * 0.28f),
        size = Size(w * 0.60f, h * 0.16f)
    )

    when (id) {
        WardrobeRegistry.ID_NECK_BELL_COLLAR -> {
            drawOval(color = Color(0xFFC84B31).copy(alpha = alpha), topLeft = Offset(cx - w * 0.34f, cy - h * 0.12f), size = Size(w * 0.68f, h * 0.24f))
            drawOval(color = Color(0xFF6B4247).copy(alpha = alpha), topLeft = Offset(cx - w * 0.34f, cy - h * 0.12f), size = Size(w * 0.68f, h * 0.24f), style = Stroke(width = 2.2f))
            drawCircle(color = Color(0xFFFFD166).copy(alpha = alpha), radius = w * 0.13f, center = Offset(cx, cy + h * 0.14f))
            drawCircle(color = Color(0xFF7A5800).copy(alpha = alpha), radius = w * 0.13f, center = Offset(cx, cy + h * 0.14f), style = Stroke(width = 2.2f))
            drawCircle(color = Color(0xFF7A5800).copy(alpha = alpha), radius = w * 0.035f, center = Offset(cx, cy + h * 0.17f))
        }
        WardrobeRegistry.ID_NECK_WARM_SCARF -> {
            drawOval(color = Color(0xFFE07A5F).copy(alpha = alpha), topLeft = Offset(cx - w * 0.35f, cy - h * 0.15f), size = Size(w * 0.70f, h * 0.30f))
            drawOval(color = Color(0xFF5A2A20).copy(alpha = alpha), topLeft = Offset(cx - w * 0.35f, cy - h * 0.15f), size = Size(w * 0.70f, h * 0.30f), style = Stroke(width = 2.2f))
            val tailPath = Path().apply {
                moveTo(cx + w * 0.08f, cy)
                lineTo(cx + w * 0.24f, cy + h * 0.34f)
                lineTo(cx + w * 0.12f, cy + h * 0.36f)
                lineTo(cx - w * 0.04f, cy + h * 0.05f)
                close()
            }
            drawPath(tailPath, color = Color(0xFFD46246).copy(alpha = alpha))
            drawPath(tailPath, color = Color(0xFF5A2A20).copy(alpha = alpha), style = Stroke(width = 2.2f))
        }
        WardrobeRegistry.ID_NECK_BLUE_BOW -> {
            val bw = w * 0.24f
            val bh = h * 0.15f
            listOf(-1f, 1f).forEach { side ->
                val wing = Path().apply {
                    moveTo(cx, cy)
                    quadraticTo(cx + side * bw, cy - bh, cx + side * bw * 1.05f, cy)
                    quadraticTo(cx + side * bw, cy + bh, cx, cy)
                    close()
                }
                drawPath(wing, color = Color(0xFF76B9C4).copy(alpha = alpha))
                drawPath(wing, color = Color(0xFF2C565F).copy(alpha = alpha), style = Stroke(width = 2.2f))
            }
            drawCircle(color = Color(0xFF4A8E9A).copy(alpha = alpha), radius = bw * 0.36f, center = Offset(cx, cy))
            drawCircle(color = Color(0xFF2C565F).copy(alpha = alpha), radius = bw * 0.36f, center = Offset(cx, cy), style = Stroke(width = 2f))
        }
        WardrobeRegistry.ID_NECK_PEARL -> {
            val count = 5
            for (i in 0 until count) {
                val t = (i - (count - 1) / 2f) / ((count - 1) / 2f)
                val px = cx + t * (w * 0.28f)
                val py = cy + (1f - t * t * 0.5f) * (h * 0.10f)
                drawCircle(color = Color(0xFFFFFDF9).copy(alpha = alpha), radius = w * 0.06f, center = Offset(px, py))
                drawCircle(color = Color(0xFFC0B5A8).copy(alpha = alpha), radius = w * 0.06f, center = Offset(px, py), style = Stroke(width = 1.5f))
            }
        }
    }
}


private fun DrawScope.drawBodywearThumbnail(id: String, cx: Float, cy: Float, w: Float, h: Float, alpha: Float) {
    drawOval(
        color = Color(0x15302020).copy(alpha = alpha * 0.5f),
        topLeft = Offset(cx - w * 0.32f, cy + h * 0.28f),
        size = Size(w * 0.64f, h * 0.16f)
    )

    when (id) {
        WardrobeRegistry.ID_BODY_SWEATER -> {
            val sweaterPath = Path().apply {
                moveTo(cx - w * 0.30f, cy - h * 0.25f)
                lineTo(cx + w * 0.30f, cy - h * 0.25f)
                lineTo(cx + w * 0.38f, cy + h * 0.25f)
                lineTo(cx - w * 0.38f, cy + h * 0.25f)
                close()
            }
            drawPath(sweaterPath, color = Color(0xFFE8927C).copy(alpha = alpha))
            drawPath(sweaterPath, color = Color(0xFF8C3E2D).copy(alpha = alpha), style = Stroke(width = 2.2f))
            // Gân sọc áo len ấm áp
            drawLine(color = Color(0xFFFFF2D8).copy(alpha = alpha), start = Offset(cx - w * 0.24f, cy), end = Offset(cx + w * 0.24f, cy), strokeWidth = 2.8f)
            drawLine(color = Color(0xFFFFF2D8).copy(alpha = alpha), start = Offset(cx - w * 0.28f, cy + h * 0.12f), end = Offset(cx + w * 0.28f, cy + h * 0.12f), strokeWidth = 2.8f)
        }
        WardrobeRegistry.ID_BODY_OVERALLS -> {
            val ovPath = Path().apply {
                moveTo(cx - w * 0.24f, cy - h * 0.05f)
                lineTo(cx + w * 0.24f, cy - h * 0.05f)
                lineTo(cx + w * 0.26f, cy + h * 0.12f)
                cubicTo(cx + w * 0.36f, cy + h * 0.18f, cx + w * 0.36f, cy + h * 0.28f, cx, cy + h * 0.28f)
                cubicTo(cx - w * 0.36f, cy + h * 0.28f, cx - w * 0.36f, cy + h * 0.18f, cx - w * 0.26f, cy + h * 0.12f)
                close()
            }
            drawPath(ovPath, color = Color(0xFF5B8CB9).copy(alpha = alpha))
            drawPath(ovPath, color = Color(0xFF223A5E).copy(alpha = alpha), style = Stroke(width = 2.2f))
            drawLine(color = Color(0xFF5B8CB9).copy(alpha = alpha), start = Offset(cx - w * 0.16f, cy - h * 0.24f), end = Offset(cx - w * 0.16f, cy - h * 0.05f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawLine(color = Color(0xFF5B8CB9).copy(alpha = alpha), start = Offset(cx + w * 0.16f, cy - h * 0.24f), end = Offset(cx + w * 0.16f, cy - h * 0.05f), strokeWidth = 5f, cap = StrokeCap.Round)
            drawCircle(color = Color(0xFFFFD166).copy(alpha = alpha), radius = w * 0.038f, center = Offset(cx - w * 0.16f, cy - h * 0.02f))
            drawCircle(color = Color(0xFFFFD166).copy(alpha = alpha), radius = w * 0.038f, center = Offset(cx + w * 0.16f, cy - h * 0.02f))
        }
        WardrobeRegistry.ID_BODY_ROYAL_CAPE -> {
            val capePath = Path().apply {
                moveTo(cx - w * 0.22f, cy - h * 0.20f)
                lineTo(cx + w * 0.22f, cy - h * 0.20f)
                cubicTo(cx + w * 0.40f, cy + h * 0.10f, cx + w * 0.38f, cy + h * 0.28f, cx, cy + h * 0.24f)
                cubicTo(cx - w * 0.38f, cy + h * 0.28f, cx - w * 0.40f, cy + h * 0.10f, cx - w * 0.22f, cy - h * 0.20f)
                close()
            }
            drawPath(capePath, color = Color(0xFF9E2A2B).copy(alpha = alpha))
            drawPath(capePath, color = Color(0xFF4D0E10).copy(alpha = alpha), style = Stroke(width = 2.2f))
            drawOval(color = Color(0xFFFFF8F0).copy(alpha = alpha), topLeft = Offset(cx - w * 0.24f, cy - h * 0.24f), size = Size(w * 0.48f, h * 0.12f))
            drawCircle(color = Color(0xFFFFD166).copy(alpha = alpha), radius = w * 0.045f, center = Offset(cx, cy - h * 0.16f))
        }
    }
}
