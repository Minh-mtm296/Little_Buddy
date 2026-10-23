package com.littlebuddy.pet.wardrobe

import androidx.compose.ui.graphics.Color
import com.littlebuddy.pet.pet.PetAccessory

/**
 * 4 Danh mục trang bị và cá nhân hóa cho thú cưng
 */
enum class WardrobeCategory(val displayName: String, val icon: String) {
    FUR_SKIN("Màu lông", "🐱"),
    HEADWEAR("Nơ & Mũ", "👒"),
    NECKWEAR("Vòng & Khăn", "🧣"),
    BODYWEAR("Trang phục", "👕")
}

/**
 * Bảng màu lông chi tiết để PetCanvas vẽ nhân vật mèo theo phong cách hoạt họa vector
 */
data class CatColorPalette(
    val bodyColor: Color,        // Màu lông thân chính (trắng, vàng, đen, xám...)
    val patchColor: Color,       // Màu đốm đặc trưng (mặt nạ, 2 tai, 2 bàn tay, đuôi)
    val outlineColor: Color,     // Màu viền nét thân và các chi tiết
    val innerEarColor: Color,    // Màu lòng tai trong
    val bellyColor: Color,       // Màu bóng mờ dưới đáy bụng
    val eyeColor: Color,         // Màu mắt hạt đậu
    val noseColor: Color         // Màu mũi nhỏ xinh
)

/**
 * Định nghĩa một vật phẩm trang bị trong tủ đồ
 */
data class WardrobeItem(
    val id: String,
    val name: String,
    val description: String,
    val category: WardrobeCategory,
    val levelRequired: Int = 1,
    val previewPrimaryColor: Color,
    val previewSecondaryColor: Color = previewPrimaryColor,
    val palette: CatColorPalette? = null,
    val legacyAccessory: PetAccessory? = null
)

/**
 * Bộ trang phục hiện đang được trang bị của mèo (lưu bằng ID duy nhất)
 */
data class EquippedWardrobe(
    val furSkinId: String = "fur_siamese",
    val headwearId: String? = null,
    val neckwearId: String? = null,
    val bodywearId: String? = null
) {
    fun getFurSkin(): WardrobeItem = WardrobeRegistry.getFurSkin(furSkinId)
    fun getHeadwear(): WardrobeItem? = headwearId?.let { WardrobeRegistry.getItem(it) }
    fun getNeckwear(): WardrobeItem? = neckwearId?.let { WardrobeRegistry.getItem(it) }
    fun getBodywear(): WardrobeItem? = bodywearId?.let { WardrobeRegistry.getItem(it) }

    fun getItemForCategory(category: WardrobeCategory): WardrobeItem? = when (category) {
        WardrobeCategory.FUR_SKIN -> getFurSkin()
        WardrobeCategory.HEADWEAR -> getHeadwear()
        WardrobeCategory.NECKWEAR -> getNeckwear()
        WardrobeCategory.BODYWEAR -> getBodywear()
    }

    fun isEquipped(itemId: String): Boolean =
        furSkinId == itemId || headwearId == itemId || neckwearId == itemId || bodywearId == itemId

    fun withEquipped(item: WardrobeItem): EquippedWardrobe = when (item.category) {
        WardrobeCategory.FUR_SKIN -> copy(furSkinId = item.id)
        WardrobeCategory.HEADWEAR -> copy(headwearId = if (headwearId == item.id) null else item.id)
        WardrobeCategory.NECKWEAR -> copy(neckwearId = if (neckwearId == item.id) null else item.id)
        WardrobeCategory.BODYWEAR -> copy(bodywearId = if (bodywearId == item.id) null else item.id)
    }

    fun withUnequipped(category: WardrobeCategory): EquippedWardrobe = when (category) {
        WardrobeCategory.FUR_SKIN -> this // Luôn có màu lông mặc định
        WardrobeCategory.HEADWEAR -> copy(headwearId = null)
        WardrobeCategory.NECKWEAR -> copy(neckwearId = null)
        WardrobeCategory.BODYWEAR -> copy(bodywearId = null)
    }

    fun unequipAllAccessories(): EquippedWardrobe = copy(
        headwearId = null,
        neckwearId = null,
        bodywearId = null
    )
}

/**
 * Kho dữ liệu đăng ký toàn bộ phụ kiện và màu lông của Little Buddy
 */
object WardrobeRegistry {

    const val DEFAULT_FUR_ID = "fur_siamese"

    // ── HẰNG SỐ ID PHỤ KIỆN & MÀU LÔNG ─────────────────────────────────────
    const val ID_FUR_SIAMESE = "fur_siamese"
    const val ID_FUR_GINGER = "fur_ginger"
    const val ID_FUR_BLACK = "fur_black"
    const val ID_FUR_SNOW = "fur_snow"
    const val ID_FUR_GREY = "fur_grey"

    const val ID_HEAD_RIBBON = "head_ribbon"
    const val ID_HEAD_STRAW_HAT = "head_straw_hat"
    const val ID_HEAD_BERET = "head_beret"
    const val ID_HEAD_FLOWER_CROWN = "head_flower_crown"
    const val ID_HEAD_CROWN = "head_crown"

    const val ID_NECK_BELL_COLLAR = "neck_bell_collar"
    const val ID_NECK_WARM_SCARF = "neck_warm_scarf"
    const val ID_NECK_BLUE_BOW = "neck_blue_bow"
    const val ID_NECK_PEARL = "neck_pearl"

    const val ID_BODY_SWEATER = "body_sweater"
    const val ID_BODY_OVERALLS = "body_overalls"
    const val ID_BODY_ROYAL_CAPE = "body_royal_cape"
    const val ID_BODY_CAPE = ID_BODY_ROYAL_CAPE

    // ── 1. MÀU LÔNG (FUR SKINS) ──────────────────────────────────────────────

    val FUR_SIAMESE = WardrobeItem(
        id = ID_FUR_SIAMESE,
        name = "Mèo Xiêm",
        description = "Bộ lông kem sữa ấm áp với mặt nạ đốm màu mocha cổ điển.",
        category = WardrobeCategory.FUR_SKIN,
        levelRequired = 1,
        previewPrimaryColor = Color(0xFFFAF5EE),
        previewSecondaryColor = Color(0xFFD5BEB1),
        palette = CatColorPalette(
            bodyColor = Color(0xFFFAF5EE),
            patchColor = Color(0xFFD5BEB1),
            outlineColor = Color(0xFF7E5657),
            innerEarColor = Color(0xFFD5BEB1),
            bellyColor = Color(0xFFEBE3D7),
            eyeColor = Color(0xFF5D3E44),
            noseColor = Color(0xFF5D3E44)
        )
    )

    val FUR_GINGER = WardrobeItem(
        id = ID_FUR_GINGER,
        name = "Mèo Cam",
        description = "Chú mèo vàng cam mật ong tinh nghịch, ấm áp và phúng phính.",
        category = WardrobeCategory.FUR_SKIN,
        levelRequired = 1,
        previewPrimaryColor = Color(0xFFFFF2DC),
        previewSecondaryColor = Color(0xFFF7A258),
        palette = CatColorPalette(
            bodyColor = Color(0xFFFFF2DC),
            patchColor = Color(0xFFF7A258),
            outlineColor = Color(0xFFB35622),
            innerEarColor = Color(0xFFFDC5B0),
            bellyColor = Color(0xFFF5E4C8),
            eyeColor = Color(0xFF7A3B12),
            noseColor = Color(0xFFD46852)
        )
    )

    val FUR_BLACK = WardrobeItem(
        id = ID_FUR_BLACK,
        name = "Mèo Mun",
        description = "Mèo đen mun tuyền bí ẩn với đôi mắt vàng sáng lung linh.",
        category = WardrobeCategory.FUR_SKIN,
        levelRequired = 2,
        previewPrimaryColor = Color(0xFF3B3840),
        previewSecondaryColor = Color(0xFFFFD152),
        palette = CatColorPalette(
            bodyColor = Color(0xFF45414B),
            patchColor = Color(0xFF302D36),
            outlineColor = Color(0xFF1E1C21),
            innerEarColor = Color(0xFFE89E92),
            bellyColor = Color(0xFF38343D),
            eyeColor = Color(0xFFFFD152),
            noseColor = Color(0xFF6B4B57)
        )
    )

    val FUR_SNOW = WardrobeItem(
        id = ID_FUR_SNOW,
        name = "Mèo Tuyết",
        description = "Bộ lông trắng tinh khôi như bông tuyết mùa đông dịu dàng.",
        category = WardrobeCategory.FUR_SKIN,
        levelRequired = 3,
        previewPrimaryColor = Color(0xFFFFFFFF),
        previewSecondaryColor = Color(0xFFFFAEC0),
        palette = CatColorPalette(
            bodyColor = Color(0xFFFFFFFF),
            patchColor = Color(0xFFF4ECE6),
            outlineColor = Color(0xFF8C7479),
            innerEarColor = Color(0xFFFFAEC0),
            bellyColor = Color(0xFFEDE4DC),
            eyeColor = Color(0xFF4FA0E2),
            noseColor = Color(0xFFF2829D)
        )
    )

    val FUR_GREY = WardrobeItem(
        id = ID_FUR_GREY,
        name = "Mèo Xám",
        description = "Mèo Anh lông ngắn màu xám bạc pastel lịch thiệp.",
        category = WardrobeCategory.FUR_SKIN,
        levelRequired = 4,
        previewPrimaryColor = Color(0xFFD3DAE2),
        previewSecondaryColor = Color(0xFF909CA8),
        palette = CatColorPalette(
            bodyColor = Color(0xFFD3DAE2),
            patchColor = Color(0xFF909CA8),
            outlineColor = Color(0xFF4E5966),
            innerEarColor = Color(0xFFDDBDC2),
            bellyColor = Color(0xFFC0CAD4),
            eyeColor = Color(0xFF38A169),
            noseColor = Color(0xFF5A6675)
        )
    )

    // ── 2. NƠ & MŨ CÀI ĐẦU (HEADWEAR) ───────────────────────────────────────

    val HEAD_RIBBON = WardrobeItem(
        id = ID_HEAD_RIBBON,
        name = "Nơ Hồng",
        description = "Chiếc nơ ruy băng hồng pastel ngọt ngào cài bên tai.",
        category = WardrobeCategory.HEADWEAR,
        levelRequired = 1,
        previewPrimaryColor = Color(0xFFFF8FAB),
        legacyAccessory = PetAccessory.RIBBON
    )

    val HEAD_STRAW_HAT = WardrobeItem(
        id = ID_HEAD_STRAW_HAT,
        name = "Mũ Cói Hoa",
        description = "Chiếc mũ cói vành tròn đồng quê thắt nơ cúc nhí.",
        category = WardrobeCategory.HEADWEAR,
        levelRequired = 1,
        previewPrimaryColor = Color(0xFFE8C28A),
        legacyAccessory = PetAccessory.HAT
    )

    val HEAD_BERET = WardrobeItem(
        id = ID_HEAD_BERET,
        name = "Mũ Nồi Beret",
        description = "Mũ nồi vàng nghệ sĩ cổ điển phong cách họa sĩ thu nhỏ.",
        category = WardrobeCategory.HEADWEAR,
        levelRequired = 2,
        previewPrimaryColor = Color(0xFFE5B03B)
    )

    val HEAD_FLOWER_CROWN = WardrobeItem(
        id = ID_HEAD_FLOWER_CROWN,
        name = "Vòng Hoa Cúc",
        description = "Vòng hoa cúc nhí mùa thu đan kết xinh xắn cài trên đầu.",
        category = WardrobeCategory.HEADWEAR,
        levelRequired = 3,
        previewPrimaryColor = Color(0xFFF9D162),
        previewSecondaryColor = Color(0xFFFFFFFF)
    )

    val HEAD_CROWN = WardrobeItem(
        id = ID_HEAD_CROWN,
        name = "Vương Miện",
        description = "Vương miện vàng hoàng gia đính viên ngọc ruby lấp lánh.",
        category = WardrobeCategory.HEADWEAR,
        levelRequired = 5,
        previewPrimaryColor = Color(0xFFFFD700)
    )

    // ── 3. VÒNG CỔ & KHĂN QUÀNG (NECKWEAR) ───────────────────────────────────

    val NECK_BELL_COLLAR = WardrobeItem(
        id = ID_NECK_BELL_COLLAR,
        name = "Vòng Chuông Vàng",
        description = "Vòng cổ đỏ thắm gắn quả chuông vàng kêu leng keng vui tai.",
        category = WardrobeCategory.NECKWEAR,
        levelRequired = 1,
        previewPrimaryColor = Color(0xFFC84B31),
        previewSecondaryColor = Color(0xFFFFD700),
        legacyAccessory = PetAccessory.COLLAR
    )

    val NECK_WARM_SCARF = WardrobeItem(
        id = ID_NECK_WARM_SCARF,
        name = "Khăn Quàng Ấm",
        description = "Chiếc khăn len quàng cổ màu cam đất giữ ấm ngày se lạnh.",
        category = WardrobeCategory.NECKWEAR,
        levelRequired = 1,
        previewPrimaryColor = Color(0xFFE07A5F),
        legacyAccessory = PetAccessory.SCARF
    )

    val NECK_BLUE_BOW = WardrobeItem(
        id = ID_NECK_BLUE_BOW,
        name = "Nơ Cổ Quý Tộc",
        description = "Nơ cổ cánh bướm màu xanh mint trang nhã cho bé mèo.",
        category = WardrobeCategory.NECKWEAR,
        levelRequired = 2,
        previewPrimaryColor = Color(0xFF76B9C4),
        legacyAccessory = PetAccessory.BOW_TIE
    )

    val NECK_PEARL = WardrobeItem(
        id = ID_NECK_PEARL,
        name = "Vòng Ngọc Trai",
        description = "Chuỗi hạt ngọc trai thanh lịch, quý phái.",
        category = WardrobeCategory.NECKWEAR,
        levelRequired = 3,
        previewPrimaryColor = Color(0xFFF0EFEB)
    )

    // ── 4. TRANG PHỤC THÂN NGƯỜI (BODYWEAR) ──────────────────────────────────

    val BODY_SWEATER = WardrobeItem(
        id = ID_BODY_SWEATER,
        name = "Áo Len Mùa Đông",
        description = "Chiếc áo len dệt sọc ấm áp quấn vừa vặn quanh bụng ú của mèo.",
        category = WardrobeCategory.BODYWEAR,
        levelRequired = 2,
        previewPrimaryColor = Color(0xFFE8927C),
        previewSecondaryColor = Color(0xFFFFF2D8)
    )

    val BODY_OVERALLS = WardrobeItem(
        id = ID_BODY_OVERALLS,
        name = "Yếm Bò Xanh",
        description = "Quần yếm denim có túi nhỏ trước ngực, phong cách tinh nghịch.",
        category = WardrobeCategory.BODYWEAR,
        levelRequired = 3,
        previewPrimaryColor = Color(0xFF6B9AC4)
    )

    val BODY_ROYAL_CAPE = WardrobeItem(
        id = ID_BODY_ROYAL_CAPE,
        name = "Áo Choàng Hoàng Gia",
        description = "Áo choàng nhung đỏ thắm viền lông trắng quyền uy.",
        category = WardrobeCategory.BODYWEAR,
        levelRequired = 4,
        previewPrimaryColor = Color(0xFF9E2A2B),
        previewSecondaryColor = Color(0xFFFFFFFF)
    )

    // ── TOÀN BỘ DANH SÁCH ───────────────────────────────────────────────────

    val ALL_ITEMS: List<WardrobeItem> = listOf(
        FUR_SIAMESE, FUR_GINGER, FUR_BLACK, FUR_SNOW, FUR_GREY,
        HEAD_RIBBON, HEAD_STRAW_HAT, HEAD_BERET, HEAD_FLOWER_CROWN, HEAD_CROWN,
        NECK_BELL_COLLAR, NECK_WARM_SCARF, NECK_BLUE_BOW, NECK_PEARL,
        BODY_SWEATER, BODY_OVERALLS, BODY_ROYAL_CAPE
    )

    private val ITEMS_BY_ID = ALL_ITEMS.associateBy { it.id }

    fun getItem(id: String): WardrobeItem? = ITEMS_BY_ID[id]

    fun getFurSkin(id: String): WardrobeItem = ITEMS_BY_ID[id] ?: FUR_SIAMESE

    fun getItemsForCategory(category: WardrobeCategory): List<WardrobeItem> =
        ALL_ITEMS.filter { it.category == category }
}
