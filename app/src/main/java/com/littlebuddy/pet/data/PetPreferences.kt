package com.littlebuddy.pet.data

import android.content.Context
import android.content.SharedPreferences
import com.littlebuddy.pet.pet.PetAccessory
import com.littlebuddy.pet.pet.PetState
import com.littlebuddy.pet.pet.PetType

/**
 * Quản lý lưu trữ trạng thái thú cưng vào SharedPreferences của Android.
 * Đảm bảo dữ liệu được bảo toàn khi người dùng thoát và mở lại ứng dụng.
 */
class PetPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "little_buddy_pet_prefs"
        private const val KEY_PET_NAME = "key_pet_name"
        private const val KEY_PET_TYPE = "key_pet_type"
        private const val KEY_HUNGER = "key_hunger"
        private const val KEY_HAPPINESS = "key_happiness"
        private const val KEY_ENERGY = "key_energy"
        private const val KEY_IS_SLEEPING = "key_is_sleeping"
        private const val KEY_INTIMACY_SCORE = "key_intimacy_score"
        private const val KEY_LEVEL = "key_level"
        private const val KEY_EXP = "key_exp"
        private const val KEY_ACCESSORY = "key_accessory"
        private const val KEY_FUR_SKIN_ID = "key_fur_skin_id"
        private const val KEY_HEADWEAR_ID = "key_headwear_id"
        private const val KEY_NECKWEAR_ID = "key_neckwear_id"
        private const val KEY_BODYWEAR_ID = "key_bodywear_id"
        private const val KEY_LAST_SAVED = "key_last_saved"
    }

    /**
     * Tải trạng thái thú cưng đã lưu
     */
    fun loadPetState(): PetState {
        val rawName = prefs.getString(KEY_PET_NAME, PetState.DEFAULT_PET_NAME) ?: PetState.DEFAULT_PET_NAME
        val name = PetState.sanitizeName(rawName) ?: PetState.DEFAULT_PET_NAME
        val petTypeName = prefs.getString(KEY_PET_TYPE, PetType.CAT.name) ?: PetType.CAT.name
        val petType = try { PetType.valueOf(petTypeName) } catch (_: Exception) { PetType.CAT }

        val hunger = prefs.getFloat(KEY_HUNGER, 70f).coerceIn(0f, 100f)
        val happiness = prefs.getFloat(KEY_HAPPINESS, 80f).coerceIn(0f, 100f)
        val energy = prefs.getFloat(KEY_ENERGY, 85f).coerceIn(0f, 100f)
        val intimacyScore = prefs.getInt(KEY_INTIMACY_SCORE, 15).coerceIn(0, PetState.MAX_INTIMACY_SCORE)
        val level = prefs.getInt(KEY_LEVEL, 1).coerceAtLeast(1)
        val exp = prefs.getInt(KEY_EXP, 0).coerceAtLeast(0)
        val isSleeping = prefs.getBoolean(KEY_IS_SLEEPING, false)
        val lastSaved = prefs.getLong(KEY_LAST_SAVED, System.currentTimeMillis())

        val accessoryName = prefs.getString(KEY_ACCESSORY, null)
        val accessory = accessoryName?.let {
            try { PetAccessory.valueOf(it) } catch (_: Exception) { null }
        }

        // Tải trang phục tủ đồ theo ID duy nhất
        val furSkinId = prefs.getString(KEY_FUR_SKIN_ID, "fur_siamese") ?: "fur_siamese"
        val headwearId = prefs.getString(KEY_HEADWEAR_ID, null)
        val neckwearId = prefs.getString(KEY_NECKWEAR_ID, null)
        val bodywearId = prefs.getString(KEY_BODYWEAR_ID, null)
        val wardrobe = com.littlebuddy.pet.wardrobe.EquippedWardrobe(
            furSkinId = furSkinId,
            headwearId = headwearId,
            neckwearId = neckwearId,
            bodywearId = bodywearId
        )

        // Passive decay & recovery: tính toán chỉ số thống nhất thông qua PetState.applyTimeElapsed
        val elapsedMinutes = ((System.currentTimeMillis() - lastSaved) / (1000 * 60)).coerceAtLeast(0)
        val loadedState = PetState(
            name = name,
            petType = petType,
            hunger = hunger,
            happiness = happiness,
            energy = energy,
            level = level,
            exp = exp,
            intimacyScore = intimacyScore,
            accessory = accessory,
            wardrobe = wardrobe,
            isSleeping = isSleeping,
            feedbackMessage = "Chào mừng bạn quay lại với $name!"
        )

        return loadedState.applyTimeElapsed(elapsedMinutes)
    }

    /**
     * Lưu trạng thái thú cưng hiện tại
     */
    fun savePetState(state: PetState) {
        prefs.edit().apply {
            putString(KEY_PET_NAME, state.name)
            putString(KEY_PET_TYPE, state.petType.name)
            putFloat(KEY_HUNGER, state.hunger)
            putFloat(KEY_HAPPINESS, state.happiness)
            putFloat(KEY_ENERGY, state.energy)
            putInt(KEY_INTIMACY_SCORE, state.intimacyScore)
            putInt(KEY_LEVEL, state.level)
            putInt(KEY_EXP, state.exp)
            putBoolean(KEY_IS_SLEEPING, state.isSleeping)
            putString(KEY_ACCESSORY, state.accessory?.name)
            putString(KEY_FUR_SKIN_ID, state.wardrobe.furSkinId)
            putString(KEY_HEADWEAR_ID, state.wardrobe.headwearId)
            putString(KEY_NECKWEAR_ID, state.wardrobe.neckwearId)
            putString(KEY_BODYWEAR_ID, state.wardrobe.bodywearId)
            putLong(KEY_LAST_SAVED, System.currentTimeMillis())
            apply()
        }
    }

    /**
     * Khôi phục trạng thái mặc định ban đầu
     */
    fun resetToDefault(): PetState {
        prefs.edit().clear().apply()
        return PetState()
    }
}
