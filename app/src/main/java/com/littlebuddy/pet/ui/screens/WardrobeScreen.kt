package com.littlebuddy.pet.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
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
import com.littlebuddy.pet.ui.theme.BlushPetal
import com.littlebuddy.pet.ui.theme.CardBorderColor
import com.littlebuddy.pet.ui.theme.MidnightLagoon
import com.littlebuddy.pet.ui.theme.MistySky
import com.littlebuddy.pet.ui.theme.Rosewood
import com.littlebuddy.pet.ui.theme.SageLeaf
import com.littlebuddy.pet.ui.theme.ShadowTint
import com.littlebuddy.pet.ui.theme.SoftWhite
import com.littlebuddy.pet.ui.theme.SurfaceCream
import com.littlebuddy.pet.ui.theme.VanillaCream
import com.littlebuddy.pet.wardrobe.CatColorPalette
import com.littlebuddy.pet.wardrobe.EquippedWardrobe
import com.littlebuddy.pet.wardrobe.WardrobeCategory
import com.littlebuddy.pet.wardrobe.WardrobeItem
import com.littlebuddy.pet.wardrobe.WardrobeRegistry

/**
 * Màn hình Tủ đồ & Cá nhân hóa thú cưng (Wardrobe Screen).
 *
 * Tính năng chính:
 * 1. Live Preview: Xem trước toàn bộ trang phục đang thử (Màu lông, Mũ/Nơ, Vòng/Khăn, Áo)
 *    trên nhân vật mèo hoạt họa vector với nhịp thở phập phồng sống động.
 * 2. 4 Danh mục trang bị:
 *    - Màu lông (Fur Skins)
 *    - Nơ & Mũ cài đầu (Headwear)
 *    - Vòng cổ & Khăn quàng (Neckwear)
 *    - Trang phục thân người (Bodywear)
 * 3. 100% Vector Canvas Thumbnails: Không sử dụng emoji làm icon trang bị cuối cùng.
 * 4. Cơ chế Xem trước & Xác nhận (Preview vs Confirm):
 *    - Đang thử: Phản hồi tức thì trên Live Preview.
 *    - Xác nhận: Lưu vào bộ nhớ cục bộ (SharedPreferences), giữ nguyên cấp độ, tên, chỉ số.
 *    - Hủy / Quay lại: Khôi phục lại bộ đồ đang mặc ban đầu mà không mất mát dữ liệu.
 * 5. Mở khóa theo Cấp độ (Level Locks) với biểu tượng 🔒 rõ ràng.
 */
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

    // Hoạt ảnh thở phập phồng nhẹ nhàng trong phòng thử đồ
    val infiniteTransition = rememberInfiniteTransition(label = "wardrobe_preview_anim")
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
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "tail_wag"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(VanillaCream)
    ) {
        // ── 1. THANH TIÊU ĐỀ TRÊN CÙNG (TOP BAR) ──────────────────────────
        Surface(
            color = SoftWhite,
            shadowElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Quay lại phòng",
                            tint = MidnightLagoon
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "Tủ Đồ Little Buddy",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MidnightLagoon
                        )
                    }
                }

                // Nút Đặt lại về mặc định
                OutlinedButton(
                    onClick = onResetDefault,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Rosewood
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Mặc định",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Mặc định", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // ── 2. KHU VỰC XEM TRƯỚC NHÂN VẬT (LIVE PREVIEW SHOWCASE) ─────────
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceCream),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .border(1.dp, CardBorderColor, RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sân khấu xem trước mèo trực tiếp
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PetCanvas(
                        petState = petState,
                        actionState = PetActionState.IDLE,
                        breatheScale = breatheScale,
                        tailWagAngle = tailWagAngle,
                        wardrobeOverride = previewWardrobe,
                        modifier = Modifier.size(200.dp)
                    )
                }

                // Tóm tắt các món đang thử nghiệm
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val furName = previewWardrobe.getFurSkin().name
                    val headName = previewWardrobe.getHeadwear()?.name
                    val neckName = previewWardrobe.getNeckwear()?.name
                    val bodyName = previewWardrobe.getBodywear()?.name

                    val summary = buildList {
                        add(furName)
                        headName?.let { add(it) }
                        neckName?.let { add(it) }
                        bodyName?.let { add(it) }
                    }.joinToString(" • ")


                }

                // Nút tháo phụ kiện của danh mục đang chọn (nếu có trang bị)
                val currentCategoryItem = previewWardrobe.getItemForCategory(selectedCategory)
                val canUnequip = selectedCategory != WardrobeCategory.FUR_SKIN && currentCategoryItem != null
                if (canUnequip) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tháo ${selectedCategory.displayName} này ✕",
                        style = MaterialTheme.typography.labelSmall,
                        color = Rosewood,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onUnequipCategory(selectedCategory) }
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        // ── 3. THANH TABS DANH MỤC PHỤ KIỆN (CATEGORY TABS) ───────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WardrobeCategory.values().forEach { category ->
                val isSelected = category == selectedCategory
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) Rosewood else SoftWhite,
                    shadowElevation = if (isSelected) 3.dp else 1.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { selectedCategory = category }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = category.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) SoftWhite else MidnightLagoon
                        )
                    }
                }
            }
        }

        // ── 4. LƯỚI VẬT PHẨM TRONG DANH MỤC (ITEMS GRID) ──────────────────
        val itemsList = WardrobeRegistry.getItemsForCategory(selectedCategory)

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(itemsList, key = { it.id }) { item ->
                val isLocked = petState.level < item.levelRequired
                val isPreviewEquipped = previewWardrobe.isEquipped(item.id)
                val isActuallyEquipped = petState.wardrobe.isEquipped(item.id)

                WardrobeItemCard(
                    item = item,
                    isLocked = isLocked,
                    isPreviewEquipped = isPreviewEquipped,
                    isActuallyEquipped = isActuallyEquipped,
                    onClick = { onSelectItem(item) }
                )
            }
        }

        // ── 5. THANH THAO TÁC XÁC NHẬN DƯỚI CÙNG (BOTTOM ACTION BAR) ─────
        Surface(
            color = SoftWhite,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Nút Hủy / Quay lại
                OutlinedButton(
                    onClick = onBack,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MidnightLagoon
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Quay lại",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                // Nút Xác nhận lưu trang phục
                Button(
                    onClick = onConfirm,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Rosewood,
                        contentColor = SoftWhite
                    ),
                    modifier = Modifier
                        .weight(1.4f)
                        .height(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Xác nhận",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Xác nhận mặc",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

/**
 * Thẻ hiển thị một vật phẩm trang bị trong tủ đồ.
 * Sử dụng 100% Vector Canvas Drawing cho hình thumbnail, tuyệt đối không dùng emoji.
 */
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
        else -> CardBorderColor
    }
    val borderWidth = if (isPreviewEquipped) 2.dp else 1.dp
    val bgColor = when {
        isLocked -> SurfaceCream.copy(alpha = 0.65f)
        isPreviewEquipped -> BlushPetal.copy(alpha = 0.35f)
        else -> SoftWhite
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPreviewEquipped) 4.dp else 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Khung hình Thumbnail vẽ bằng Compose Canvas
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(VanillaCream.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                WardrobeItemThumbnail(
                    item = item,
                    modifier = Modifier.size(60.dp)
                )

                // Biểu tượng khóa cấp độ
                if (isLocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.35f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = "Khóa",
                                tint = SoftWhite,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Lv.${item.levelRequired}",
                                color = SoftWhite,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Tên vật phẩm
            Text(
                text = item.name,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isLocked) MidnightLagoon.copy(alpha = 0.5f) else MidnightLagoon,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Nhãn trạng thái (Đang thử / Đang mặc / Khóa)
            Spacer(modifier = Modifier.height(2.dp))
            when {
                isPreviewEquipped -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Rosewood,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "Đang thử",
                            color = SoftWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                isActuallyEquipped -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SageLeaf,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = "Đang mặc",
                            color = SoftWhite,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                isLocked -> {
                    Text(
                        text = "Cần Cấp ${item.levelRequired}",
                        color = MidnightLagoon.copy(alpha = 0.5f),
                        fontSize = 9.sp
                    )
                }
                else -> {
                    Text(
                        text = "Mở khóa",
                        color = SageLeaf,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Thumbnail vẽ bằng Compose Canvas vector cho từng vật phẩm.
 * Chuẩn đồ họa hoạt hình vector hoàn toàn, không phụ thuộc vào emoji.
 */
@Composable
private fun WardrobeItemThumbnail(
    item: WardrobeItem,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f

        when (item.category) {
            WardrobeCategory.FUR_SKIN -> {
                // Vẽ đầu mèo chibi nhỏ với bảng màu của Skin
                val p = item.palette ?: CatColorPalette(
                    bodyColor = item.previewPrimaryColor,
                    patchColor = item.previewSecondaryColor,
                    outlineColor = Color(0xFF7E5657),
                    innerEarColor = item.previewSecondaryColor,
                    bellyColor = item.previewPrimaryColor,
                    eyeColor = Color(0xFF5D3E44),
                    noseColor = Color(0xFF5D3E44)
                )

                val headRadius = w * 0.32f
                val earW = headRadius * 0.45f

                // 2 Tai mèo
                listOf(-1f, 1f).forEach { side ->
                    val earPath = Path().apply {
                        moveTo(cx + side * headRadius * 0.3f, cy - headRadius * 0.6f)
                        lineTo(cx + side * headRadius * 0.85f, cy - headRadius * 1.15f)
                        lineTo(cx + side * headRadius * 0.95f, cy - headRadius * 0.4f)
                        close()
                    }
                    drawPath(earPath, color = p.patchColor)
                    drawPath(earPath, color = p.outlineColor, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
                }

                // Đầu tròn
                drawCircle(color = p.bodyColor, radius = headRadius, center = Offset(cx, cy))
                drawCircle(color = p.outlineColor, radius = headRadius, center = Offset(cx, cy), style = Stroke(width = 2.5f))

                // Mặt nạ đốm
                drawOval(
                    color = p.patchColor,
                    topLeft = Offset(cx - headRadius * 0.55f, cy - headRadius * 0.45f),
                    size = Size(headRadius * 1.1f, headRadius * 0.85f)
                )

                // Mắt hạt đậu
                val eyeOx = headRadius * 0.32f
                drawCircle(color = p.eyeColor, radius = headRadius * 0.12f, center = Offset(cx - eyeOx, cy - headRadius * 0.08f))
                drawCircle(color = p.eyeColor, radius = headRadius * 0.12f, center = Offset(cx + eyeOx, cy - headRadius * 0.08f))

                // Mũi nhỏ
                drawCircle(color = p.noseColor, radius = headRadius * 0.09f, center = Offset(cx, cy + headRadius * 0.14f))
            }

            WardrobeCategory.HEADWEAR -> {
                when (item.id) {
                    "head_ribbon" -> {
                        // Nơ hồng
                        val rw = w * 0.22f
                        val rh = h * 0.14f
                        listOf(-1f, 1f).forEach { side ->
                            val wing = Path().apply {
                                moveTo(cx, cy)
                                quadraticTo(cx + side * rw, cy - rh, cx + side * rw * 1.05f, cy)
                                quadraticTo(cx + side * rw, cy + rh, cx, cy)
                                close()
                            }
                            drawPath(wing, color = Color(0xFFFF8FAB))
                            drawPath(wing, color = Color(0xFF6B4247), style = Stroke(width = 2.2f))
                        }
                        drawCircle(color = Color(0xFFFF6584), radius = rw * 0.35f, center = Offset(cx, cy))
                        drawCircle(color = Color(0xFF6B4247), radius = rw * 0.35f, center = Offset(cx, cy), style = Stroke(width = 2f))
                    }
                    "head_straw_hat" -> {
                        // Mũ cói
                        val bw = w * 0.70f
                        val bh = h * 0.18f
                        drawOval(color = Color(0xFFEEDAA2), topLeft = Offset(cx - bw / 2f, cy + h * 0.05f), size = Size(bw, bh))
                        drawOval(color = Color(0xFF6B4247), topLeft = Offset(cx - bw / 2f, cy + h * 0.05f), size = Size(bw, bh), style = Stroke(width = 2.2f))

                        val dome = Path().apply {
                            moveTo(cx - w * 0.22f, cy + h * 0.12f)
                            cubicTo(cx - w * 0.20f, cy - h * 0.24f, cx + w * 0.20f, cy - h * 0.24f, cx + w * 0.22f, cy + h * 0.12f)
                            close()
                        }
                        drawPath(dome, color = Color(0xFFEEDAA2))
                        drawPath(dome, color = Color(0xFF6B4247), style = Stroke(width = 2.2f))
                        // Ruy băng
                        drawRect(color = Color(0xFF8B5E3C), topLeft = Offset(cx - w * 0.22f, cy + h * 0.04f), size = Size(w * 0.44f, h * 0.08f))
                    }
                    "head_beret" -> {
                        // Mũ nồi
                        val beretPath = Path().apply {
                            moveTo(cx - w * 0.32f, cy + h * 0.05f)
                            cubicTo(cx - w * 0.36f, cy - h * 0.25f, cx + w * 0.32f, cy - h * 0.27f, cx + w * 0.32f, cy + h * 0.05f)
                            cubicTo(cx + w * 0.20f, cy + h * 0.16f, cx - w * 0.20f, cy + h * 0.16f, cx - w * 0.32f, cy + h * 0.05f)
                            close()
                        }
                        drawPath(beretPath, color = Color(0xFF8E4A49))
                        drawPath(beretPath, color = Color(0xFF4A2525), style = Stroke(width = 2.2f))
                        drawLine(color = Color(0xFF4A2525), start = Offset(cx, cy - h * 0.20f), end = Offset(cx, cy - h * 0.30f), strokeWidth = 3f)
                    }
                    "head_flower_crown" -> {
                        // Vòng hoa
                        val vine = Path().apply {
                            moveTo(cx - w * 0.35f, cy + h * 0.05f)
                            quadraticTo(cx, cy - h * 0.10f, cx + w * 0.35f, cy + h * 0.05f)
                        }
                        drawPath(vine, color = Color(0xFF74A57F), style = Stroke(width = 3.5f, cap = StrokeCap.Round))
                        listOf(-w * 0.26f, -w * 0.13f, 0f, w * 0.13f, w * 0.26f).forEachIndexed { i, ox ->
                            val oy = cy - (1f - (ox / (w * 0.35f)) * (ox / (w * 0.35f))) * (h * 0.08f)
                            val petalColor = if (i % 2 == 0) Color.White else Color(0xFFFFD1DC)
                            drawCircle(color = petalColor, radius = w * 0.07f, center = Offset(cx + ox, oy))
                            drawCircle(color = Color(0xFFFFD166), radius = w * 0.035f, center = Offset(cx + ox, oy))
                            drawCircle(color = Color(0xFF5D3E44), radius = w * 0.07f, center = Offset(cx + ox, oy), style = Stroke(width = 1.5f))
                        }
                    }
                    "head_crown" -> {
                        // Vương miện hoàng gia
                        val cw = w * 0.50f
                        val ch = h * 0.32f
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
                        drawPath(crownPath, color = Color(0xFFFFC72C))
                        drawPath(crownPath, color = Color(0xFF7A5800), style = Stroke(width = 2.2f))
                        // Viên ruby
                        drawCircle(color = Color(0xFFE63946), radius = w * 0.045f, center = Offset(cx, cy - ch / 2f * 1.2f))
                    }
                }
            }

            WardrobeCategory.NECKWEAR -> {
                when (item.id) {
                    "neck_bell_collar" -> {
                        // Vòng cổ đỏ + chuông vàng
                        drawOval(color = Color(0xFFE76F51), topLeft = Offset(cx - w * 0.32f, cy - h * 0.12f), size = Size(w * 0.64f, h * 0.24f))
                        drawOval(color = Color(0xFF6B4247), topLeft = Offset(cx - w * 0.32f, cy - h * 0.12f), size = Size(w * 0.64f, h * 0.24f), style = Stroke(width = 2.2f))
                        // Chuông vàng
                        drawCircle(color = Color(0xFFFFD166), radius = w * 0.12f, center = Offset(cx, cy + h * 0.14f))
                        drawCircle(color = Color(0xFF7A5800), radius = w * 0.12f, center = Offset(cx, cy + h * 0.14f), style = Stroke(width = 2.2f))
                        drawCircle(color = Color(0xFF7A5800), radius = w * 0.035f, center = Offset(cx, cy + h * 0.17f))
                    }
                    "neck_warm_scarf" -> {
                        // Khăn quàng len
                        drawOval(color = Color(0xFFE07A5F), topLeft = Offset(cx - w * 0.34f, cy - h * 0.15f), size = Size(w * 0.68f, h * 0.30f))
                        drawOval(color = Color(0xFF5A2A20), topLeft = Offset(cx - w * 0.34f, cy - h * 0.15f), size = Size(w * 0.68f, h * 0.30f), style = Stroke(width = 2.2f))
                        // Đuôi khăn
                        val tailPath = Path().apply {
                            moveTo(cx + w * 0.08f, cy)
                            lineTo(cx + w * 0.22f, cy + h * 0.32f)
                            lineTo(cx + w * 0.10f, cy + h * 0.34f)
                            lineTo(cx - w * 0.04f, cy + h * 0.05f)
                            close()
                        }
                        drawPath(tailPath, color = Color(0xFFD46246))
                        drawPath(tailPath, color = Color(0xFF5A2A20), style = Stroke(width = 2.2f))
                    }
                    "neck_blue_bow" -> {
                        // Nơ xanh
                        val bw = w * 0.22f
                        val bh = h * 0.14f
                        listOf(-1f, 1f).forEach { side ->
                            val wing = Path().apply {
                                moveTo(cx, cy)
                                quadraticTo(cx + side * bw, cy - bh, cx + side * bw * 1.05f, cy)
                                quadraticTo(cx + side * bw, cy + bh, cx, cy)
                                close()
                            }
                            drawPath(wing, color = Color(0xFF88C9D4))
                            drawPath(wing, color = Color(0xFF33626C), style = Stroke(width = 2.2f))
                        }
                        drawCircle(color = Color(0xFF569DAA), radius = bw * 0.35f, center = Offset(cx, cy))
                        drawCircle(color = Color(0xFF33626C), radius = bw * 0.35f, center = Offset(cx, cy), style = Stroke(width = 2f))
                    }
                    "neck_pearl" -> {
                        // Vòng ngọc trai
                        val count = 5
                        for (i in 0 until count) {
                            val t = (i - (count - 1) / 2f) / ((count - 1) / 2f)
                            val px = cx + t * (w * 0.26f)
                            val py = cy + (1f - t * t * 0.5f) * (h * 0.10f)
                            drawCircle(color = Color(0xFFFFFDF9), radius = w * 0.055f, center = Offset(px, py))
                            drawCircle(color = Color(0xFFC0B5A8), radius = w * 0.055f, center = Offset(px, py), style = Stroke(width = 1.5f))
                        }
                    }
                }
            }

            WardrobeCategory.BODYWEAR -> {
                when (item.id) {
                    "body_sweater" -> {
                        // Áo len xanh mint
                        val sweaterPath = Path().apply {
                            moveTo(cx - w * 0.28f, cy - h * 0.25f)
                            lineTo(cx + w * 0.28f, cy - h * 0.25f)
                            lineTo(cx + w * 0.36f, cy + h * 0.25f)
                            lineTo(cx - w * 0.36f, cy + h * 0.25f)
                            close()
                        }
                        drawPath(sweaterPath, color = Color(0xFFB5D5C5))
                        drawPath(sweaterPath, color = Color(0xFF4A6B5D), style = Stroke(width = 2.2f))
                        // Cổ áo & gân
                        drawLine(color = Color(0xFF99C2AC), start = Offset(cx - w * 0.22f, cy), end = Offset(cx + w * 0.22f, cy), strokeWidth = 2.5f)
                        drawLine(color = Color(0xFF99C2AC), start = Offset(cx - w * 0.26f, cy + h * 0.12f), end = Offset(cx + w * 0.26f, cy + h * 0.12f), strokeWidth = 2.5f)
                    }
                    "body_overalls" -> {
                        // Quần yếm bò
                        val ovPath = Path().apply {
                            moveTo(cx - w * 0.28f, cy - h * 0.05f)
                            lineTo(cx + w * 0.28f, cy - h * 0.05f)
                            lineTo(cx + w * 0.32f, cy + h * 0.28f)
                            lineTo(cx - w * 0.32f, cy + h * 0.28f)
                            close()
                        }
                        drawPath(ovPath, color = Color(0xFF6C9BCF))
                        drawPath(ovPath, color = Color(0xFF2C4A70), style = Stroke(width = 2.2f))
                        // Dây đeo yếm
                        drawLine(color = Color(0xFF6C9BCF), start = Offset(cx - w * 0.18f, cy - h * 0.28f), end = Offset(cx - w * 0.18f, cy - h * 0.05f), strokeWidth = 6f)
                        drawLine(color = Color(0xFF6C9BCF), start = Offset(cx + w * 0.18f, cy - h * 0.28f), end = Offset(cx + w * 0.18f, cy - h * 0.05f), strokeWidth = 6f)
                        drawCircle(color = Color(0xFFFFD166), radius = w * 0.035f, center = Offset(cx - w * 0.18f, cy - h * 0.02f))
                        drawCircle(color = Color(0xFFFFD166), radius = w * 0.035f, center = Offset(cx + w * 0.18f, cy - h * 0.02f))
                    }
                    "body_royal_cape" -> {
                        // Áo choàng hoàng gia
                        val capePath = Path().apply {
                            moveTo(cx - w * 0.20f, cy - h * 0.20f)
                            lineTo(cx + w * 0.20f, cy - h * 0.20f)
                            cubicTo(cx + w * 0.38f, cy + h * 0.10f, cx + w * 0.36f, cy + h * 0.28f, cx, cy + h * 0.24f)
                            cubicTo(cx - w * 0.36f, cy + h * 0.28f, cx - w * 0.38f, cy + h * 0.10f, cx - w * 0.20f, cy - h * 0.20f)
                            close()
                        }
                        drawPath(capePath, color = Color(0xFF9E2A2B))
                        drawPath(capePath, color = Color(0xFF4D0E10), style = Stroke(width = 2.2f))
                        // Cổ áo lông trắng
                        drawOval(color = Color(0xFFFFF8F0), topLeft = Offset(cx - w * 0.22f, cy - h * 0.24f), size = Size(w * 0.44f, h * 0.12f))
                        drawCircle(color = Color(0xFFFFD166), radius = w * 0.04f, center = Offset(cx, cy - h * 0.16f))
                    }
                }
            }
        }
    }
}
