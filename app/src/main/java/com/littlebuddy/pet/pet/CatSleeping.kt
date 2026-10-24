package com.littlebuddy.pet.pet

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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.luminance
import com.littlebuddy.pet.wardrobe.CatColorPalette
import com.littlebuddy.pet.wardrobe.EquippedWardrobe
import kotlin.math.PI
import kotlin.math.sin

/**
 * Mèo trắng ngủ cuộn tròn (Fluffy Curled-up Sleeping Cat) – vẽ 100% vector Canvas.
 *
 * Phiên bản này được TRÍCH XUẤT TRỰC TIẾP từ ảnh minh họa mẫu (viền ngoài + toàn bộ nét bên trong:
 * lông tỉa, tai, mắt, mũi, râu, bàn chân, đường cong đuôi / mông...) thay vì ước lượng bằng tay,
 * nên hình dáng khớp ảnh mẫu. Toạ độ chuẩn hoá theo đơn vị u: tâm (0,0) = tâm ảnh, rộng ≈ 2.15u, cao ≈ 1.5u.
 *
 * Lớp vẽ: bóng sàn → thân + màu nước → ruột tai/má hồng/mũi → nét viền → hoa văn skin →
 * trang phục → vòng cổ → mũ → Zzz.
 *
 * Lưu ý: [sleepingBreathScale] cần được caller animate theo thời gian (ví dụ 1.00f..1.03f).
 */

// ───────────────────────── Dữ liệu hình học (đơn vị u) ─────────────────────────

private const val SILHOUETTE_DATA = "-0.074,-0.752 -0.120,-0.734 -0.144,-0.734 -0.213,-0.699 -0.254,-0.705 -0.313,-0.688 -0.441,-0.623 -0.487,-0.588 -0.511,-0.600 -0.662,-0.449 -0.674,-0.443 -0.697,-0.466 -0.773,-0.344 -0.784,-0.338 -0.808,-0.344 -0.825,-0.245 -0.866,-0.210 -0.919,-0.140 -0.953,-0.035 -0.988,0.012 -0.965,0.029 -0.988,0.117 -0.983,0.151 -1.000,0.163 -1.018,0.163 -1.023,0.175 -0.988,0.204 -0.971,0.251 -1.053,0.350 -1.070,0.379 -1.070,0.396 -1.058,0.408 -0.994,0.431 -0.971,0.431 -0.942,0.449 -0.953,0.489 -0.953,0.530 -0.942,0.565 -0.901,0.600 -0.819,0.600 -0.773,0.565 -0.744,0.583 -0.744,0.606 -0.726,0.612 -0.715,0.629 -0.680,0.594 -0.662,0.600 -0.598,0.594 -0.581,0.606 -0.581,0.623 -0.604,0.664 -0.604,0.688 -0.598,0.658 -0.563,0.594 -0.534,0.583 -0.511,0.594 -0.522,0.664 -0.505,0.583 -0.429,0.542 -0.412,0.548 -0.382,0.588 -0.353,0.600 -0.313,0.600 -0.295,0.612 -0.295,0.623 -0.208,0.635 -0.167,0.653 -0.126,0.658 -0.103,0.676 -0.103,0.693 -0.085,0.699 0.066,0.722 0.165,0.717 0.183,0.728 0.183,0.740 0.253,0.740 0.369,0.722 0.387,0.734 0.387,0.752 0.404,0.752 0.567,0.711 0.626,0.688 0.742,0.618 0.760,0.629 0.760,0.641 0.794,0.629 0.841,0.594 0.923,0.513 0.958,0.460 0.969,0.454 0.987,0.460 0.998,0.420 1.033,0.396 1.057,0.286 1.051,0.192 1.039,0.151 1.057,0.140 1.080,0.140 1.010,0.035 0.958,-0.064 0.946,-0.111 0.923,-0.157 0.929,-0.204 0.894,-0.268 0.841,-0.338 0.783,-0.396 0.614,-0.530 0.620,-0.559 0.573,-0.583 0.556,-0.612 0.392,-0.670 0.276,-0.682 0.258,-0.693 0.253,-0.711 0.218,-0.722 0.154,-0.734 0.020,-0.734 -0.039,-0.722 -0.056,-0.734 -0.056,-0.752"

/** Nét viền chính: lưng, đuôi, mông, tai, mép đầu, bàn chân. */
private const val STROKES_MAIN = "-0.074,-0.744 -0.091,-0.746 -0.097,-0.740 -0.138,-0.734 -0.155,-0.722 -0.167,-0.722 -0.179,-0.711 -0.190,-0.711 -0.225,-0.693 -0.237,-0.693 -0.248,-0.705 -0.254,-0.699 -0.272,-0.699 -0.295,-0.688 -0.307,-0.688 -0.324,-0.676 -0.336,-0.676 -0.435,-0.623 -0.499,-0.577 -0.516,-0.580|-0.085,-0.713 -0.068,-0.711 -0.045,-0.722 0.020,-0.728 0.025,-0.734 0.148,-0.734 0.154,-0.728 0.212,-0.722 0.229,-0.711 0.258,-0.705|0.547,-0.597 0.544,-0.612 0.480,-0.641 0.433,-0.653 0.427,-0.658 0.410,-0.658 0.387,-0.670 0.328,-0.676 0.323,-0.682 0.270,-0.682 0.258,-0.688|-0.516,-0.580 -0.534,-0.577 -0.621,-0.495 -0.621,-0.489 -0.674,-0.431 -0.685,-0.431 -0.691,-0.425 -0.699,-0.443|0.911,-0.192 0.917,-0.204 0.911,-0.227 0.894,-0.251 0.888,-0.268 0.824,-0.344 0.824,-0.350 0.736,-0.431 0.730,-0.431 0.626,-0.519 0.602,-0.530 0.600,-0.551|0.119,-0.460 0.089,-0.449 0.020,-0.396 -0.050,-0.326 -0.074,-0.291 -0.109,-0.256 -0.103,-0.239|-0.699,-0.443 -0.715,-0.437 -0.726,-0.414 -0.744,-0.396 -0.755,-0.373 -0.755,-0.361 -0.773,-0.338 -0.773,-0.326 -0.784,-0.315 -0.790,-0.320 -0.809,-0.326|-0.199,-0.020 -0.208,-0.041 -0.231,-0.058 -0.283,-0.082 -0.289,-0.087 -0.278,-0.105 -0.278,-0.117 -0.283,-0.122 -0.330,-0.122 -0.336,-0.128 -0.336,-0.151 -0.342,-0.157 -0.377,-0.134 -0.394,-0.134 -0.400,-0.128 -0.417,-0.146 -0.464,-0.216 -0.493,-0.245 -0.493,-0.251 -0.546,-0.303 -0.581,-0.326 -0.592,-0.326 -0.610,-0.309 -0.621,-0.286 -0.627,-0.256 -0.645,-0.221 -0.645,-0.204 -0.650,-0.198 -0.662,-0.151 -0.670,-0.138|-0.809,-0.326 -0.814,-0.309 -0.814,-0.286 -0.819,-0.280 -0.819,-0.256 -0.825,-0.245 -0.895,-0.169 -0.919,-0.134 -0.919,-0.122 -0.930,-0.111 -0.930,-0.093 -0.942,-0.076 -0.953,-0.029 -0.977,0.006 -0.959,0.017 -0.959,0.029 -0.971,0.052 -0.977,0.093 -0.988,0.122|-0.557,-0.103 -0.569,-0.117 -0.575,-0.151 -0.581,-0.157 -0.581,-0.198 -0.586,-0.204 -0.586,-0.286 -0.581,-0.297|-0.151,-0.047 -0.149,-0.064 -0.144,-0.111 -0.138,-0.117 -0.126,-0.163 -0.114,-0.181 -0.114,-0.192 -0.097,-0.221 -0.103,-0.239|1.045,0.128 1.051,0.111 1.039,0.082 1.004,0.035 0.952,-0.064 0.946,-0.093 0.934,-0.111 0.934,-0.122 0.911,-0.169 0.911,-0.181 0.911,-0.192|-0.829,-0.044 -0.831,-0.029 -0.831,-0.012 -0.854,0.012 -0.889,0.064 -0.889,0.076 -0.913,0.122 -0.919,0.157 -0.930,0.169|-0.126,0.107 -0.120,0.128 -0.114,0.134 -0.114,0.146 -0.097,0.163 -0.085,0.192 -0.074,0.204 -0.062,0.239 -0.079,0.251 -0.109,0.251 -0.114,0.256 -0.138,0.256 -0.149,0.262 -0.165,0.260|1.012,0.382 1.028,0.379 1.039,0.355 1.045,0.286 1.051,0.280 1.045,0.198 1.039,0.192 1.039,0.175 1.028,0.146 1.045,0.128|0.614,0.169 0.550,0.239 0.515,0.262 0.451,0.291 0.433,0.291 0.410,0.303 0.363,0.309 0.358,0.315 0.241,0.315 0.229,0.309 0.219,0.325|-0.963,0.241 -0.971,0.256 -1.023,0.315 -1.035,0.338 -1.064,0.373 -1.070,0.390 -1.053,0.408 -1.018,0.414 -0.971,0.431 -0.953,0.431 -0.939,0.439|-0.324,0.350 -0.330,0.326 -0.336,0.320 -0.336,0.303 -0.348,0.297 -0.382,0.344 -0.377,0.350 -0.348,0.344 -0.324,0.350|-0.837,0.309 -0.849,0.303 -0.854,0.309 -0.889,0.309 -0.895,0.315 -0.924,0.320 -0.994,0.355 -1.023,0.379|0.219,0.325 0.206,0.338 0.101,0.338 0.095,0.332 0.055,0.332 0.049,0.326 -0.091,0.326 -0.097,0.332 -0.120,0.332 -0.126,0.338 -0.138,0.350|-0.408,0.530 -0.394,0.513 -0.388,0.495 -0.353,0.460 -0.336,0.454 -0.243,0.379 -0.229,0.365|-0.775,0.527 -0.790,0.524 -0.802,0.519 -0.808,0.478 -0.825,0.460 -0.849,0.449 -0.872,0.449 -0.878,0.443 -0.907,0.443 -0.913,0.437 -0.924,0.437 -0.939,0.439|0.967,0.429 0.952,0.443 0.940,0.466 0.940,0.478 0.917,0.513 0.841,0.588 0.789,0.623 0.774,0.627|-0.866,0.592 -0.884,0.594 -0.895,0.594 -0.919,0.583 -0.942,0.554 -0.942,0.542 -0.948,0.536 -0.948,0.472 -0.942,0.466 -0.942,0.454 -0.939,0.439|0.410,0.738 0.427,0.740 0.439,0.734 0.462,0.734 0.468,0.728 0.492,0.728 0.532,0.711 0.567,0.705 0.614,0.682 0.626,0.682 0.637,0.670 0.719,0.629 0.754,0.600 0.771,0.596|-0.085,0.680 -0.091,0.664 -0.097,0.658 -0.120,0.658 -0.126,0.653 -0.213,0.635 -0.237,0.623 -0.260,0.623 -0.266,0.618 -0.282,0.610|0.207,0.724 0.194,0.705 0.183,0.705 0.177,0.711 0.113,0.711 0.107,0.717 0.072,0.717 0.066,0.711 0.008,0.711 0.002,0.705 -0.027,0.705 -0.033,0.699 -0.074,0.693 -0.085,0.680|0.207,0.724 0.223,0.734 0.247,0.734 0.253,0.728 0.299,0.728 0.305,0.722 0.334,0.722 0.340,0.717 0.369,0.717 0.375,0.711 0.398,0.711 0.410,0.705 0.427,0.707"

/** Nét mảnh: râu, nét tỉa lông, vệt trong tai. */
private const val STROKES_FINE = "0.547,-0.597 0.532,-0.594 0.521,-0.594|0.600,-0.551 0.596,-0.571 0.561,-0.583 0.547,-0.597|0.573,-0.548 0.579,-0.548 0.600,-0.551|0.229,-0.507 0.235,-0.513 0.247,-0.513|0.218,-0.507 0.189,-0.501 0.148,-0.472|-0.699,-0.443 -0.697,-0.460 -0.691,-0.466 -0.699,-0.443|-0.074,-0.251 -0.079,-0.245 -0.103,-0.239|0.870,-0.233 0.899,-0.198 0.911,-0.192|-0.522,-0.175 -0.528,-0.169|-0.534,-0.157 -0.546,-0.117 -0.557,-0.103|-0.516,-0.146 -0.522,-0.111|-0.670,-0.138 -0.685,-0.134 -0.691,-0.128 -0.709,-0.120|-0.750,-0.093 -0.726,-0.122 -0.709,-0.120|-0.670,-0.138 -0.668,-0.122 -0.668,-0.105|-0.511,-0.099 -0.505,-0.117|-0.476,-0.117 -0.476,-0.111|-0.829,-0.044 -0.814,-0.041 -0.808,-0.041 -0.790,-0.064 -0.773,-0.076|-0.199,-0.020 -0.179,-0.029 -0.167,-0.041 -0.151,-0.047|-0.149,0.017 -0.149,-0.029 -0.151,-0.047|-0.225,-0.017 -0.219,-0.017 -0.199,-0.020|-0.114,0.012 -0.132,0.023|-0.204,0.015 -0.219,0.029 -0.225,0.035|-0.204,0.015 -0.190,0.029 -0.184,0.047|-0.160,0.056 -0.138,0.064 -0.132,0.087 -0.126,0.107|-0.266,0.204 -0.260,0.186 -0.190,0.082|-0.126,0.107 -0.109,0.093 -0.091,0.087|-0.167,0.093 -0.167,0.105 -0.161,0.111 -0.157,0.132|1.074,0.140 1.080,0.140|-0.157,0.132 -0.173,0.146 -0.231,0.198 -0.254,0.233|-0.963,0.241 -0.977,0.227 -0.988,0.198 -1.006,0.181 -1.006,0.169 -0.971,0.151|-0.179,0.233 -0.167,0.221 -0.161,0.216 -0.161,0.181 -0.149,0.169 -0.155,0.151 -0.157,0.132|-0.924,0.186 -0.930,0.186 -0.942,0.204 -0.936,0.216|-0.936,0.216 -0.924,0.210 -0.907,0.204|-0.963,0.241 -0.948,0.233 -0.942,0.227 -0.936,0.216|-0.179,0.233 -0.190,0.245 -0.196,0.256 -0.194,0.272|-0.194,0.272 -0.202,0.286 -0.219,0.320 -0.219,0.338 -0.231,0.350 -0.229,0.365|0.194,0.297 0.200,0.303|-0.884,0.355 -0.884,0.332 -0.889,0.338 -0.884,0.355|-0.854,0.344 -0.843,0.338|-0.283,0.344 -0.301,0.355 -0.324,0.350|1.004,0.344 1.004,0.361 1.010,0.367 1.012,0.382|-0.229,0.365 -0.213,0.367 -0.155,0.355 -0.149,0.350 -0.138,0.350|-0.103,0.355 -0.126,0.355 -0.138,0.350|-0.831,0.367 -0.849,0.373 -0.872,0.390 -0.884,0.390|-0.318,0.385 -0.318,0.373 -0.324,0.350|-0.849,0.396 -0.825,0.396|1.012,0.382 0.998,0.396 0.993,0.431 0.981,0.443 0.967,0.429|0.967,0.429 0.975,0.408 0.969,0.414 0.967,0.429|-0.482,0.466 -0.464,0.449|-0.441,0.454 -0.458,0.478 -0.458,0.489 -0.476,0.513 -0.487,0.542|-0.487,0.478 -0.499,0.489|-0.566,0.586 -0.551,0.565 -0.516,0.513|-0.866,0.592 -0.872,0.577 -0.884,0.554 -0.884,0.524|-0.498,0.568 -0.476,0.559 -0.423,0.536 -0.408,0.530|-0.361,0.580 -0.359,0.565 -0.359,0.554 -0.342,0.536|-0.408,0.530 -0.406,0.548 -0.400,0.554 -0.400,0.565 -0.382,0.583 -0.377,0.583 -0.361,0.580|0.771,0.596 0.783,0.583 0.794,0.565|-0.790,0.571 -0.808,0.588 -0.849,0.594 -0.866,0.592|-0.689,0.585 -0.703,0.577 -0.732,0.594 -0.744,0.588 -0.744,0.583|-0.498,0.568 -0.522,0.577 -0.534,0.577 -0.540,0.583|-0.592,0.588 -0.656,0.594 -0.674,0.583 -0.689,0.585|-0.361,0.580 -0.348,0.594 -0.295,0.594 -0.282,0.610|-0.715,0.623 -0.697,0.600 -0.689,0.585|-0.254,0.606 -0.266,0.600 -0.282,0.610|-0.604,0.688 -0.598,0.653 -0.575,0.606 -0.566,0.586|-0.511,0.606 -0.522,0.664|-0.056,0.670 -0.068,0.670 -0.085,0.680|0.497,0.676 0.468,0.693 0.445,0.699 0.427,0.707|0.207,0.724 0.223,0.705 0.264,0.688|0.183,0.734 0.189,0.734 0.207,0.724|0.410,0.738 0.387,0.746 0.392,0.746 0.410,0.738"

/** Hai nét mắt nhắm (vẽ đậm hơn). */
private const val STROKES_EYE = "-0.348,0.070 -0.353,0.076 -0.353,0.122 -0.371,0.163 -0.394,0.192 -0.388,0.198|-0.621,0.425 -0.569,0.402 -0.522,0.350"

/** Phân tích chuỗi "x,y x,y|x,y ..." thành danh sách đa tuyến (chỉ chạy một lần). */
private fun parseStrokes(data: String): List<FloatArray> =
    data.split('|').filter { it.isNotBlank() }.map { stroke ->
        val pts = stroke.trim().split(' ')
        FloatArray(pts.size * 2).also { arr ->
            pts.forEachIndexed { i, p ->
                val xy = p.split(',')
                arr[i * 2] = xy[0].toFloat()
                arr[i * 2 + 1] = xy[1].toFloat()
            }
        }
    }

private val SILHOUETTE: FloatArray by lazy { parseStrokes(SILHOUETTE_DATA).first() }
private val MAIN_STROKES: List<FloatArray> by lazy { parseStrokes(STROKES_MAIN) }
private val FINE_STROKES: List<FloatArray> by lazy { parseStrokes(STROKES_FINE) }
private val EYE_STROKES: List<FloatArray> by lazy { parseStrokes(STROKES_EYE) }

/** Tâm đầu (đơn vị u) – điểm neo duy nhất cho phụ kiện và hiệu ứng Zzz. */
private const val HEAD_CX_U = -0.50f
private const val HEAD_CY_U = 0.18f
private const val HEAD_RADIUS_U = 0.34f

/** Góc nghiêng phụ bọc ngoài mũ/nơ (0f vì drawSleepingHeadwear đã tự căn chỉnh góc cho từng loại mũ). */
private const val HEADWEAR_TILT_DEG = 0f

/**
 * Vẽ mèo cuộn tròn ngủ (curled loaf) dáng ấm áp chuẩn tranh minh họa.
 *
 * @param tailWag Độ lệch góc vẫy đuôi theo độ (swing amplitude in degrees, thường dao động trong khoảng -4f..+4f khi ngủ).
 */
internal fun DrawScope.drawSleepingChibiCat(
    cx: Float,
    cy: Float,
    u: Float,
    baseU: Float,
    tailWag: Float,
    effectProgress: Float,
    sleepingBreathScale: Float,
    palette: CatColorPalette,
    wardrobe: EquippedWardrobe,
    legacyAccessory: PetAccessory? = null
) {
    // Độ dày nét: tỉ lệ theo u (ảnh mẫu ≈ 0.012u); sàn 1.6px để không biến mất khi vẽ nhỏ
    val lineW = (u * 0.014f).coerceAtLeast(1.6f)
    val strokeW = (baseU * 0.033f).coerceAtLeast(3.8f) // vẫn dùng cho phụ kiện/trang phục

    val isLightFur = palette.bodyColor.luminance() > 0.60f
    val outlineCol = when {
        palette.bodyColor.luminance() < 0.30f -> Color(0xFF2A242B) // mèo đen
        else -> Color(0xFF8C6A72)                                  // cocoa mauve như ảnh mẫu
    }

    val breath = (sleepingBreathScale - 1f) * 0.32f
    val wag = (tailWag / 10f) * (u * 0.018f)

    // Biến đổi 1 điểm (đơn vị u) → pixel, có nhịp thở ở lưng và vẫy nhẹ ở đáy đuôi
    fun tx(x: Float, y: Float): Float {
        val tailW = ((y - 0.45f) / 0.30f).coerceIn(0f, 1f)
        return cx + x * u + wag * tailW
    }
    fun ty(y: Float): Float {
        val lift = ((-y - 0.10f) / 0.65f).coerceIn(0f, 1f)
        return cy + y * u - breath * lift * (u * 0.10f)
    }
    fun polyline(path: Path, pts: FloatArray, close: Boolean = false) {
        var i = 0
        while (i < pts.size) {
            val px = tx(pts[i], pts[i + 1]); val py = ty(pts[i + 1])
            if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            i += 2
        }
        if (close) path.close()
    }
    fun pt(x: Float, y: Float) = Offset(tx(x, y), ty(y))

    // ── BƯỚC 1: Bóng đổ elip dưới sàn ──
    val shadowW = u * 2.25f
    val shadowH = u * 0.44f
    val shadowX = cx + u * 0.02f
    val shadowY = cy + u * 0.74f
    // Vầng bóng loang mềm mại
    drawOval(
        color = Color(0x332A1F1B),
        topLeft = Offset(shadowX - shadowW / 2f, shadowY - shadowH / 2f),
        size = Size(shadowW, shadowH)
    )
    // Lớp bóng đậm tiếp xúc sàn ngay sát bụng tạo trọng lực tiếp đất vững chãi
    drawOval(
        color = Color(0x543A2720),
        topLeft = Offset(shadowX - (shadowW * 0.78f) / 2f, shadowY - (shadowH * 0.65f) / 2f),
        size = Size(shadowW * 0.78f, shadowH * 0.65f)
    )

    // ── BƯỚC 2: Thân nguyên khối + màu nước pastel ──
    val bodyPath = Path().also { polyline(it, SILHOUETTE, close = true) }

    clipPath(bodyPath) {
        drawPath(bodyPath, color = palette.bodyColor)

        if (isLightFur) {
            fun wash(x: Float, y: Float, r: Float, c: Long, a: Int) {
                val col = Color(c)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(col.copy(alpha = a / 255f), col.copy(alpha = 0f)),
                        center = pt(x, y), radius = r * u
                    ),
                    radius = r * u, center = pt(x, y)
                )
            }
            wash(-0.20f, -0.50f, 0.85f, 0xFFFFE9C8, 110)  // vàng kem ấm trên lưng/đầu
            wash(0.85f, 0.00f, 0.60f, 0xFFD9EAF8, 120)    // xanh sương bên phải
            wash(0.70f, 0.55f, 0.60f, 0xFFFFD6DA, 150)    // hồng phấn đuôi phải
            wash(0.10f, 0.60f, 0.55f, 0xFFFFDDD6, 130)    // hồng đào dưới đuôi
            wash(-0.85f, 0.35f, 0.40f, 0xFFFFE3D8, 110)   // đào nhạt ở tai trái
        } else {
            // Lông sẫm: chỉ một quầng sáng nhẹ trên lưng cho có khối
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x30FFFFFF), Color(0x00FFFFFF)),
                    center = pt(0.1f, -0.45f), radius = u * 0.8f
                ),
                radius = u * 0.8f, center = pt(0.1f, -0.45f)
            )
        }

        // ── BƯỚC 3: Hoa văn skin (cắt theo thân) ──
        if (wardrobe.furSkinId == "fur_siamese") {
            drawCircle(
                color = palette.patchColor.copy(alpha = 0.75f),
                radius = u * 0.13f, center = pt(-0.34f, 0.54f)  // chóp đuôi
            )
            drawOval(
                color = palette.patchColor.copy(alpha = 0.50f),
                topLeft = pt(-0.62f, 0.10f), size = Size(u * 0.50f, u * 0.42f) // mặt nạ quanh mắt mũi
            )
        } else if (wardrobe.furSkinId == "fur_orange" || wardrobe.furSkinId == "fur_ginger") {
            listOf(
                Offset(0.05f, -0.62f) to Offset(0.02f, -0.46f),
                Offset(0.30f, -0.58f) to Offset(0.25f, -0.43f),
                Offset(0.58f, -0.42f) to Offset(0.50f, -0.30f)
            ).forEach { (s, e) ->
                drawLine(
                    color = palette.patchColor.copy(alpha = 0.65f),
                    start = pt(s.x, s.y), end = pt(e.x, e.y),
                    strokeWidth = lineW * 4.5f, cap = StrokeCap.Round
                )
            }
        } else if (wardrobe.furSkinId == "fur_calico") {
            // Đốm nâu trên lưng
            drawOval(
                color = palette.patchColor.copy(alpha = 0.85f),
                topLeft = pt(0.05f, -0.65f), size = Size(u * 0.40f, u * 0.35f)
            )
            // Đốm xám bên cạnh
            val secColor = palette.secondaryPatchColor ?: Color(0xFF6B6E78)
            drawOval(
                color = secColor.copy(alpha = 0.85f),
                topLeft = pt(0.35f, -0.50f), size = Size(u * 0.35f, u * 0.30f)
            )
        }
    }

    // ── BƯỚC 4: Ruột tai, mũi, má hồng ──
    fun fillPoly(coords: FloatArray, color: Color) {
        val p = Path().also { polyline(it, coords, close = true) }
        drawPath(p, color = color)
    }
    fillPoly(floatArrayOf(-0.581f, -0.289f, -0.567f, -0.103f, -0.427f, -0.126f), palette.innerEarColor)
    fillPoly(floatArrayOf(-1.033f, 0.386f, -0.936f, 0.309f, -0.829f, 0.309f, -0.829f, 0.402f, -0.936f, 0.425f), palette.innerEarColor)
    fillPoly(floatArrayOf(-0.379f, 0.322f, -0.322f, 0.303f, -0.344f, 0.357f), palette.noseColor)

    // Má hồng: elip nghiêng, mờ dần ra rìa (watercolor)
    listOf(
        floatArrayOf(-0.276f, 0.115f, 0.054f, 0.070f, -15.000f),
        floatArrayOf(-0.557f, 0.470f, 0.087f, 0.052f, -20.000f)
    ).forEach { (bx, by, rx, ry, deg) ->
        val c = pt(bx, by)
        withTransform({ rotate(deg, pivot = c) }) {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x99FFA8A4), Color(0x4DFFB8B4), Color(0x00FFB8B4)),
                    center = c, radius = rx * u * 1.25f
                ),
                topLeft = Offset(c.x - rx * u * 1.25f, c.y - ry * u * 1.25f),
                size = Size(rx * u * 2.5f, ry * u * 2.5f)
            )
        }
    }

    // ── BƯỚC 5: Nét viền (3 lớp: chính / mảnh / mắt) ──
    fun strokeLayer(list: List<FloatArray>, width: Float, alpha: Float) {
        val p = Path()
        list.forEach { polyline(p, it) }
        drawPath(
            path = p, color = outlineCol.copy(alpha = alpha),
            style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
    // Viền ngoài khép kín của thân
    drawPath(
        path = bodyPath, color = outlineCol.copy(alpha = 0.55f),
        style = Stroke(width = lineW * 0.8f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )
    strokeLayer(MAIN_STROKES, lineW, 0.90f)
    strokeLayer(FINE_STROKES, lineW * 0.60f, 0.70f)
    strokeLayer(EYE_STROKES, lineW * 1.30f, 1.00f)

    // ── BƯỚC 6: Trang phục thân (Bodywear) ──
    drawSleepingBodywear(wardrobe.getBodywear()?.id, cx, cy, u, strokeW)

    // ── BƯỚC 7: Vòng cổ / Khăn quàng (Neckwear) ──
    val headRadius = u * HEAD_RADIUS_U
    val headCx = cx + u * HEAD_CX_U
    val headCy = cy + u * HEAD_CY_U
    val effectiveNeckwear = wardrobe.getNeckwear()?.id ?: when (legacyAccessory) {
        PetAccessory.COLLAR -> "neck_bell_collar"
        PetAccessory.SCARF -> "neck_warm_scarf"
        PetAccessory.BOW_TIE -> "neck_blue_bow"
        else -> null
    }
    drawSleepingNeckwear(effectiveNeckwear, headCx, headCy, u, strokeW)

    // ── BƯỚC 8: Mũ / Nơ cài đầu (Headwear) ──
    val effectiveHeadwear = wardrobe.getHeadwear()?.id ?: when (legacyAccessory) {
        PetAccessory.RIBBON -> "head_ribbon"
        PetAccessory.HAT -> "head_straw_hat"
        else -> null
    }
    withTransform({ rotate(HEADWEAR_TILT_DEG, pivot = Offset(headCx, headCy)) }) {
        drawSleepingHeadwear(effectiveHeadwear, headCx, headCy, headRadius, u, strokeW)
    }

    // ── BƯỚC 9: Chữ Zzz bay lượn & ngôi sao giấc mơ ──
    drawSleepingZzzEffects(
        headCx = headCx,
        headCy = headCy,
        headRadius = headRadius,
        u = u,
        progress = effectProgress
    )
}

internal fun DrawScope.drawSleepingZzzEffects(
    headCx: Float,
    headCy: Float,
    headRadius: Float,
    u: Float,
    progress: Float
) {
    val startX = headCx + headRadius * 0.40f
    val startY = headCy - headRadius * 0.30f

    val zData = listOf(
        Triple(0.00f, 0.28f, u * 0.08f),
        Triple(0.18f, 0.60f, u * 0.12f),
        Triple(0.38f, 0.95f, u * 0.16f)
    )

    zData.forEachIndexed { index, (ox, _, zSize) ->
        val currentProgress = (progress + index * 0.33f) % 1.0f
        val zY = startY - currentProgress * u * 1.15f
        val swayX = sin(currentProgress * 2f * PI.toFloat()) * (u * 0.07f)
        val zX = startX + ox * u + swayX
        val alpha = sin(currentProgress * PI.toFloat()).coerceIn(0f, 1f) * 0.85f

        val zPath = Path().apply {
            moveTo(zX - zSize * 0.45f, zY - zSize * 0.45f)
            lineTo(zX + zSize * 0.45f, zY - zSize * 0.45f)
            lineTo(zX - zSize * 0.40f, zY + zSize * 0.45f)
            lineTo(zX + zSize * 0.48f, zY + zSize * 0.45f)
        }
        drawPath(
            path = zPath,
            color = Color(0xFF6B8BA4).copy(alpha = alpha),
            style = Stroke(width = (zSize * 0.22f).coerceAtLeast(3f), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        if (alpha > 0.40f) {
            val bubbleRadius = (zSize * 0.22f) * (1f - currentProgress * 0.3f)
            drawCircle(
                color = Color(0xFFB8D8E8).copy(alpha = alpha * 0.70f),
                radius = bubbleRadius,
                center = Offset(zX - zSize * 0.55f, zY + zSize * 0.30f)
            )
        }

        if (index == 2 && alpha > 0.35f) {
            val starX = zX + zSize * 0.65f
            val starY = zY - zSize * 0.25f
            val starR = zSize * 0.32f
            val starPath = Path().apply {
                moveTo(starX, starY - starR)
                quadraticTo(starX, starY, starX + starR, starY)
                quadraticTo(starX, starY, starX, starY + starR)
                quadraticTo(starX, starY, starX - starR, starY)
                quadraticTo(starX, starY, starX, starY - starR)
                close()
            }
            drawPath(starPath, color = Color(0xFFFFD54F).copy(alpha = alpha))
        }
    }
}