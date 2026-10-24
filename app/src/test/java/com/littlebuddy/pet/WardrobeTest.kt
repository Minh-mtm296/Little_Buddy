package com.littlebuddy.pet

import com.littlebuddy.pet.pet.PetState
import com.littlebuddy.pet.wardrobe.EquippedWardrobe
import com.littlebuddy.pet.wardrobe.WardrobeCategory
import com.littlebuddy.pet.wardrobe.WardrobeRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Test kiểm tra toàn bộ logic hệ thống tủ đồ và cá nhân hóa thú cưng:
 * - 4 danh mục trang bị
 * - Quy tắc mỗi danh mục tối đa 1 món
 * - Phối hợp trang bị đa danh mục
 * - Tháo trang bị
 * - Không ảnh hưởng đến chỉ số/cấp độ/tên thú cưng
 */
class WardrobeTest {

    @Test
    fun registry_hasItemsInAllFourCategories() {
        val furSkins = WardrobeRegistry.getItemsForCategory(WardrobeCategory.FUR_SKIN)
        val headwear = WardrobeRegistry.getItemsForCategory(WardrobeCategory.HEADWEAR)
        val neckwear = WardrobeRegistry.getItemsForCategory(WardrobeCategory.NECKWEAR)
        val bodywear = WardrobeRegistry.getItemsForCategory(WardrobeCategory.BODYWEAR)

        assertTrue("Cần có ít nhất 3 màu lông", furSkins.size >= 3)
        assertTrue("Cần có ít nhất 3 mũ/nơ", headwear.size >= 3)
        assertTrue("Cần có ít nhất 3 vòng/khăn", neckwear.size >= 3)
        assertTrue("Cần có ít nhất 3 trang phục thân", bodywear.size >= 3)
    }

    @Test
    fun calico_furSkin_existsAndHasSecondaryPatchColor() {
        val calico = WardrobeRegistry.getItem(WardrobeRegistry.ID_FUR_CALICO)
        assertNotNull(calico)
        assertEquals("Mèo Tam Thể", calico?.name)
        assertNotNull(calico?.palette?.secondaryPatchColor)
        assertEquals(2, calico?.levelRequired)
    }

    @Test
    fun defaultWardrobe_hasSiameseFurAndNoAccessories() {
        val wardrobe = EquippedWardrobe()

        assertEquals("fur_siamese", wardrobe.furSkinId)
        assertNull(wardrobe.headwearId)
        assertNull(wardrobe.neckwearId)
        assertNull(wardrobe.bodywearId)
        assertEquals("Mèo Xiêm", wardrobe.getFurSkin().name)
    }

    @Test
    fun equipItem_replacesOldItemInSameCategory() {
        var wardrobe = EquippedWardrobe()

        // Mặc Nơ hồng
        wardrobe = wardrobe.withEquipped(WardrobeRegistry.HEAD_RIBBON)
        assertEquals("head_ribbon", wardrobe.headwearId)

        // Đổi sang Mũ cói (cùng category HEADWEAR) -> Phải thay thế chứ không chồng đè
        wardrobe = wardrobe.withEquipped(WardrobeRegistry.HEAD_STRAW_HAT)
        assertEquals("head_straw_hat", wardrobe.headwearId)
    }

    @Test
    fun equipMultipleCategories_combinesEquippedItems() {
        var wardrobe = EquippedWardrobe()

        wardrobe = wardrobe
            .withEquipped(WardrobeRegistry.FUR_GINGER)
            .withEquipped(WardrobeRegistry.HEAD_BERET)
            .withEquipped(WardrobeRegistry.NECK_WARM_SCARF)
            .withEquipped(WardrobeRegistry.BODY_SWEATER)

        assertEquals("fur_ginger", wardrobe.furSkinId)
        assertEquals("head_beret", wardrobe.headwearId)
        assertEquals("neck_warm_scarf", wardrobe.neckwearId)
        assertEquals("body_sweater", wardrobe.bodywearId)

        assertTrue(wardrobe.isEquipped("fur_ginger"))
        assertTrue(wardrobe.isEquipped("head_beret"))
        assertTrue(wardrobe.isEquipped("neck_warm_scarf"))
        assertTrue(wardrobe.isEquipped("body_sweater"))
    }

    @Test
    fun unequipCategory_removesOnlyTargetCategory() {
        var wardrobe = EquippedWardrobe(
            furSkinId = "fur_black",
            headwearId = "head_crown",
            neckwearId = "neck_bell_collar",
            bodywearId = "body_royal_cape"
        )

        // Tháo mũ
        wardrobe = wardrobe.withUnequipped(WardrobeCategory.HEADWEAR)
        assertNull(wardrobe.headwearId)
        assertEquals("fur_black", wardrobe.furSkinId)
        assertEquals("neck_bell_collar", wardrobe.neckwearId)
        assertEquals("body_royal_cape", wardrobe.bodywearId)
    }

    @Test
    fun unequipAllAccessories_preservesFurSkin() {
        val wardrobe = EquippedWardrobe(
            furSkinId = "fur_grey",
            headwearId = "head_ribbon",
            neckwearId = "neck_pearl",
            bodywearId = "body_overalls"
        )

        val unequipped = wardrobe.unequipAllAccessories()
        assertEquals("fur_grey", unequipped.furSkinId)
        assertNull(unequipped.headwearId)
        assertNull(unequipped.neckwearId)
        assertNull(unequipped.bodywearId)
    }

    @Test
    fun petState_updatingWardrobePreservesStatsAndName() {
        val initialPet = PetState(
            name = "Bé Bơ",
            level = 5,
            exp = 40,
            hunger = 85f,
            happiness = 90f,
            energy = 75f
        )

        val newWardrobe = EquippedWardrobe(
            furSkinId = "fur_snow",
            headwearId = "head_flower_crown"
        )

        val updatedPet = initialPet.copy(wardrobe = newWardrobe)

        assertEquals("Bé Bơ", updatedPet.name)
        assertEquals(5, updatedPet.level)
        assertEquals(40, updatedPet.exp)
        assertEquals(85f, updatedPet.hunger, 0.01f)
        assertEquals(90f, updatedPet.happiness, 0.01f)
        assertEquals(75f, updatedPet.energy, 0.01f)
        assertEquals("fur_snow", updatedPet.wardrobe.furSkinId)
        assertEquals("head_flower_crown", updatedPet.wardrobe.headwearId)
    }

    @Test
    fun levelRequirements_areConfiguredReasonably() {
        // Cấp 1 có sẵn các món cơ bản
        val lv1Fur = WardrobeRegistry.FUR_SIAMESE.levelRequired
        assertEquals(1, lv1Fur)

        val lv1Head = WardrobeRegistry.HEAD_RIBBON.levelRequired
        assertEquals(1, lv1Head)

        // Các món cao cấp yêu cầu cấp độ cao hơn
        assertTrue(WardrobeRegistry.FUR_GREY.levelRequired > 1)
        assertTrue(WardrobeRegistry.HEAD_CROWN.levelRequired > 1)
        assertTrue(WardrobeRegistry.BODY_ROYAL_CAPE.levelRequired > 1)
    }
}
