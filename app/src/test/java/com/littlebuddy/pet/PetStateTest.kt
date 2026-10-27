package com.littlebuddy.pet

import com.littlebuddy.pet.pet.BowlFood
import com.littlebuddy.pet.pet.PetMood
import com.littlebuddy.pet.pet.PetState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit Test kiểm tra logic nghiệp vụ và ràng buộc chỉ số của thú cưng Little Buddy
 */
class PetStateTest {

    @Test
    fun feed_increasesFullnessAndHappiness() {
        val initial = PetState(name = "Miu", hunger = 50f, happiness = 50f)
        val updated = initial.feed(BowlFood.FISH)

        assertEquals(80f, updated.hunger, 0.01f)
        assertEquals(70f, updated.happiness, 0.01f)
        assertTrue(updated.feedbackMessage.contains("Miu"))
    }

    @Test
    fun feed_clampsFullnessAt100() {
        val initial = PetState(hunger = 85f, happiness = 95f)
        val updated = initial.feed()

        assertEquals(100f, updated.hunger, 0.01f)
        assertEquals(100f, updated.happiness, 0.01f)
    }

    @Test
    fun feed_doesNothingWhenSleeping() {
        val initial = PetState(hunger = 50f, isSleeping = true)
        val updated = initial.feed()

        assertEquals(50f, updated.hunger, 0.01f)
        assertTrue(updated.isSleeping)
    }

    @Test
    fun pet_increasesHappinessClampedAt100_andUsesCustomName() {
        val initial = PetState(name = "Bông", happiness = 90f)
        val updated = initial.pet()

        assertEquals(100f, updated.happiness, 0.01f)
        assertEquals(PetMood.HAPPY, updated.temporaryMood)
        assertTrue(updated.feedbackMessage.contains("Bông"))
    }

    @Test
    fun play_consumesEnergyAndDecreasesFullness() {
        val initial = PetState(happiness = 50f, energy = 80f, hunger = 60f)
        val updated = initial.play()

        assertEquals(75f, updated.happiness, 0.01f)
        assertEquals(62f, updated.energy, 0.01f)
        assertEquals(48f, updated.hunger, 0.01f)
        assertEquals(PetMood.PLAYFUL, updated.temporaryMood)
    }

    @Test
    fun play_withMiniGameRewards_appliesConfiguredExpAndCosts() {
        val initial = PetState(happiness = 50f, energy = 80f, hunger = 60f, exp = 10)
        // Mô phỏng trò chơi Cuộn len: +25% Niềm vui, -18% Năng lượng, -12% Đói, +15 EXP
        val yarnGameResult = initial.play(happinessGain = 25f, energyCost = 18f, hungerCost = 12f, expGain = 15)

        assertEquals(75f, yarnGameResult.happiness, 0.01f)
        assertEquals(62f, yarnGameResult.energy, 0.01f)
        assertEquals(48f, yarnGameResult.hunger, 0.01f)
        assertEquals(25, yarnGameResult.exp)
        assertEquals(PetMood.PLAYFUL, yarnGameResult.temporaryMood)
    }

    @Test
    fun play_failsWhenEnergyIsTooLow() {
        val initial = PetState(happiness = 50f, energy = 10f)
        assertFalse(initial.canPlay)

        val updated = initial.play()
        assertEquals(10f, updated.energy, 0.01f)
    }

    @Test
    fun toggleSleep_switchesStateProperly() {
        val awake = PetState(isSleeping = false)
        val sleeping = awake.toggleSleep()
        assertTrue(sleeping.isSleeping)
        assertEquals(PetMood.SLEEPY, sleeping.currentMood)

        val wokeUp = sleeping.toggleSleep()
        assertFalse(wokeUp.isSleeping)
    }



    @Test
    fun currentMood_reflectsHungryStateWhenFullnessIsLow() {
        val hungryPet = PetState(hunger = 20f, happiness = 50f, energy = 80f)
        assertEquals(PetMood.HUNGRY, hungryPet.currentMood)
    }

    @Test
    fun expAndLevel_levelsUpWhenReaching100Exp() {
        val initial = PetState(level = 1, exp = 85)
        val afterPlay = initial.play(expGain = 25) // +25 EXP -> 110 EXP -> Level 2, EXP 10

        assertEquals(2, afterPlay.level)
        assertEquals(10, afterPlay.exp)
    }

    @Test
    fun sanitizeName_trimsAndRejectsBlank() {
        assertNull(PetState.sanitizeName("    "))
        assertEquals("Mèo Cam", PetState.sanitizeName("  Mèo   Cam  "))
    }

    @Test
    fun intimacyScore_isCappedAtMaxIntimacyScore() {
        val nearCapPet = PetState(intimacyScore = 195)
        val afterPet = nearCapPet.pet() // +10 intimacy
        assertEquals(PetState.MAX_INTIMACY_SCORE, afterPet.intimacyScore)
        assertEquals(200, afterPet.intimacyScore)
    }

    @Test
    fun pet_whenSleeping_capsIntimacyAtMaxAndUpdatesTimestamp() {
        val oldTimestamp = 1000L
        val sleepingPet = PetState(
            intimacyScore = 199,
            isSleeping = true,
            lastInteractionTimestamp = oldTimestamp
        )
        val afterPet = sleepingPet.pet()

        assertEquals(PetState.MAX_INTIMACY_SCORE, afterPet.intimacyScore)
        assertEquals(200, afterPet.intimacyScore)
        assertTrue(afterPet.lastInteractionTimestamp >= oldTimestamp)
        assertTrue(afterPet.isSleeping)
    }

    @Test
    fun applyTimeElapsed_decaysHungerAndHappiness_andRecoversSleepingEnergy() {
        val sleepingPet = PetState(
            hunger = 80f,
            happiness = 80f,
            energy = 40f,
            isSleeping = true
        )
        // Khi ngủ: hunger dùng SLEEP_HUNGER_DECAY_PER_MINUTE = 0.05f
        // 10 phút: hunger -0.5f, happiness -1.5f, energy +40f
        val updated = sleepingPet.applyTimeElapsed(10L)

        assertEquals(79.5f, updated.hunger, 0.01f)
        assertEquals(78.5f, updated.happiness, 0.01f)
        assertEquals(80f, updated.energy, 0.01f)
    }

    @Test
    fun applyTimeElapsed_usesFullDecayRateWhenAwake() {
        val awakePet = PetState(hunger = 80f, happiness = 80f, energy = 40f, isSleeping = false)
        // Khi thức: hunger dùng HUNGER_DECAY_PER_MINUTE = 0.2f
        // 10 phút: hunger -2f, happiness -1.5f
        val updated = awakePet.applyTimeElapsed(10L)

        assertEquals(78f, updated.hunger, 0.01f)
        assertEquals(78.5f, updated.happiness, 0.01f)
    }

    @Test
    fun applyTimeElapsed_doesNotRecoverEnergyWhenAwake() {
        val awakePet = PetState(
            energy = 40f,
            isSleeping = false
        )
        val updated = awakePet.applyTimeElapsed(10L)

        assertEquals(40f, updated.energy, 0.01f)
    }

    @Test
    fun gainExp_handlesMultipleLevelUpsInOneBatch() {
        // EXP hiện tại 90, nhận 250 EXP → 340 total → 3 level-ups (100, 100, 100), còn 40 EXP
        val pet = PetState(level = 1, exp = 90)
        val (newExp, newLevel) = pet.gainExp(250)
        assertEquals(40, newExp)
        assertEquals(4, newLevel)
    }

    @Test
    fun gainExp_computesCorrectLevels() {
        val pet = PetState(level = 1, exp = 90)
        val (newExp, newLevel) = pet.gainExp(20) // 110 exp -> 10 exp, level 2
        assertEquals(10, newExp)
        assertEquals(2, newLevel)
    }
}
