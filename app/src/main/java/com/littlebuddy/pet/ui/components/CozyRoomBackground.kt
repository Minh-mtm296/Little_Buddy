package com.littlebuddy.pet.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp

// ═════════════════════════════════════════════════════════════════════════════
// BẢNG MÀU CĂN PHÒNG ẤM CÚNG (CHUẨN THEO HÌNH MẪU BÊN PHẢI)
// ═════════════════════════════════════════════════════════════════════════════
private val WallStripeLight = Color(0xFFFCF6E6)
private val WallStripeDark = Color(0xFFF0EAD8)
private val WainscotBg = Color(0xFFEAE2CE)
private val WainscotLine = Color(0xFFCBBBA0)

private val FloorBase = Color(0xFFDEC3A3)
private val FloorPattern = Color(0xFFD0B18E)
private val FloorFlower = Color(0xFFEBD4B7)

private val WoodDark = Color(0xFF6E4B35)
private val WoodMedium = Color(0xFF96694C)
private val WoodLight = Color(0xFFB88764)
private val WoodHighlight = Color(0xFFD4A37E)

private val RugOuter = Color(0xFF91B296)
private val RugMain = Color(0xFFACC9AE)
private val RugInner = Color(0xFFBFDCC1)

private val LeafGreenDark = Color(0xFF5D8249)
private val LeafGreenMid = Color(0xFF7FA668)
private val LeafGreenLight = Color(0xFFA3C686)

private val PlaidPink = Color(0xFFE69588)
private val PlaidYellow = Color(0xFFF5D182)
private val PlaidCream = Color(0xFFFFF7E4)
private val PlaidGreen = Color(0xFF9EB584)

/**
 * Background căn phòng ấm cúng vẽ bằng Compose Canvas theo đúng bố cục hình mẫu bên phải:
 * - Tường kẻ sọc kem & phào chân tường cổ điển
 * - Sàn gạch bông màu kem nâu ấm
 * - Cửa sổ gỗ mái vòm nhọn bên trái nhìn ra rừng thu + rèm ren + chậu cây rủ
 * - Giường gỗ tai mèo trải chăn caro ở góc trái dưới
 * - Đèn cây cổ điển tỏa ánh sáng vàng ấm áp (có hiệu ứng lung linh nhẹ)
 * - Kệ gỗ cốc hoa, khung tranh nấm, khung tranh bí ngô & bộ 3 tranh nhỏ trên tường
 * - Bàn tủ gỗ phủ khăn đỏ + Ghế bành sofa caro hồng ở góc phải dưới
 * - Thảm tròn xanh sage điểm hoa nhỏ dưới sàn nơi mèo đứng
 */
@Composable
fun CozyRoomBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "roomLife")

    // Hạt bụi bay chậm và chùm nắng lung linh nhẹ nhàng
    val dustPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dustPhase"
    )
    val sunPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sunPulse"
    )

    Box(modifier = modifier) {
        // Lớp tĩnh: không đọc state animate → Compose chỉ vẽ lại khi đổi kích thước
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val horizonY = h * 0.40f // Hạ chân tường xuống 40%, sàn rộng chiếm 60%

            // 1. Tường sọc dọc & Phào chân tường + Đèn dây đom đóm ấm áp
            drawStripedWallAndWainscot(w, h, horizonY)

            // 2. Sàn gạch bông hoa văn rộng rãi
            drawPatternedTileFloor(w, h, horizonY)

            // 3. Thảm tròn xanh sage viền hoa ở giữa sàn nơi mèo đứng
            drawRoundFloralRug(w, h)

            // 4. Cửa sổ gỗ vòm nhọn mùa thu + Rèm ren (Góc trên trái)
            drawGothicAutumnWindow(w, h)

            // 5. Cụm đồ trang trí Gallery Wall & Kệ gỗ gắn kết
            drawWallFramesAndShelf(w, h)

            // 6. Bàn tủ gỗ & Ghế sofa ở góc phải sàn
            drawRightCabinetAndArmchair(w, h)

            // 7. Đèn cây cổ điển (thân đèn, không glow)
            drawVintageFloorLamp(w, h)

            // 8. Giường gỗ tai mèo & Chăn caro (Góc dưới trái)
            drawCatEarWoodenBed(w, h)

            // 9. Đồ vật trang trí trên sàn (Cột cào móng, chậu cây)
            drawFloorAccessories(w, h)
        }

        // Lớp động: chỉ nắng, quầng đèn, bụi
        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height

            // Chùm nắng ấm áp từ cửa sổ chiếu xiên qua phòng
            drawAutumnSunbeam(w, h, sunPulse)

            // Quầng sáng đèn cây lung linh
            drawLampGlow(w, h, sunPulse)

            // Hạt bụi vàng lơ lửng bay chậm trong chùm nắng
            drawFloatingDustMotes(w, h, dustPhase, sunPulse)
        }
    }
}

// ── 1. TƯỜNG KẺ SỌC & PHÀO CHÂN TƯỜNG ───────────────────────────────────────

private fun DrawScope.drawStripedWallAndWainscot(w: Float, h: Float, horizonY: Float) {
    val wainscotTop = h * 0.31f
    val stripeCount = 13
    val stripeW = w / stripeCount

    for (i in 0 until stripeCount) {
        drawRect(
            color = if (i % 2 == 0) WallStripeLight else WallStripeDark,
            topLeft = Offset(i * stripeW, 0f),
            size = Size(stripeW + 1f, wainscotTop)
        )
    }

    // Phào chân tường (Wainscoting)
    drawRect(
        color = WainscotBg,
        topLeft = Offset(0f, wainscotTop),
        size = Size(w, horizonY - wainscotTop)
    )
    // Thanh gờ trên phào
    drawLine(
        color = WainscotLine,
        start = Offset(0f, wainscotTop),
        end = Offset(w, wainscotTop),
        strokeWidth = 3.5.dp.toPx()
    )
    drawLine(
        color = WallStripeLight,
        start = Offset(0f, wainscotTop + 3.5.dp.toPx()),
        end = Offset(w, wainscotTop + 3.5.dp.toPx()),
        strokeWidth = 2.dp.toPx()
    )
    // Hoa văn vòm nhỏ trên phào chân tường
    val archCount = 10
    val archW = w / archCount
    for (i in 0 until archCount) {
        val ax = i * archW + archW * 0.12f
        val aw = archW * 0.76f
        val ay = wainscotTop + (horizonY - wainscotTop) * 0.20f
        val ah = (horizonY - wainscotTop) * 0.65f
        drawRoundRect(
            color = WainscotLine.copy(alpha = 0.55f),
            topLeft = Offset(ax, ay),
            size = Size(aw, ah),
            cornerRadius = CornerRadius(aw * 0.4f, aw * 0.4f),
            style = Stroke(width = 2.dp.toPx())
        )
    }
    // Đường chân tường giáp sàn
    drawLine(
        color = WoodMedium.copy(alpha = 0.6f),
        start = Offset(0f, horizonY),
        end = Offset(w, horizonY),
        strokeWidth = 4.dp.toPx()
    )

    // Đèn dây đom đóm (Fairy string lights) uốn lượn ấm áp dọc mép tường
    val bulbCount = 7
    val startX = w * 0.32f
    val endX = w * 0.95f
    val wireY = h * 0.04f
    for (b in 0 until bulbCount) {
        val bx = startX + (endX - startX) * (b.toFloat() / (bulbCount - 1))
        val sag = kotlin.math.sin(b * 1.05f) * 5.dp.toPx()
        val by = wireY + sag
        if (b > 0) {
            val prevX = startX + (endX - startX) * ((b - 1).toFloat() / (bulbCount - 1))
            val prevSag = kotlin.math.sin((b - 1) * 1.05f) * 5.dp.toPx()
            val prevY = wireY + prevSag
            drawLine(
                color = WoodDark.copy(alpha = 0.4f),
                start = Offset(prevX, prevY),
                end = Offset(bx, by),
                strokeWidth = 1.5.dp.toPx()
            )
        }
        // Quả cầu đèn phát sáng vàng ấm
        drawCircle(color = PlaidYellow.copy(alpha = 0.35f), radius = 8.dp.toPx(), center = Offset(bx, by + 4.dp.toPx()))
        drawCircle(color = Color(0xFFFFEA7A), radius = 3.5.dp.toPx(), center = Offset(bx, by + 4.dp.toPx()))
    }
}

// ── 2. SÀN GẠCH BÔNG HOA VĂN ────────────────────────────────────────────────

private fun DrawScope.drawPatternedTileFloor(w: Float, h: Float, horizonY: Float) {
    drawRect(
        color = FloorBase,
        topLeft = Offset(0f, horizonY),
        size = Size(w, h - horizonY)
    )

    val cols = 5
    val rows = 5
    val cellW = w / cols
    val cellH = (h - horizonY) / rows

    for (r in 0..rows) {
        for (c in 0..cols) {
            val cx = c * cellW
            val cy = horizonY + r * cellH

            // Vòng cung gạch đan nhau
            drawCircle(
                color = FloorPattern.copy(alpha = 0.40f),
                radius = cellW * 0.48f,
                center = Offset(cx, cy),
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Hoa gạch 8 cánh ở tâm mỗi giao điểm
            drawCircle(
                color = FloorFlower,
                radius = cellW * 0.12f,
                center = Offset(cx, cy)
            )
            drawCircle(
                color = FloorPattern,
                radius = cellW * 0.12f,
                center = Offset(cx, cy),
                style = Stroke(width = 1.8.dp.toPx())
            )
            drawCircle(
                color = FloorBase,
                radius = cellW * 0.045f,
                center = Offset(cx, cy)
            )
        }
    }
}

// ── 3. THẢM TRÒN XANH SAGE VIỀN HOA ─────────────────────────────────────────

private fun DrawScope.drawRoundFloralRug(w: Float, h: Float) {
    val rugCx = w * 0.50f
    val rugCy = h * 0.67f
    val rugW = w * 0.68f
    val rugH = h * 0.28f

    // Bóng đổ thảm
    drawOval(
        color = WoodDark.copy(alpha = 0.18f),
        topLeft = Offset(rugCx - rugW * 0.51f, rugCy - rugH * 0.48f + 5.dp.toPx()),
        size = Size(rugW * 1.02f, rugH)
    )

    // Vành ngoài xanh đậm
    drawOval(
        color = RugOuter,
        topLeft = Offset(rugCx - rugW * 0.5f, rugCy - rugH * 0.5f),
        size = Size(rugW, rugH)
    )
    drawOval(
        color = WoodDark.copy(alpha = 0.65f),
        topLeft = Offset(rugCx - rugW * 0.5f, rugCy - rugH * 0.5f),
        size = Size(rugW, rugH),
        style = Stroke(width = 3.dp.toPx())
    )

    // Vành chính xanh sage
    drawOval(
        color = RugMain,
        topLeft = Offset(rugCx - rugW * 0.46f, rugCy - rugH * 0.44f),
        size = Size(rugW * 0.92f, rugH * 0.88f)
    )

    // Lòng trong thảm xanh sáng
    drawOval(
        color = RugInner,
        topLeft = Offset(rugCx - rugW * 0.35f, rugCy - rugH * 0.33f),
        size = Size(rugW * 0.70f, rugH * 0.66f)
    )
    drawOval(
        color = WallStripeLight.copy(alpha = 0.6f),
        topLeft = Offset(rugCx - rugW * 0.35f, rugCy - rugH * 0.33f),
        size = Size(rugW * 0.70f, rugH * 0.66f),
        style = Stroke(width = 2.dp.toPx())
    )

    // Hoa nhỏ trang trí vòng quanh vành thảm
    val angles = listOf(0.15f, 0.45f, 0.75f, 1.05f, 1.35f, 1.65f, 1.95f, 2.25f, 2.55f, 2.85f)
    angles.forEachIndexed { idx, a ->
        val rad = a * Math.PI
        val fx = rugCx + ( kotlin.math.cos(rad) * rugW * 0.40f ).toFloat()
        val fy = rugCy + ( kotlin.math.sin(rad) * rugH * 0.38f ).toFloat()
        drawCircle(
            color = if (idx % 2 == 0) WallStripeLight else PlaidYellow,
            radius = 4.5.dp.toPx(),
            center = Offset(fx, fy)
        )
        drawCircle(
            color = LeafGreenDark,
            radius = 2.5.dp.toPx(),
            center = Offset(fx + 5.dp.toPx(), fy + 2.dp.toPx())
        )
    }
}

// ── 4. CỬA SỔ GỖ MÁI VÒM NHỌN MÙA THU & CÂY RỦ ──────────────────────────────

private fun DrawScope.drawGothicAutumnWindow(w: Float, h: Float) {
    val winL = w * 0.045f
    val winT = h * 0.11f
    val winW = w * 0.25f
    val winH = h * 0.20f
    val archPeakY = h * 0.025f
    val winCx = winL + winW * 0.5f

    // 4.1 Rèm ren kem dưới bệ cửa sổ
    val laceTop = winT + winH
    val laceH = h * 0.040f
    val lacePath = Path().apply {
        moveTo(winL + 4.dp.toPx(), laceTop)
        lineTo(winL + winW - 4.dp.toPx(), laceTop)
        lineTo(winL + winW - 8.dp.toPx(), laceTop + laceH * 0.75f)
        val scallops = 5
        val sw = (winW - 16.dp.toPx()) / scallops
        for (i in scallops downTo 1) {
            val sx = winL + 8.dp.toPx() + (i - 0.5f) * sw
            val ex = winL + 8.dp.toPx() + (i - 1) * sw
            quadraticTo(sx, laceTop + laceH * 1.15f, ex, laceTop + laceH * 0.75f)
        }
        close()
    }
    drawPath(lacePath, color = Color(0xFFFFF5D2))
    drawPath(lacePath, color = WoodMedium.copy(alpha = 0.6f), style = Stroke(width = 2.5.dp.toPx()))

    // 4.2 Khung gỗ mái vòm nhọn phía trên
    val outerWindowPath = Path().apply {
        moveTo(winL, winT)
        lineTo(winCx, archPeakY)
        lineTo(winL + winW, winT)
        lineTo(winL + winW, winT + winH)
        lineTo(winL, winT + winH)
        close()
    }
    drawPath(outerWindowPath, color = WoodMedium)
    drawPath(outerWindowPath, color = WoodDark, style = Stroke(width = 4.dp.toPx(), join = StrokeJoin.Round))

    // Họa tiết chạm khắc hình chiếc lá trên vòm gỗ
    val leafCarve = Path().apply {
        moveTo(winCx, winT - 4.dp.toPx())
        quadraticTo(winCx - winW * 0.18f, winT - (winT - archPeakY) * 0.45f, winCx, archPeakY + 12.dp.toPx())
        quadraticTo(winCx + winW * 0.18f, winT - (winT - archPeakY) * 0.45f, winCx, winT - 4.dp.toPx())
        close()
    }
    drawPath(leafCarve, color = WoodHighlight)
    drawPath(leafCarve, color = WoodDark, style = Stroke(width = 2.5.dp.toPx()))

    // 4.3 Mặt kính cửa sổ (Cảnh sắc thu vàng cam bên ngoài)
    val glassMargin = winW * 0.08f
    val glassL = winL + glassMargin
    val glassT = winT + glassMargin * 0.5f
    val glassW = winW - glassMargin * 2f
    val glassH = winH - glassMargin * 1.3f

    val glassPath = Path().apply {
        addRect(androidx.compose.ui.geometry.Rect(glassL, glassT, glassL + glassW, glassT + glassH))
    }
    clipPath(glassPath) {
        // Nền nắng thu vàng ấm
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFE59E), Color(0xFFFFF6D6)),
                startY = glassT,
                endY = glassT + glassH
            ),
            topLeft = Offset(glassL, glassT),
            size = Size(glassW, glassH)
        )
        // Tán cây mùa thu hồng cam bên trái
        drawCircle(
            color = Color(0xFFEDA894),
            radius = glassW * 0.38f,
            center = Offset(glassL + glassW * 0.2f, glassT + glassH * 0.35f)
        )
        // Tán cây mùa thu vàng cam bên phải
        drawCircle(
            color = Color(0xFFF3B36C),
            radius = glassW * 0.42f,
            center = Offset(glassL + glassW * 0.8f, glassT + glassH * 0.30f)
        )
        drawCircle(
            color = Color(0xFFF7CE76),
            radius = glassW * 0.32f,
            center = Offset(glassL + glassW * 0.65f, glassT + glassH * 0.60f)
        )
    }

    // Đố cửa chữ thập chia 4 ô kính
    drawRect(
        color = WoodDark,
        topLeft = Offset(glassL, glassT),
        size = Size(glassW, glassH),
        style = Stroke(width = 3.5.dp.toPx())
    )
    drawLine(
        color = WoodMedium,
        start = Offset(winCx, glassT),
        end = Offset(winCx, glassT + glassH),
        strokeWidth = 4.dp.toPx()
    )
    drawLine(
        color = WoodMedium,
        start = Offset(glassL, glassT + glassH * 0.5f),
        end = Offset(glassL + glassW, glassT + glassH * 0.5f),
        strokeWidth = 4.dp.toPx()
    )

    // Bệ cửa sổ gỗ
    drawRoundRect(
        color = WoodLight,
        topLeft = Offset(winL - 5.dp.toPx(), winT + winH - 6.dp.toPx()),
        size = Size(winW + 10.dp.toPx(), 10.dp.toPx()),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    drawRoundRect(
        color = WoodDark,
        topLeft = Offset(winL - 5.dp.toPx(), winT + winH - 6.dp.toPx()),
        size = Size(winW + 10.dp.toPx(), 10.dp.toPx()),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
        style = Stroke(width = 2.5.dp.toPx())
    )

    // 4.4 Chậu cây nhỏ trên bệ cửa sổ
    val potCx = winL + winW * 0.24f
    val potBottomY = winT + winH - 4.dp.toPx()
    drawOval(
        color = PlaidPink,
        topLeft = Offset(potCx - 11.dp.toPx(), potBottomY - 14.dp.toPx()),
        size = Size(22.dp.toPx(), 14.dp.toPx())
    )
    drawCircle(color = LeafGreenMid, radius = 7.dp.toPx(), center = Offset(potCx - 6.dp.toPx(), potBottomY - 18.dp.toPx()))
    drawCircle(color = LeafGreenLight, radius = 8.dp.toPx(), center = Offset(potCx, potBottomY - 21.dp.toPx()))
    drawCircle(color = LeafGreenLight, radius = 7.dp.toPx(), center = Offset(potCx + 6.dp.toPx(), potBottomY - 18.dp.toPx()))
}

// ── 5. CHÙM NẮNG CHIẾU TỪ CỬA SỔ ────────────────────────────────────────────

private fun DrawScope.drawAutumnSunbeam(w: Float, h: Float, sunGlow: Float) {
    val beamPath = Path().apply {
        moveTo(w * 0.08f, h * 0.12f)
        lineTo(w * 0.28f, h * 0.15f)
        lineTo(w * 0.74f, h * 0.82f)
        lineTo(w * 0.36f, h * 0.86f)
        close()
    }
    drawPath(
        path = beamPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFFFF2B0).copy(alpha = 0.26f * sunGlow),
                Color(0xFFFFE898).copy(alpha = 0.14f * sunGlow),
                Color(0xFFFFE898).copy(alpha = 0f)
            ),
            start = Offset(w * 0.16f, h * 0.13f),
            end = Offset(w * 0.65f, h * 0.84f)
        )
    )
}

// ── 6. KỆ GỖ & CỤM TRANH TREO TƯỜNG (GALLERY WALL CLUSTER) ──────────────────

private fun DrawScope.drawWallFramesAndShelf(w: Float, h: Float) {
    // 6.1 Kệ gỗ gắn tường ở giữa
    val shelfL = w * 0.44f
    val shelfT = h * 0.22f
    val shelfW = w * 0.26f
    val shelfH = h * 0.028f
    drawRoundRect(
        color = WoodLight,
        topLeft = Offset(shelfL, shelfT),
        size = Size(shelfW, shelfH),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    drawRoundRect(
        color = WoodDark,
        topLeft = Offset(shelfL, shelfT),
        size = Size(shelfW, shelfH),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
        style = Stroke(width = 2.5.dp.toPx())
    )
    // Tách hoa và chậu cây mini trên kệ
    val cup1X = shelfL + shelfW * 0.26f
    drawOval(color = WallStripeLight, topLeft = Offset(cup1X - 10.dp.toPx(), shelfT - 10.dp.toPx()), size = Size(20.dp.toPx(), 10.dp.toPx()))
    drawCircle(color = Color(0xFFB392C8), radius = 5.dp.toPx(), center = Offset(cup1X - 3.dp.toPx(), shelfT - 14.dp.toPx()))
    drawCircle(color = PlaidYellow, radius = 4.dp.toPx(), center = Offset(cup1X + 4.dp.toPx(), shelfT - 13.dp.toPx()))

    val cup2X = shelfL + shelfW * 0.74f
    drawOval(color = RugInner, topLeft = Offset(cup2X - 10.dp.toPx(), shelfT - 10.dp.toPx()), size = Size(20.dp.toPx(), 10.dp.toPx()))
    drawCircle(color = LeafGreenLight, radius = 7.dp.toPx(), center = Offset(cup2X, shelfT - 16.dp.toPx()))
    drawCircle(color = LeafGreenMid, radius = 5.dp.toPx(), center = Offset(cup2X - 5.dp.toPx(), shelfT - 13.dp.toPx()))

    // 6.2 Cụm tranh treo tường (Gallery Wall Group) gắn kết phía trên kệ
    // Khung tranh hoa cúc treo dây chính giữa
    val topFrameL = w * 0.49f
    val topFrameT = h * 0.07f
    val topFrameW = w * 0.14f
    val topFrameH = h * 0.09f
    val topFrameCx = topFrameL + topFrameW * 0.5f

    drawCircle(color = PlaidYellow, radius = 4.dp.toPx(), center = Offset(topFrameCx, h * 0.045f))
    drawLine(color = WoodDark, start = Offset(topFrameCx, h * 0.045f), end = Offset(topFrameL + 6.dp.toPx(), topFrameT), strokeWidth = 2.dp.toPx())
    drawLine(color = WoodDark, start = Offset(topFrameCx, h * 0.045f), end = Offset(topFrameL + topFrameW - 6.dp.toPx(), topFrameT), strokeWidth = 2.dp.toPx())

    drawRoundRect(color = WoodHighlight, topLeft = Offset(topFrameL, topFrameT), size = Size(topFrameW, topFrameH), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()))
    drawRoundRect(color = WoodDark, topLeft = Offset(topFrameL, topFrameT), size = Size(topFrameW, topFrameH), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()), style = Stroke(width = 2.5.dp.toPx()))
    drawRect(color = WallStripeLight, topLeft = Offset(topFrameL + 5.dp.toPx(), topFrameT + 5.dp.toPx()), size = Size(topFrameW - 10.dp.toPx(), topFrameH - 10.dp.toPx()))
    drawCircle(color = Color(0xFFF29B60), radius = 6.dp.toPx(), center = Offset(topFrameCx, topFrameT + topFrameH * 0.5f))
    drawCircle(color = LeafGreenMid, radius = 4.dp.toPx(), center = Offset(topFrameCx - 7.dp.toPx(), topFrameT + topFrameH * 0.52f))
    drawCircle(color = LeafGreenMid, radius = 4.dp.toPx(), center = Offset(topFrameCx + 7.dp.toPx(), topFrameT + topFrameH * 0.52f))

    // Khung tranh nấm rừng bên phải cụm
    val mushL = w * 0.69f
    val mushT = h * 0.11f
    val mushW = w * 0.13f
    val mushH = h * 0.12f
    drawRoundRect(color = WoodMedium, topLeft = Offset(mushL, mushT), size = Size(mushW, mushH), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()))
    drawRoundRect(color = WoodDark, topLeft = Offset(mushL, mushT), size = Size(mushW, mushH), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()), style = Stroke(width = 2.5.dp.toPx()))
    drawRect(color = WallStripeLight, topLeft = Offset(mushL + 5.dp.toPx(), mushT + 5.dp.toPx()), size = Size(mushW - 10.dp.toPx(), mushH - 10.dp.toPx()))
    val mCx = mushL + mushW * 0.5f
    val mCy = mushT + mushH * 0.56f
    drawRoundRect(color = WallStripeLight, topLeft = Offset(mCx - 4.dp.toPx(), mCy), size = Size(8.dp.toPx(), mushH * 0.30f), cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()))
    drawArc(color = PlaidPink, startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(mCx - 12.dp.toPx(), mCy - 10.dp.toPx()), size = Size(24.dp.toPx(), 18.dp.toPx()))
    drawCircle(color = WallStripeLight, radius = 2.dp.toPx(), center = Offset(mCx - 4.dp.toPx(), mCy - 5.dp.toPx()))
    drawCircle(color = WallStripeLight, radius = 2.dp.toPx(), center = Offset(mCx + 4.dp.toPx(), mCy - 5.dp.toPx()))

    // Khung tranh quả bí ngô bên trái cụm
    val pmpL = w * 0.33f
    val pmpT = h * 0.12f
    val pmpW = w * 0.11f
    val pmpH = h * 0.11f
    drawRoundRect(color = WoodHighlight, topLeft = Offset(pmpL, pmpT), size = Size(pmpW, pmpH), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()))
    drawRoundRect(color = WoodDark, topLeft = Offset(pmpL, pmpT), size = Size(pmpW, pmpH), cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()), style = Stroke(width = 2.5.dp.toPx()))
    drawRect(color = WallStripeLight, topLeft = Offset(pmpL + 4.dp.toPx(), pmpT + 4.dp.toPx()), size = Size(pmpW - 8.dp.toPx(), pmpH - 8.dp.toPx()))
    val pmpCx = pmpL + pmpW * 0.5f
    val pmpCy = pmpT + pmpH * 0.55f
    drawOval(color = Color(0xFFE88A38), topLeft = Offset(pmpCx - 9.dp.toPx(), pmpCy - 6.dp.toPx()), size = Size(18.dp.toPx(), 14.dp.toPx()))
    drawOval(color = Color(0xFFF5A34E), topLeft = Offset(pmpCx - 5.dp.toPx(), pmpCy - 6.dp.toPx()), size = Size(10.dp.toPx(), 14.dp.toPx()))
    drawLine(color = LeafGreenDark, start = Offset(pmpCx, pmpCy - 6.dp.toPx()), end = Offset(pmpCx + 3.dp.toPx(), pmpCy - 9.dp.toPx()), strokeWidth = 2.dp.toPx())
}

// ── 7. TỦ GỖ KHĂN ĐỎ & GHẾ BÀNH SOFA CARO HỒNG ─────────────────────────────

private fun DrawScope.drawRightCabinetAndArmchair(w: Float, h: Float) {
    // 7.1 Tủ gỗ nhỏ kèm khăn trải đỏ viền vàng
    val cabL = w * 0.70f
    val cabT = h * 0.42f
    val cabW = w * 0.15f
    val cabH = h * 0.13f

    drawRoundRect(
        color = WoodMedium,
        topLeft = Offset(cabL, cabT),
        size = Size(cabW, cabH),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
    drawRoundRect(
        color = WoodDark,
        topLeft = Offset(cabL, cabT),
        size = Size(cabW, cabH),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
        style = Stroke(width = 2.5.dp.toPx())
    )
    // Khăn trải bàn màu đỏ viền vàng
    val runnerPath = Path().apply {
        moveTo(cabL + 3.dp.toPx(), cabT)
        lineTo(cabL + cabW - 3.dp.toPx(), cabT)
        quadraticTo(cabL + cabW * 0.5f, cabT + cabH * 0.65f, cabL + 3.dp.toPx(), cabT)
        close()
    }
    drawPath(runnerPath, color = Color(0xFFA64438))
    drawPath(runnerPath, color = PlaidYellow, style = Stroke(width = 2.dp.toPx()))

    // Chậu cây nhỏ trên tủ
    val plantX = cabL + cabW * 0.35f
    drawRoundRect(
        color = PlaidPink,
        topLeft = Offset(plantX - 8.dp.toPx(), cabT - 12.dp.toPx()),
        size = Size(16.dp.toPx(), 12.dp.toPx()),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    drawCircle(color = LeafGreenMid, radius = 8.dp.toPx(), center = Offset(plantX, cabT - 18.dp.toPx()))
    drawCircle(color = LeafGreenLight, radius = 6.dp.toPx(), center = Offset(plantX - 5.dp.toPx(), cabT - 15.dp.toPx()))
    drawCircle(color = LeafGreenLight, radius = 6.dp.toPx(), center = Offset(plantX + 5.dp.toPx(), cabT - 15.dp.toPx()))

    // 7.2 Ghế bành sofa caro hồng bên phải sàn
    val chairL = w * 0.81f
    val chairT = h * 0.44f
    val chairW = w * 0.18f
    val chairH = h * 0.20f

    // Chân ghế gỗ
    drawLine(
        color = WoodDark,
        start = Offset(chairL + chairW * 0.15f, chairT + chairH * 0.85f),
        end = Offset(chairL + chairW * 0.15f, chairT + chairH * 1.05f),
        strokeWidth = 5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawLine(
        color = WoodDark,
        start = Offset(chairL + chairW * 0.70f, chairT + chairH * 0.90f),
        end = Offset(chairL + chairW * 0.70f, chairT + chairH * 1.08f),
        strokeWidth = 5.dp.toPx(),
        cap = StrokeCap.Round
    )

    // Lưng ghế sofa bo tròn
    val backPath = Path().apply {
        moveTo(chairL + chairW * 0.15f, chairT + chairH * 0.55f)
        cubicTo(
            chairL + chairW * 0.10f, chairT - chairH * 0.05f,
            chairL + chairW * 0.85f, chairT - chairH * 0.05f,
            chairL + chairW * 0.96f, chairT + chairH * 0.35f
        )
        lineTo(chairL + chairW * 0.92f, chairT + chairH * 0.88f)
        lineTo(chairL + chairW * 0.15f, chairT + chairH * 0.82f)
        close()
    }
    clipPath(backPath) {
        drawPath(backPath, color = PlaidPink)
        for (i in 0..5) {
            val sx = chairL + i * (chairW * 0.2f)
            drawLine(
                color = PlaidYellow.copy(alpha = 0.55f),
                start = Offset(sx, chairT),
                end = Offset(sx - 10.dp.toPx(), chairT + chairH),
                strokeWidth = 5.dp.toPx()
            )
            val sy = chairT + i * (chairH * 0.2f)
            drawLine(
                color = PlaidCream.copy(alpha = 0.45f),
                start = Offset(chairL, sy),
                end = Offset(chairL + chairW, sy + 6.dp.toPx()),
                strokeWidth = 4.dp.toPx()
            )
        }
    }
    drawPath(backPath, color = WoodDark, style = Stroke(width = 3.dp.toPx(), join = StrokeJoin.Round))

    // Tay vịn & Đệm ngồi ghế sofa
    drawRoundRect(
        color = PlaidPink,
        topLeft = Offset(chairL + chairW * 0.04f, chairT + chairH * 0.46f),
        size = Size(chairW * 0.86f, chairH * 0.44f),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
    )
    drawRoundRect(
        color = WoodDark,
        topLeft = Offset(chairL + chairW * 0.04f, chairT + chairH * 0.46f),
        size = Size(chairW * 0.86f, chairH * 0.44f),
        cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
        style = Stroke(width = 3.dp.toPx())
    )

    // Gối tựa nhỏ trên ghế
    drawRoundRect(
        color = PlaidYellow,
        topLeft = Offset(chairL + chairW * 0.32f, chairT + chairH * 0.32f),
        size = Size(chairW * 0.38f, chairH * 0.28f),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
    )
    drawRoundRect(
        color = WoodDark,
        topLeft = Offset(chairL + chairW * 0.32f, chairT + chairH * 0.32f),
        size = Size(chairW * 0.38f, chairH * 0.28f),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
    )
}

// ── 8. ĐÈN CÂY CỔ ĐIỂN TỎA SÁNG ─────────────────────────────────────────────

private fun DrawScope.drawVintageFloorLamp(w: Float, h: Float) {
    val lampX = w * 0.36f
    val shadeTop = h * 0.23f
    val shadeBottom = h * 0.31f
    val shadeW = w * 0.14f
    val baseBottom = h * 0.51f

    // Thân đèn và chân đế
    drawLine(
        color = WoodMedium,
        start = Offset(lampX, shadeBottom),
        end = Offset(lampX, baseBottom),
        strokeWidth = 5.dp.toPx(),
        cap = StrokeCap.Round
    )
    drawOval(
        color = WoodLight,
        topLeft = Offset(lampX - 16.dp.toPx(), baseBottom - 7.dp.toPx()),
        size = Size(32.dp.toPx(), 13.dp.toPx())
    )
    drawOval(
        color = WoodDark,
        topLeft = Offset(lampX - 16.dp.toPx(), baseBottom - 7.dp.toPx()),
        size = Size(32.dp.toPx(), 13.dp.toPx()),
        style = Stroke(width = 2.5.dp.toPx())
    )

    // Chao đèn xếp ly kem vàng
    val shadePath = Path().apply {
        moveTo(lampX - shadeW * 0.18f, shadeTop)
        lineTo(lampX + shadeW * 0.18f, shadeTop)
        quadraticTo(
            lampX + shadeW * 0.52f, shadeBottom - 6.dp.toPx(),
            lampX + shadeW * 0.50f, shadeBottom
        )
        quadraticTo(
            lampX, shadeBottom + 8.dp.toPx(),
            lampX - shadeW * 0.50f, shadeBottom
        )
        quadraticTo(
            lampX - shadeW * 0.52f, shadeBottom - 6.dp.toPx(),
            lampX - shadeW * 0.18f, shadeTop
        )
        close()
    }
    drawPath(
        path = shadePath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFFFBE0), Color(0xFFF9E4A0)),
            startY = shadeTop,
            endY = shadeBottom
        )
    )
    drawPath(shadePath, color = WoodMedium, style = Stroke(width = 2.5.dp.toPx(), join = StrokeJoin.Round))
}

private fun DrawScope.drawLampGlow(w: Float, h: Float, lampGlow: Float) {
    val lampX = w * 0.36f
    val shadeTop = h * 0.23f
    val shadeBottom = h * 0.31f
    val shadeW = w * 0.14f

    // Vầng hào quang ấm áp quanh chao đèn
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFF3A8).copy(alpha = 0.45f * lampGlow),
                Color(0xFFFFF3A8).copy(alpha = 0f)
            ),
            center = Offset(lampX, (shadeTop + shadeBottom) * 0.5f),
            radius = shadeW * 0.95f * lampGlow
        ),
        radius = shadeW * 0.95f * lampGlow,
        center = Offset(lampX, (shadeTop + shadeBottom) * 0.5f)
    )
}

// ── 9. GIƯỜNG GỖ TAI MÈO & CHĂN CARO (GÓC DƯỚI BÊN TRÁI) ───────────────────

private fun DrawScope.drawCatEarWoodenBed(w: Float, h: Float) {
    val bedL = w * 0.035f
    val bedT = h * 0.44f
    val bedW = w * 0.31f
    val bedH = h * 0.28f
    val bedCx = bedL + bedW * 0.5f

    // 9.1 Đầu giường gỗ tạo hình đôi tai mèo
    val headboardPath = Path().apply {
        moveTo(bedL + 6f, bedT + bedH * 0.45f)
        lineTo(bedL + 6f, bedT + bedH * 0.15f)
        // Tai mèo trái của đầu giường
        lineTo(bedL + bedW * 0.24f, bedT)
        quadraticTo(bedCx, bedT + bedH * 0.12f, bedL + bedW * 0.76f, bedT)
        // Tai mèo phải của đầu giường
        lineTo(bedL + bedW - 6.dp.toPx(), bedT + bedH * 0.15f)
        lineTo(bedL + bedW - 6.dp.toPx(), bedT + bedH * 0.45f)
        close()
    }
    drawPath(headboardPath, color = WoodMedium)
    drawPath(headboardPath, color = WoodDark, style = Stroke(width = 3.5.dp.toPx(), join = StrokeJoin.Round))

    // Hai trụ gỗ đầu giường
    drawRoundRect(
        color = WoodLight,
        topLeft = Offset(bedL, bedT + bedH * 0.08f),
        size = Size(11.dp.toPx(), bedH * 0.45f),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
    drawRoundRect(
        color = WoodDark,
        topLeft = Offset(bedL, bedT + bedH * 0.08f),
        size = Size(11.dp.toPx(), bedH * 0.45f),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
        style = Stroke(width = 2.5.dp.toPx())
    )
    drawRoundRect(
        color = WoodLight,
        topLeft = Offset(bedL + bedW - 11.dp.toPx(), bedT + bedH * 0.08f),
        size = Size(11.dp.toPx(), bedH * 0.45f),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
    drawRoundRect(
        color = WoodDark,
        topLeft = Offset(bedL + bedW - 11.dp.toPx(), bedT + bedH * 0.08f),
        size = Size(11.dp.toPx(), bedH * 0.45f),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
        style = Stroke(width = 2.5.dp.toPx())
    )

    // 9.2 Gối trắng phồng
    drawRoundRect(
        color = Color(0xFFFFFDF9),
        topLeft = Offset(bedL + bedW * 0.25f, bedT + bedH * 0.20f),
        size = Size(bedW * 0.50f, bedH * 0.18f),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
    )
    drawRoundRect(
        color = WoodDark,
        topLeft = Offset(bedL + bedW * 0.25f, bedT + bedH * 0.20f),
        size = Size(bedW * 0.50f, bedH * 0.18f),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
        style = Stroke(width = 2.5.dp.toPx())
    )

    // 9.3 Chăn bông caro nhiều màu
    val quiltTop = bedT + bedH * 0.34f
    val quiltH = bedH * 0.48f
    val quiltPath = Path().apply {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                left = bedL + 4.dp.toPx(),
                top = quiltTop,
                right = bedL + bedW - 4.dp.toPx(),
                bottom = quiltTop + quiltH,
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
            )
        )
    }
    clipPath(quiltPath) {
        drawPath(quiltPath, color = PlaidYellow)
        val cellW = bedW / 4f
        val cellH = quiltH / 3f
        for (r in 0..2) {
            for (c in 0..3) {
                val color = when ((r + c) % 3) {
                    0 -> PlaidYellow
                    1 -> PlaidPink
                    else -> PlaidGreen
                }
                drawRect(
                    color = color,
                    topLeft = Offset(bedL + c * cellW, quiltTop + r * cellH),
                    size = Size(cellW + 1f, cellH + 1f)
                )
            }
        }
        // Mép chăn gấp màu kem ở trên
        drawRect(
            color = PlaidCream,
            topLeft = Offset(bedL, quiltTop),
            size = Size(bedW, quiltH * 0.24f)
        )
        drawLine(
            color = WoodDark,
            start = Offset(bedL, quiltTop + quiltH * 0.24f),
            end = Offset(bedL + bedW, quiltTop + quiltH * 0.24f),
            strokeWidth = 2.5.dp.toPx()
        )
    }
    drawPath(quiltPath, color = WoodDark, style = Stroke(width = 3.5.dp.toPx()))

    // 9.4 Thành cuối giường gỗ + dấu chân mèo
    val footTop = bedT + bedH * 0.72f
    val footH = bedH * 0.24f
    drawRoundRect(
        color = WoodMedium,
        topLeft = Offset(bedL + 4.dp.toPx(), footTop),
        size = Size(bedW - 8.dp.toPx(), footH),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
    )
    drawRoundRect(
        color = WoodDark,
        topLeft = Offset(bedL + 4.dp.toPx(), footTop),
        size = Size(bedW - 8.dp.toPx(), footH),
        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
        style = Stroke(width = 3.5.dp.toPx())
    )
    drawCircle(color = WoodDark.copy(alpha = 0.55f), radius = 4.dp.toPx(), center = Offset(bedCx, footTop + footH * 0.55f))
    drawCircle(color = WoodDark.copy(alpha = 0.55f), radius = 2.dp.toPx(), center = Offset(bedCx - 4.5.dp.toPx(), footTop + footH * 0.36f))
    drawCircle(color = WoodDark.copy(alpha = 0.55f), radius = 2.dp.toPx(), center = Offset(bedCx, footTop + footH * 0.32f))
    drawCircle(color = WoodDark.copy(alpha = 0.55f), radius = 2.dp.toPx(), center = Offset(bedCx + 4.5.dp.toPx(), footTop + footH * 0.36f))

    // Hai trụ gỗ cuối giường
    drawRoundRect(
        color = WoodLight,
        topLeft = Offset(bedL, footTop - 8.dp.toPx()),
        size = Size(11.dp.toPx(), footH + 14.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
    drawRoundRect(
        color = WoodDark,
        topLeft = Offset(bedL, footTop - 8.dp.toPx()),
        size = Size(11.dp.toPx(), footH + 14.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
        style = Stroke(width = 2.5.dp.toPx())
    )
    drawRoundRect(
        color = WoodLight,
        topLeft = Offset(bedL + bedW - 11.dp.toPx(), footTop - 8.dp.toPx()),
        size = Size(11.dp.toPx(), footH + 14.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
    )
    drawRoundRect(
        color = WoodDark,
        topLeft = Offset(bedL + bedW - 11.dp.toPx(), footTop - 8.dp.toPx()),
        size = Size(11.dp.toPx(), footH + 14.dp.toPx()),
        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
        style = Stroke(width = 2.5.dp.toPx())
    )
}

// ── 10. ĐỒ VẬT TRANG TRÍ TRÊN SÀN (CỘT CÀO MÓNG, CÂY CẢNH) ─────────────────

private fun DrawScope.drawFloorAccessories(w: Float, h: Float) {
    // Cột cào móng mèo (Cat scratching post) ở góc phải
    val postX = w * 0.88f
    val postBaseY = h * 0.74f
    val postH = h * 0.16f
    val postW = w * 0.052f

    // Đế tròn gỗ
    drawOval(
        color = WoodDark,
        topLeft = Offset(postX - postW * 1.3f, postBaseY - 5.dp.toPx()),
        size = Size(postW * 2.6f, 14.dp.toPx())
    )
    drawOval(
        color = WoodMedium,
        topLeft = Offset(postX - postW * 1.2f, postBaseY - 4.dp.toPx()),
        size = Size(postW * 2.4f, 11.dp.toPx())
    )

    // Thân cột quấn dây sisal
    val postTop = postBaseY - postH
    drawRoundRect(
        color = Color(0xFFD8C2A0),
        topLeft = Offset(postX - postW * 0.5f, postTop),
        size = Size(postW, postH),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
    )
    drawRoundRect(
        color = WoodDark,
        topLeft = Offset(postX - postW * 0.5f, postTop),
        size = Size(postW, postH),
        cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
        style = Stroke(width = 2.dp.toPx())
    )
    val ropeLoops = 7
    val loopStep = postH / ropeLoops
    for (i in 1 until ropeLoops) {
        val ly = postTop + i * loopStep
        drawLine(
            color = Color(0xFFB59A75),
            start = Offset(postX - postW * 0.46f, ly),
            end = Offset(postX + postW * 0.46f, ly),
            strokeWidth = 2.dp.toPx()
        )
    }

    // Đỉnh cột có bóng tròn treo chuông nhỏ
    drawCircle(color = WoodMedium, radius = postW * 0.55f, center = Offset(postX, postTop))
    drawCircle(color = WoodDark, radius = postW * 0.55f, center = Offset(postX, postTop), style = Stroke(width = 2.dp.toPx()))
    drawLine(color = WoodDark, start = Offset(postX + postW * 0.3f, postTop), end = Offset(postX + postW * 0.8f, postTop + 14.dp.toPx()), strokeWidth = 1.5.dp.toPx())
    drawCircle(color = PlaidYellow, radius = 3.5.dp.toPx(), center = Offset(postX + postW * 0.8f, postTop + 16.dp.toPx()))

    // Chậu cây cảnh trên sàn ở góc trái
    val plantX = w * 0.05f
    val plantY = h * 0.74f
    val potPath = Path().apply {
        moveTo(plantX - 11.dp.toPx(), plantY)
        lineTo(plantX + 11.dp.toPx(), plantY)
        lineTo(plantX + 8.dp.toPx(), plantY + 16.dp.toPx())
        lineTo(plantX - 8.dp.toPx(), plantY + 16.dp.toPx())
        close()
    }
    drawPath(potPath, color = Color(0xFFD49A80))
    drawPath(potPath, color = WoodDark, style = Stroke(width = 2.dp.toPx()))
    drawCircle(color = LeafGreenDark, radius = 8.dp.toPx(), center = Offset(plantX - 7.dp.toPx(), plantY - 5.dp.toPx()))
    drawCircle(color = LeafGreenMid, radius = 9.dp.toPx(), center = Offset(plantX + 2.dp.toPx(), plantY - 9.dp.toPx()))
    drawCircle(color = LeafGreenLight, radius = 7.dp.toPx(), center = Offset(plantX + 9.dp.toPx(), plantY - 4.dp.toPx()))
}

// ── 11. HẠT BỤI VÀNG BAY CHẬM TRONG NẮNG ─────────────────────────────────────

private fun DrawScope.drawFloatingDustMotes(w: Float, h: Float, phase: Float, glow: Float) {
    val motes = listOf(
        listOf(w * 0.24f, h * 0.26f, 3.0f, 0.1f),
        listOf(w * 0.32f, h * 0.36f, 2.6f, 0.4f),
        listOf(w * 0.42f, h * 0.46f, 3.2f, 0.7f),
        listOf(w * 0.50f, h * 0.56f, 2.5f, 0.2f),
        listOf(w * 0.58f, h * 0.66f, 2.8f, 0.9f),
        listOf(w * 0.36f, h * 0.52f, 2.2f, 0.5f),
        listOf(w * 0.46f, h * 0.38f, 3.0f, 0.3f),
        listOf(w * 0.62f, h * 0.72f, 2.4f, 0.8f)
    )

    for (m in motes) {
        val bx = m[0]
        val by = m[1]
        val r = m[2].dp.toPx()
        val p = m[3]

        val t = (phase + p) % 1f
        val driftX = (kotlin.math.cos(t * 2 * Math.PI) * 7.dp.toPx()).toFloat()
        val driftY = (kotlin.math.sin(t * 2 * Math.PI) * 10.dp.toPx()).toFloat()

        val cx = bx + driftX
        val cy = by + driftY
        val alpha = (0.35f + kotlin.math.sin(t * Math.PI).toFloat() * 0.45f) * glow

        drawCircle(
            color = Color(0xFFFFF8D0).copy(alpha = (alpha * 0.4f).coerceIn(0f, 1f)),
            radius = r * 1.8f,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color(0xFFFFFDF0).copy(alpha = alpha.coerceIn(0f, 1f)),
            radius = r,
            center = Offset(cx, cy)
        )
    }
}
