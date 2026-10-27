package com.littlebuddy.pet.pet

/**
 * Trạng thái và các chỉ số của thú cưng
 */
data class PetState(
    val name: String = DEFAULT_PET_NAME,
    val petType: PetType = PetType.CAT,
    val hunger: Float = 75f,
    val happiness: Float = 85f,
    val energy: Float = 90f,
    val exp: Int = 20,
    val level: Int = 1,
    val intimacyScore: Int = 15,
    val accessory: PetAccessory? = null,
    val wardrobe: com.littlebuddy.pet.wardrobe.EquippedWardrobe = com.littlebuddy.pet.wardrobe.EquippedWardrobe(),
    val isSleeping: Boolean = false,
    val temporaryMood: PetMood? = null,
    val feedbackMessage: String = "",
    val lastInteractionTimestamp: Long = System.currentTimeMillis()
) {
    companion object {
        const val DEFAULT_PET_NAME = "Momo"
        const val MAX_NAME_LENGTH = 18
        const val MAX_INTIMACY_SCORE = 200

        const val HUNGER_DECAY_PER_MINUTE = 0.2f
        /** Giảm 75% tiêu hao đói khi ngủ để tránh kiệt sức khi qua đêm */
        const val SLEEP_HUNGER_DECAY_PER_MINUTE = 0.05f
        const val HAPPINESS_DECAY_PER_MINUTE = 0.15f
        const val SLEEP_ENERGY_RECOVER_PER_MINUTE = 4.0f

        fun sanitizeName(rawName: String): String? {
            val cleaned = rawName.trim().replace(Regex("\\s+"), " ")
            if (cleaned.isBlank()) return null
            return cleaned.take(MAX_NAME_LENGTH)
        }
    }

    val petName: String
        get() = name.ifBlank { DEFAULT_PET_NAME }

    val intimacyLevel: Int
        get() = (intimacyScore / 50) + 1

    val intimacyTitle: String
        get() = when (intimacyLevel) {
            1 -> "Bạn mới quen"
            2 -> "Bạn thân thiết"
            3 -> "Tri kỷ ngọt ngào"
            else -> "Gia đình ấm áp"
        }

    val levelTitle: String
        get() = when (level) {
            1 -> "Bé mèo đáng yêu"
            2 -> "Bé mèo tinh nghịch"
            3 -> "Bé mèo hoạt bát"
            4 -> "Bé mèo thông thái"
            5 -> "Bé mèo thân thiết"
            else -> "Siêu sao đáng yêu"
        }

    val expToNextLevel: Int get() = 100

    val expProgress: Float get() = (exp.toFloat() / expToNextLevel).coerceIn(0f, 1f)

    val currentMood: PetMood
        get() {
            temporaryMood?.let { return it }
            return when {
                isSleeping -> PetMood.SLEEPY
                hunger <= 30f -> PetMood.HUNGRY
                energy <= 20f -> PetMood.SLEEPY
                happiness >= 75f -> PetMood.HAPPY
                happiness >= 50f -> PetMood.PLAYFUL
                else -> PetMood.NEUTRAL
            }
        }

    val canPlay: Boolean
        get() = !isSleeping && energy >= 15f && hunger >= 15f

    fun feed(food: BowlFood = BowlFood.DRY_FOOD): PetState {
        if (isSleeping) {
            return copy(feedbackMessage = "$petName đang ngủ say, đừng đánh thức bé nha!")
        }
        val hungerGain = if (food == BowlFood.EMPTY) 30f else food.hungerBoost
        val happinessGain = if (food == BowlFood.EMPTY) 10f else food.happinessBoost
        val newHunger = (hunger + hungerGain).coerceIn(0f, 100f)
        val newHappiness = (happiness + happinessGain).coerceIn(0f, 100f)
        val (newExp, newLevel) = gainExp(15)
        return copy(
            hunger = newHunger,
            happiness = newHappiness,
            exp = newExp,
            level = newLevel,
            intimacyScore = (intimacyScore + 5).coerceAtMost(MAX_INTIMACY_SCORE),
            temporaryMood = null,
            feedbackMessage = "$petName ăn no rồi~🐾",
            lastInteractionTimestamp = System.currentTimeMillis()
        )
    }

    fun pet(): PetState {
        if (isSleeping) {
            return copy(
                happiness = (happiness + 5f).coerceIn(0f, 100f),
                intimacyScore = (intimacyScore + 2).coerceAtMost(MAX_INTIMACY_SCORE),
                lastInteractionTimestamp = System.currentTimeMillis(),
                feedbackMessage = "$petName khẽ cựa quậy ..."
            )
        }
        val newHappiness = (happiness + 15f).coerceIn(0f, 100f)
        val (newExp, newLevel) = gainExp(10)
        return copy(
            happiness = newHappiness,
            exp = newExp,
            level = newLevel,
            intimacyScore = (intimacyScore + 10).coerceAtMost(MAX_INTIMACY_SCORE),
            temporaryMood = PetMood.HAPPY,
            feedbackMessage = "Meo~ $petName thích được bạn vuốt ve! ♡",
            lastInteractionTimestamp = System.currentTimeMillis()
        )
    }

    fun play(
        happinessGain: Float = 25f,
        energyCost: Float = 18f,
        hungerCost: Float = 12f,
        expGain: Int = 15
    ): PetState {
        if (isSleeping) {
            return copy(feedbackMessage = "$petName đang ngủ rồi, để bé nghỉ ngơi nhé!")
        }
        if (!canPlay) {
            return if (energy < 15f) {
                copy(feedbackMessage = "$petName buồn ngủ rồi, cho bé nghỉ ngơi nha!")
            } else {
                copy(feedbackMessage = "$petName đang đói meo, cho bé ăn trước khi chơi nhé!")
            }
        }
        val newHappiness = (happiness + happinessGain).coerceIn(0f, 100f)
        val newEnergy = (energy - energyCost).coerceIn(0f, 100f)
        val newHunger = (hunger - hungerCost).coerceIn(0f, 100f)
        val (newExp, newLevel) = gainExp(expGain)
        val levelUpMsg = if (newLevel > level) " 🎉 Chúc mừng $petName đã lên Cấp $newLevel!" else ""
        return copy(
            happiness = newHappiness,
            energy = newEnergy,
            hunger = newHunger,
            exp = newExp,
            level = newLevel,
            intimacyScore = (intimacyScore + 15).coerceAtMost(MAX_INTIMACY_SCORE),
            temporaryMood = PetMood.PLAYFUL,
            feedbackMessage = "Bắt được rồi! Chơi cùng $petName nữa đi!$levelUpMsg",
            lastInteractionTimestamp = System.currentTimeMillis()
        )
    }

    fun toggleSleep(): PetState {
        return if (isSleeping) {
            copy(
                isSleeping = false,
                temporaryMood = PetMood.HAPPY,
                feedbackMessage = "Oáp~ $petName đã thức dậy tràn đầy năng lượng!",
                lastInteractionTimestamp = System.currentTimeMillis()
            )
        } else {
            copy(
                isSleeping = true,
                temporaryMood = PetMood.SLEEPY,
                feedbackMessage = "Khò khò... $petName đang chìm vào giấc mơ ngọt ngào~",
                lastInteractionTimestamp = System.currentTimeMillis()
            )
        }
    }

    /**
     *chỉ số suy giảm hoặc hồi phục theo thời gian trôi qua
     */
    fun applyTimeElapsed(elapsedMinutes: Long): PetState {
        val safeMinutes = elapsedMinutes.coerceAtLeast(0)
        val hungerDecayRate = if (isSleeping) SLEEP_HUNGER_DECAY_PER_MINUTE else HUNGER_DECAY_PER_MINUTE
        val decayedHunger = (hunger - (safeMinutes * hungerDecayRate)).coerceIn(0f, 100f)
        val decayedHappiness = (happiness - (safeMinutes * HAPPINESS_DECAY_PER_MINUTE)).coerceIn(0f, 100f)
        val updatedEnergy = if (isSleeping) {
            (energy + (safeMinutes * SLEEP_ENERGY_RECOVER_PER_MINUTE)).coerceIn(0f, 100f)
        } else {
            energy
        }
        return copy(
            hunger = decayedHunger,
            happiness = decayedHappiness,
            energy = updatedEnergy
        )
    }

    /**
     *  kinh nghiệm và xử lý lên cấp lũy tiến.
     */
    internal fun gainExp(amount: Int): Pair<Int, Int> {
        var currentExp = exp + amount
        var currentLevel = level
        while (currentExp >= expToNextLevel) {
            currentExp -= expToNextLevel
            currentLevel++
        }
        return Pair(currentExp, currentLevel)
    }
}

/** Phụ kiện cho thú cưng */
enum class PetAccessory(val displayName: String, val category: String, val emoji: String) {
    RIBBON("Nơ hồng", "Nơ & Mũ", "🎀"),
    HAT("Mũ cói", "Nơ & Mũ", "👒"),
    COLLAR("Vòng cổ chuông", "Vòng cổ", "🔔"),
    SCARF("Khăn quàng ấm", "Khăn & Áo", "🧣"),
    BOW_TIE("Nơ cổ xanh", "Vòng cổ", "🦋")
}

/** Trạng thái hành động vật lý của mèo */
enum class PetActionState {
    IDLE,
    WALKING,
    EATING,
    SLEEPING,
    WAKING_UP,
    STRETCHING,
    PLAYING,
    PETTING
}

enum class BowlFood(
    val displayName: String,
    val emoji: String,
    val hungerBoost: Float,
    val happinessBoost: Float
) {
    EMPTY("Trống", "🍽️", 0f, 0f),
    DRY_FOOD("Hạt khô", "🟤", 25f, 5f),
    PATE("Pate", "🥩", 35f, 12f),
    FISH("Cá khô", "🐟", 30f, 20f)
}
