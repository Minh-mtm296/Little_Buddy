package com.littlebuddy.pet.pet

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import com.littlebuddy.pet.wardrobe.CatColorPalette

private class CalicoBlob(
    val dx: Float,
    val dy: Float,
    val rx: Float,
    val ry: Float,
    val rot: Float,
    val dark: Boolean
)

// Toạ độ theo đơn vị u, gốc = (cx, cy) của thân đứng
private val STANDING_BLOBS = listOf(
    CalicoBlob(-0.36f, 0.40f, 0.30f, 0.34f, -20f, dark = false), // nâu: hông trái
    CalicoBlob( 0.40f, 0.62f, 0.26f, 0.30f,  15f, dark = true),  // xám: hông phải
    CalicoBlob( 0.22f, 0.10f, 0.14f, 0.12f,   0f, dark = false)  // đốm nâu nhỏ: vai phải
)

internal fun DrawScope.drawCalicoBodyPatches(
    cx: Float, cy: Float, u: Float, sx: Float, sy: Float, palette: CatColorPalette
) {
    val dark = palette.secondaryPatchColor ?: return
    for (b in STANDING_BLOBS) {
        val c = Offset(cx + b.dx * u * sx, cy + b.dy * u * sy)
        withTransform({ rotate(b.rot, pivot = c) }) {
            drawOval(
                color = if (b.dark) dark else palette.patchColor,
                topLeft = Offset(c.x - b.rx * u * sx, c.y - b.ry * u * sy),
                size = Size(b.rx * 2f * u * sx, b.ry * 2f * u * sy)
            )
        }
    }
}
