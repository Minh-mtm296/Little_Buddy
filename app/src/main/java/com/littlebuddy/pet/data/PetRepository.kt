package com.littlebuddy.pet.data

import com.littlebuddy.pet.pet.PetState

/**
 * Repository làm cầu nối trung gian giữa ViewModel và tầng lưu trữ dữ liệu
 */
class PetRepository(private val preferences: PetPreferences) {

    fun getInitialPetState(): PetState {
        return preferences.loadPetState()
    }

    fun persistPetState(state: PetState) {
        preferences.savePetState(state)
    }

    fun resetPetState(): PetState {
        return preferences.resetToDefault()
    }
}
