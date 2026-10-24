package com.littlebuddy.pet.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.littlebuddy.pet.data.PetRepository
import com.littlebuddy.pet.pet.BowlFood
import com.littlebuddy.pet.pet.PetAccessory
import com.littlebuddy.pet.pet.PetActionState
import com.littlebuddy.pet.pet.PetMood
import com.littlebuddy.pet.pet.PetState
import com.littlebuddy.pet.pet.PetType
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel quản lý logic tương tác và trạng thái của Little Buddy.
 *
 * State Machine hành động vật lý (actionState):
 * - IDLE: Đứng yên tại vị trí trung tâm (catPositionX = 0.5f), thở phập phồng nhẹ.
 * - WALKING: Mèo đang di chuyển với bước chân so le, hướng thân xoay theo facingDirection.
 * - EATING: Đang ăn tại bát, đầu gật gù nhai nhóp nhép.
 * - SLEEPING: Nằm cuộn tròn trên đệm, thở sâu chậm rãi, Zzz bay lượn.
 * - WAKING_UP / STRETCHING: Tỉnh giấc, duỗi 2 chân trước, cong lưng vươn vai rồi mới đi ra.
 * - PLAYING: Đang trong mini-game.
 * - PETTING: Đang được vuốt ve (rung rinh purr vibration, híp mắt hạnh phúc, tim bay).
 */
class PetViewModel(
    private val repository: PetRepository
) : ViewModel() {

    private val _petState = MutableStateFlow(repository.getInitialPetState())
    val petState: StateFlow<PetState> = _petState.asStateFlow()

    /** Trạng thái hành động vật lý hiện tại của mèo */
    private val _actionState = MutableStateFlow(PetActionState.IDLE)
    val actionState: StateFlow<PetActionState> = _actionState.asStateFlow()

    /** Vị trí ngang của mèo (0.0 = cạnh trái, 1.0 = cạnh phải) — dùng để animation đi bộ */
    private val _catPositionX = MutableStateFlow(0.5f)
    val catPositionX: StateFlow<Float> = _catPositionX.asStateFlow()

    /** Hướng nhìn của mèo: 1f = nhìn sang phải, -1f = nhìn sang trái (xoay theo chiều đi) */
    private val _facingDirection = MutableStateFlow(1f)
    val facingDirection: StateFlow<Float> = _facingDirection.asStateFlow()

    /** Nội dung bát thức ăn trong phòng */
    private val _bowlFood = MutableStateFlow(BowlFood.EMPTY)
    val bowlFood: StateFlow<BowlFood> = _bowlFood.asStateFlow()

    /** Điều khiển hiển thị tủ đồ phụ kiện */
    private val _showWardrobe = MutableStateFlow(false)
    val showWardrobe: StateFlow<Boolean> = _showWardrobe.asStateFlow()

    /** Trạng thái tủ đồ đang thử (Live Preview) trong màn hình Wardrobe */
    private val _previewWardrobe = MutableStateFlow(repository.getInitialPetState().wardrobe)
    val previewWardrobe: StateFlow<com.littlebuddy.pet.wardrobe.EquippedWardrobe> = _previewWardrobe.asStateFlow()

    /** Điều khiển hiển thị bảng chọn thức ăn */
    private val _showFoodPicker = MutableStateFlow(false)
    val showFoodPicker: StateFlow<Boolean> = _showFoodPicker.asStateFlow()

    /** Điều khiển hiển thị bảng chọn trò chơi */
    private val _showGamePicker = MutableStateFlow(false)
    val showGamePicker: StateFlow<Boolean> = _showGamePicker.asStateFlow()

    private var sleepJob: Job? = null
    private var temporaryMoodJob: Job? = null
    private var walkJob: Job? = null
    private var eatJob: Job? = null
    private var wakeJob: Job? = null
    private var idleLifeJob: Job? = null
    private var feedbackDismissJob: Job? = null
    private var scratchJob: Job? = null
    private var pettingJob: Job? = null
    private var statsJob: Job? = null

    /** Thời điểm vuốt mèo đang ngủ gần nhất */
    private var lastSleepPetAt: Long = 0L

    /** Thời điểm tương tác gần nhất để chống spam miễn phí */
    private var lastDrinkTimestamp: Long = 0L
    private var lastYarnTimestamp: Long = 0L

    /**
     * Thức ăn đang trong phiên ăn hiện tại. Được khoá lại khi bắt đầu ăn để tránh
     * race condition với [selectFood] ghi đè [_bowlFood] trong lúc animation đang chạy.
     */
    private var activeMeal: BowlFood? = null

    // Vị trí bát thức ăn (bên phải phòng)
    val bowlPositionX = 0.72f
    // Vị trí đệm ngủ (sát góc trái phòng trên giường gỗ)
    val bedPositionX = 0.02f

    init {
        // Tự động ẩn phản hồi ở một chỗ duy nhất sau 3.5s
        viewModelScope.launch {
            _petState.map { it.feedbackMessage }
                .distinctUntilChanged()
                .collectLatest { msg ->
                    if (msg.isNotBlank()) {
                        delay(3500L)
                        _petState.update { if (it.feedbackMessage == msg) it.copy(feedbackMessage = "") else it }
                    }
                }
        }

        // Ticker định kỳ 60s cập nhật chỉ số suy giảm & hồi phục thống nhất
        startStatsTicker()

        if (_petState.value.isSleeping) {
            _actionState.value = PetActionState.SLEEPING
            _catPositionX.value = bedPositionX
            _facingDirection.value = -1f
            startSleepCycle()
        } else {
            startIdleLifeCycle()
        }
    }

    private fun notifyBusy() =
        showFeedback("${_petState.value.petName} đang bận, đợi bé một chút nhé~", 2000L)

    private fun startStatsTicker() {
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            while (true) {
                delay(60_000L)
                _petState.update { it.applyTimeElapsed(1L) }
                repository.persistPetState(_petState.value)
            }
        }
    }

    private suspend fun moveCatTo(target: Float, speed: Float = 0.20f) {
        val start = _catPositionX.value
        val duration = (kotlin.math.abs(target - start) / speed).coerceAtLeast(0.01f)
        var elapsed = 0f
        var last = System.nanoTime()
        while (elapsed < duration) {
            delay(16L)
            val now = System.nanoTime()
            elapsed += (now - last) / 1e9f
            last = now
            val f = (elapsed / duration).coerceIn(0f, 1f)
            val eased = f * f * (3f - 2f * f)
            _catPositionX.value = start + (target - start) * eased
        }
        _catPositionX.value = target
    }

    /** Xóa thông điệp phản hồi */
    fun clearFeedback() {
        _petState.update { it.copy(feedbackMessage = "") }
    }

    /** Hiển thị phản hồi và tự ẩn sau autoDismissMs */
    fun showFeedback(msg: String, autoDismissMs: Long = 3000L) {
        feedbackDismissJob?.cancel()
        _petState.update { it.copy(feedbackMessage = msg) }
        if (msg.isNotBlank() && autoDismissMs > 0L) {
            feedbackDismissJob = viewModelScope.launch {
                delay(autoDismissMs)
                _petState.update { if (it.feedbackMessage == msg) it.copy(feedbackMessage = "") else it }
            }
        }
    }

    /**
     * Vòng lặp cử động tự nhiên khi mèo rảnh (IDLE):
     * Thỉnh thoảng nhìn quanh, đi dạo vài bước nhẹ trên thảm hoặc kêu meo meo.
     */
    private fun startIdleLifeCycle() {
        idleLifeJob?.cancel()
        idleLifeJob = viewModelScope.launch {
            while (true) {
                delay((9000L..15000L).random())
                if (_actionState.value == PetActionState.IDLE &&
                    _bowlFood.value != BowlFood.EMPTY && _petState.value.hunger < 80f) {
                    startWalkingToBowl()
                    continue
                }
                if (_actionState.value == PetActionState.IDLE && !_petState.value.isSleeping) {
                    when ((1..3).random()) {
                        1 -> {
                            // Nhìn quanh: quay sang hướng khác một lúc rồi quay lại
                            _facingDirection.value = -_facingDirection.value
                            delay(2200L)
                            _facingDirection.value = -_facingDirection.value
                        }
                        2 -> {
                            // Đi dạo vài bước nhẹ trên thảm
                            val cur = _catPositionX.value
                            val target = if (cur < 0.48f) 0.54f else 0.44f
                            val start = cur
                            _facingDirection.value = if (target > start) 1f else -1f
                            _actionState.value = PetActionState.WALKING
                            moveCatTo(target, speed = 0.15f)
                            _actionState.value = PetActionState.IDLE
                        }
                        3 -> {
                            val cuteMoods = listOf("Meo~ ♡", "Ngoao... 🐾", "*khịt khịt mũi* 🌸", "Purrr... ✨")
                            showFeedback(cuteMoods.random(), 2500L)
                        }
                    }
                }
            }
        }
    }

    /**
     * Kiểm tra mèo có đang rảnh để nhận tương tác phụ (uống nước, vờn len, cào móng, vuốt ve).
     * Chỉ cho phép khi đang IDLE hoặc PETTING và không ngủ.
     */
    private fun isFreeForSideAction(): Boolean {
        if (_petState.value.isSleeping || _actionState.value == PetActionState.SLEEPING) return false
        return _actionState.value == PetActionState.IDLE || _actionState.value == PetActionState.PETTING
    }

    /** Uống nước từ bát nước bên cạnh bát thức ăn */
    fun drinkWater() {
        if (!isFreeForSideAction()) return
        val now = System.currentTimeMillis()
        if (now - lastDrinkTimestamp < 8000L) {
            showFeedback("${_petState.value.petName} vừa uống nước xong, bụng còn no nước lắm! 💧", 2000L)
            return
        }
        lastDrinkTimestamp = now
        _petState.update { state ->
            val newEnergy = (state.energy + 10f).coerceIn(0f, 100f)
            val newHappy = (state.happiness + 6f).coerceIn(0f, 100f)
            state.copy(
                energy = newEnergy,
                happiness = newHappy,
                temporaryMood = PetMood.HAPPY
            )
        }
        showFeedback("${_petState.value.petName} đã uống ngụm nước mát lành, sảng khoái quá! 💧✨", 3000L)
        repository.persistPetState(_petState.value)
    }

    /** Tương tác cuộn len trên sàn */
    fun playWithYarn() {
        if (!isFreeForSideAction()) return
        val now = System.currentTimeMillis()
        if (now - lastYarnTimestamp < 8000L) {
            showFeedback("${_petState.value.petName} đang nghỉ thở một chút trước khi vờn len tiếp nhé! 🧶", 2000L)
            return
        }
        lastYarnTimestamp = now
        _petState.update { state ->
            val newHappy = (state.happiness + 10f).coerceIn(0f, 100f)
            val newEnergy = (state.energy - 5f).coerceIn(0f, 100f)
            state.copy(
                happiness = newHappy,
                energy = newEnergy,
                temporaryMood = PetMood.PLAYFUL
            )
        }
        showFeedback("${_petState.value.petName} vồ vồ cuộn len lăn tròn trên thảm cực vui! 🧶🐾", 3000L)
        repository.persistPetState(_petState.value)
    }

    /** Cào móng ở cột cào móng */
    fun scratchPost() {
        if (!isFreeForSideAction()) return
        scratchJob?.cancel()
        _actionState.value = PetActionState.STRETCHING
        showFeedback("${_petState.value.petName} đang mài móng sột soạt sảng khoái... Meo~ 🪵✨", 3000L)
        scratchJob = viewModelScope.launch {
            _petState.update { it.copy(happiness = (it.happiness + 8f).coerceIn(0f, 100f), temporaryMood = PetMood.HAPPY) }
            repository.persistPetState(_petState.value)
            delay(1600L)
            if (_actionState.value == PetActionState.STRETCHING) {
                _actionState.value = PetActionState.IDLE
            }
        }
    }

    // ── HÀNH ĐỘNG TƯƠNG TÁC ──────────────────────────────────────────────────

    /**
     * Mở bảng chọn thức ăn.
     * Chỉ cho phép nếu mèo không đang ngủ hoặc đang bận hành động khác.
     */
    fun openFoodPicker() {
        if (_petState.value.isSleeping || _actionState.value == PetActionState.SLEEPING) {
            _petState.update { it.copy(feedbackMessage = "${it.petName} đang ngủ say, đừng đánh thức bé nha!") }
            return
        }
        if (_actionState.value != PetActionState.IDLE && _actionState.value != PetActionState.PETTING) {
            _petState.update { it.copy(feedbackMessage = "${it.petName} đang bận, đợi bé một chút nhé~") }
            return
        }
        _showFoodPicker.value = true
    }

    fun closeFoodPicker() { _showFoodPicker.value = false }

    /**
     * Chọn món ăn → đặt vào bát → nếu đủ điều kiện thì mèo quay đầu và đi tới ăn.
     * Không cho phép chọn thức ăn khi đang bận hành động khác hoặc đang ngủ để tránh race condition.
     */
    fun selectFood(food: BowlFood) {
        val isBusy = _actionState.value == PetActionState.EATING ||
                _actionState.value == PetActionState.WALKING ||
                _actionState.value == PetActionState.SLEEPING ||
                _actionState.value == PetActionState.WAKING_UP ||
                _actionState.value == PetActionState.STRETCHING ||
                _actionState.value == PetActionState.PLAYING ||
                _petState.value.isSleeping

        if (isBusy) {
            _showFoodPicker.value = false
            notifyBusy()
            return
        }
        _showFoodPicker.value = false
        _bowlFood.value = food

        val state = _petState.value
        val canAutoEat = (_actionState.value == PetActionState.IDLE || _actionState.value == PetActionState.PETTING) &&
                state.hunger < 80f && !state.isSleeping

        if (canAutoEat) {
            startWalkingToBowl()
        } else if (state.hunger >= 80f) {
            _petState.update { it.copy(feedbackMessage = "${it.petName} no rồi, để dành thức ăn đây cho bé ăn sau nhé~") }
        }
    }

    /**
     * Mèo xoay người nhìn sang phải và đi bộ tới bát thức ăn.
     */
    private fun startWalkingToBowl() {
        walkJob?.cancel()
        eatJob?.cancel()
        _facingDirection.value = 1f // Quay sang phải hướng về bát
        _actionState.value = PetActionState.WALKING
        _petState.update { it.copy(feedbackMessage = "${it.petName} đang đi tới bát thức ăn...🐾") }

        walkJob = viewModelScope.launch {
            moveCatTo(bowlPositionX, speed = 0.20f)
            startEatingAtBowl()
        }
    }

    /**
     * Mèo đang ăn tại bát: chuyển sang EATING, cập nhật stats, sau đó quay đầu đi về giữa phòng.
     * [activeMeal] được khoá ngay khi bắt đầu ăn để tránh [selectFood] thay đổi món giữa chừng.
     */
    private fun startEatingAtBowl() {
        val food = _bowlFood.value
        if (food == BowlFood.EMPTY) {
            _actionState.value = PetActionState.IDLE
            return
        }

        // Khoá phiên ăn: mọi thay đổi _bowlFood sau đây sẽ không ảnh hưởng tới tính toán chỉ số
        activeMeal = food
        _actionState.value = PetActionState.EATING
        _petState.update { state ->
            state.copy(
                temporaryMood = PetMood.EATING,
                feedbackMessage = "Chóp chép… ${state.petName} đang thưởng thức ${food.displayName} ngon lắm! ♡"
            )
        }

        eatJob = viewModelScope.launch {
            delay(2400L)

            // Dùng activeMeal (đã khoá) thay vì đọc lại _bowlFood.value có thể đã bị ghi đè
            val lockedFood = activeMeal ?: BowlFood.EMPTY
            activeMeal = null
            _petState.update { state ->
                val newHunger = (state.hunger + lockedFood.hungerBoost).coerceIn(0f, 100f)
                val newHappiness = (state.happiness + lockedFood.happinessBoost).coerceIn(0f, 100f)
                val (newExp, newLevel) = gainExp(state, 15)
                state.copy(
                    hunger = newHunger,
                    happiness = newHappiness,
                    exp = newExp,
                    level = newLevel,
                    intimacyScore = (state.intimacyScore + 5).coerceAtMost(PetState.MAX_INTIMACY_SCORE),
                    temporaryMood = null,
                    feedbackMessage = "${state.petName} ăn no rồi~ Cảm ơn bạn nhiều lắm! 🐾",
                    lastInteractionTimestamp = System.currentTimeMillis()
                )
            }
            _bowlFood.value = BowlFood.EMPTY
            repository.persistPetState(_petState.value)

            // Quay đầu sang trái và đi về giữa phòng
            _facingDirection.value = -1f
            walkBackToCenter()
        }
    }

    /** Mèo đi bộ về vị trí trung tâm sau khi ăn/chơi */
    private suspend fun walkBackToCenter() {
        _actionState.value = PetActionState.WALKING
        moveCatTo(0.5f, speed = 0.20f)
        _facingDirection.value = 1f // Quay lại nhìn thẳng / sang phải
        _actionState.value = PetActionState.IDLE
    }

    /**
     * Vuốt ve thú cưng (Pet) - phản ứng rung rinh purr và híp mắt cười.
     * Khi chạm liên tục trong cùng một chu kỳ, chỉ gia hạn animation chứ không cộng dồn chỉ số (chống spam cày EXP).
     */
    fun pet() {
        if (_petState.value.isSleeping || _actionState.value == PetActionState.SLEEPING) {
            val now = System.currentTimeMillis()
            if (now - lastSleepPetAt < 5000L) return
            lastSleepPetAt = now
            _petState.update { it.pet() }
            repository.persistPetState(_petState.value)
            return
        }
        if (!isFreeForSideAction()) {
            return
        }

        if (_actionState.value == PetActionState.PETTING) {
            // Đang vuốt ve: gia hạn thời gian hiệu ứng rung rinh nhưng KHÔNG cộng thêm EXP/chỉ số
            pettingJob?.cancel()
            pettingJob = viewModelScope.launch {
                delay(2200L)
                if (_actionState.value == PetActionState.PETTING) {
                    _actionState.value = PetActionState.IDLE
                }
            }
            return
        }

        // Bắt đầu chu kỳ vuốt ve mới: cộng chỉ số 1 lần duy nhất
        _actionState.value = PetActionState.PETTING
        _petState.update { it.pet() }
        persistAndScheduleMoodReset()

        pettingJob?.cancel()
        pettingJob = viewModelScope.launch {
            delay(2200L)
            if (_actionState.value == PetActionState.PETTING) {
                _actionState.value = PetActionState.IDLE
            }
        }
    }

    /** Mở bảng chọn trò chơi */
    fun openGamePicker() {
        if (_petState.value.isSleeping || _actionState.value == PetActionState.SLEEPING) {
            _petState.update { it.copy(feedbackMessage = "${it.petName} đang ngủ rồi, để bé nghỉ ngơi nhé!") }
            return
        }
        if (_actionState.value != PetActionState.IDLE && _actionState.value != PetActionState.PETTING) {
            _petState.update { it.copy(feedbackMessage = "${it.petName} đang bận, đợi bé một chút nhé~") }
            return
        }
        if (!_petState.value.canPlay) {
            val state = _petState.value
            if (state.energy < 15f) {
                _petState.update { it.copy(feedbackMessage = "${it.petName} buồn ngủ rồi, cho bé nghỉ ngơi nha!") }
            } else {
                _petState.update { it.copy(feedbackMessage = "${it.petName} đang đói meo, cho bé ăn trước khi chơi nhé!") }
            }
            return
        }
        _showGamePicker.value = true
    }

    fun closeGamePicker() { _showGamePicker.value = false }

    /** Kết thúc mini-game */
    fun finishPlaying(happinessGain: Float = 25f, energyCost: Float = 20f, hungerCost: Float = 15f) {
        // Chỉ chuyển về IDLE nếu đang thực sự ở PLAYING, tránh ghi đè trạng thái khác
        if (_actionState.value == PetActionState.PLAYING) {
            _actionState.value = PetActionState.IDLE
        }
        _petState.update { state ->
            val newHappiness = (state.happiness + happinessGain).coerceIn(0f, 100f)
            val newEnergy = (state.energy - energyCost).coerceIn(0f, 100f)
            val newHunger = (state.hunger - hungerCost).coerceIn(0f, 100f)
            val (newExp, newLevel) = gainExp(state, 25)
            state.copy(
                happiness = newHappiness,
                energy = newEnergy,
                hunger = newHunger,
                exp = newExp,
                level = newLevel,
                intimacyScore = (state.intimacyScore + 15).coerceAtMost(PetState.MAX_INTIMACY_SCORE),
                temporaryMood = PetMood.PLAYFUL,
                feedbackMessage = "Bắt được rồi! Chơi cùng ${state.petName} nữa đi bạn ơi!",
                lastInteractionTimestamp = System.currentTimeMillis()
            )
        }
        persistAndScheduleMoodReset()
    }

    /**
     * Bật/Tắt chế độ ngủ với chuỗi chuyển động hoàn chỉnh:
     * - Khi ngủ: Mèo quay trái, đi bộ tới đệm (vẫn thức khi đi), nằm cuộn tròn ngủ (mới đặt isSleeping = true).
     * - Khi thức: Mèo mở mắt (isSleeping = false), vươn vai (stretching) duỗi 2 chân trước, rồi mới đi ra giữa phòng.
     */
    fun toggleSleep() {
        // Chặn spam thao tác khi đang trong chuỗi chuyển tiếp đi lại hoặc vươn vai
        if (_actionState.value == PetActionState.WALKING ||
            _actionState.value == PetActionState.WAKING_UP ||
            _actionState.value == PetActionState.STRETCHING
        ) {
            notifyBusy()
            return
        }

        val isCurrentlySleeping = _petState.value.isSleeping || _actionState.value == PetActionState.SLEEPING

        if (!isCurrentlySleeping) {
            // Chuỗi hành động: ĐI TỚI ĐỆM -> NẰM NGỦ
            idleLifeJob?.cancel()
            walkJob?.cancel()
            eatJob?.cancel()
            wakeJob?.cancel()
            activeMeal = null
            _bowlFood.value = BowlFood.EMPTY

            // Tách trạng thái: Chỉ đi bộ tới đệm, KHÔNG đặt isSleeping = true khi đang đi
            _petState.update { it.copy(feedbackMessage = "${it.petName} đang đi tới đệm ngủ...💤") }
            _facingDirection.value = -1f // Quay sang trái hướng về đệm
            _actionState.value = PetActionState.WALKING

            walkJob = viewModelScope.launch {
                moveCatTo(bedPositionX, speed = 0.20f)
                // Chỉ khi đến đệm mới chuyển sang SLEEPING và cập nhật isSleeping = true
                _actionState.value = PetActionState.SLEEPING
                _petState.update { it.copy(isSleeping = true, feedbackMessage = "${it.petName} đã nằm cuộn tròn ngủ ngon lành... Khò khò~") }
                startSleepCycle()
                repository.persistPetState(_petState.value)
            }
        } else {
            // Chuỗi hành động: TỈNH DẬY -> VƯƠN VAI STRETCHING -> ĐI VỀ GIỮA PHÒNG
            sleepJob?.cancel()
            sleepJob = null
            walkJob?.cancel()

            wakeJob = viewModelScope.launch {
                // Đặt isSleeping = false ngay khi bắt đầu tỉnh giấc
                _petState.update { it.copy(isSleeping = false, feedbackMessage = "${it.petName} khẽ chớp mắt tỉnh giấc...") }
                _actionState.value = PetActionState.WAKING_UP
                delay(600L)

                // Vươn vai duỗi chân trước
                _actionState.value = PetActionState.STRETCHING
                _petState.update { it.copy(feedbackMessage = "${it.petName} đang vươn vai duỗi người sảng khoái... Meo~") }
                delay(1600L)

                // Quay người sang phải và đi ra giữa phòng
                _facingDirection.value = 1f
                _actionState.value = PetActionState.WALKING
                moveCatTo(0.5f, speed = 0.20f)
                _actionState.value = PetActionState.IDLE
                _petState.update { it.copy(feedbackMessage = "${it.petName} đã thức dậy đầy năng lượng sẵn sàng chơi rồi! 🐾") }
                repository.persistPetState(_petState.value)
                startIdleLifeCycle()
            }
        }
    }

    // ── TỦ ĐỒ PHỤ KIỆN & CÁ NHÂN HÓA ──────────────────────────────────────────

    fun openWardrobe() {
        _previewWardrobe.value = _petState.value.wardrobe
        _showWardrobe.value = true
    }

    fun closeWardrobe() {
        _previewWardrobe.value = _petState.value.wardrobe
        _showWardrobe.value = false
    }

    /**
     * Chọn xem trước một vật phẩm trong tủ đồ.
     * Kiểm tra cấp độ mở khóa, nếu chưa đủ cấp sẽ thông báo phản hồi.
     */
    fun selectPreviewItem(item: com.littlebuddy.pet.wardrobe.WardrobeItem) {
        val currentLevel = _petState.value.level
        if (currentLevel < item.levelRequired) {
            showFeedback("Cần Cấp ${item.levelRequired} để mở khóa ${item.name}! 🔒", 2500L)
            return
        }

        _previewWardrobe.update { current ->
            when (item.category) {
                com.littlebuddy.pet.wardrobe.WardrobeCategory.FUR_SKIN -> {
                    current.copy(furSkinId = item.id)
                }
                com.littlebuddy.pet.wardrobe.WardrobeCategory.HEADWEAR -> {
                    if (current.headwearId == item.id) current.copy(headwearId = null)
                    else current.copy(headwearId = item.id)
                }
                com.littlebuddy.pet.wardrobe.WardrobeCategory.NECKWEAR -> {
                    if (current.neckwearId == item.id) current.copy(neckwearId = null)
                    else current.copy(neckwearId = item.id)
                }
                com.littlebuddy.pet.wardrobe.WardrobeCategory.BODYWEAR -> {
                    if (current.bodywearId == item.id) current.copy(bodywearId = null)
                    else current.copy(bodywearId = item.id)
                }
            }
        }
    }

    /** Tháo toàn bộ phụ kiện trong một danh mục đang xem trước */
    fun unequipCategoryInPreview(category: com.littlebuddy.pet.wardrobe.WardrobeCategory) {
        _previewWardrobe.update { current ->
            when (category) {
                com.littlebuddy.pet.wardrobe.WardrobeCategory.FUR_SKIN -> {
                    current.copy(furSkinId = com.littlebuddy.pet.wardrobe.WardrobeRegistry.DEFAULT_FUR_ID)
                }
                com.littlebuddy.pet.wardrobe.WardrobeCategory.HEADWEAR -> current.copy(headwearId = null)
                com.littlebuddy.pet.wardrobe.WardrobeCategory.NECKWEAR -> current.copy(neckwearId = null)
                com.littlebuddy.pet.wardrobe.WardrobeCategory.BODYWEAR -> current.copy(bodywearId = null)
            }
        }
    }

    /** Hoàn tác xem trước, quay lại bộ đồ đang mặc thực tế */
    fun resetPreviewToCurrent() {
        _previewWardrobe.value = _petState.value.wardrobe
    }

    /** Khôi phục tủ đồ xem trước về mặc định (Mèo Xiêm nguyên bản, không phụ kiện) */
    fun resetPreviewToDefault() {
        _previewWardrobe.value = com.littlebuddy.pet.wardrobe.EquippedWardrobe()
    }

    /** Xác nhận lưu và áp dụng trang phục đang xem trước cho nhân vật mèo */
    fun confirmWardrobe() {
        val newWardrobe = _previewWardrobe.value
        _petState.update { state ->
            state.copy(
                wardrobe = newWardrobe,
                feedbackMessage = "Đã cập nhật tủ đồ cho ${state.petName} thật phong cách! ✨🐾"
            )
        }
        repository.persistPetState(_petState.value)
        _showWardrobe.value = false
    }

    fun equipAccessory(accessory: PetAccessory) {
        _petState.update { state ->
            val newAccessory = if (state.accessory == accessory) null else accessory
            val message = if (newAccessory != null) {
                "Ôi xinh quá! ${state.petName} trông thật đáng yêu với ${newAccessory.displayName}!"
            } else {
                "${state.petName} đã tháo ${accessory.displayName} ra rồi."
            }
            state.copy(accessory = newAccessory, feedbackMessage = message)
        }
        repository.persistPetState(_petState.value)
    }

    // ── QUẢN LÝ HỒ SƠ ────────────────────────────────────────────────────────

    fun renamePet(newName: String) {
        val validName = PetState.sanitizeName(newName) ?: return
        _petState.update {
            it.copy(
                name = validName,
                feedbackMessage = "Meo~ Từ giờ hãy gọi bé là $validName nha! ♡"
            )
        }
        repository.persistPetState(_petState.value)
    }

    fun switchPetType(type: PetType) {
        _petState.update {
            it.copy(
                petType = type,
                feedbackMessage = "Bạn đã chọn ${type.displayName}!"
            )
        }
        repository.persistPetState(_petState.value)
    }

    fun resetPet() {
        sleepJob?.cancel()
        walkJob?.cancel()
        eatJob?.cancel()
        wakeJob?.cancel()
        idleLifeJob?.cancel()
        temporaryMoodJob?.cancel()
        feedbackDismissJob?.cancel()
        scratchJob?.cancel()
        pettingJob?.cancel()
        activeMeal = null
        _actionState.value = PetActionState.IDLE
        _catPositionX.value = 0.5f
        _facingDirection.value = 1f
        _bowlFood.value = BowlFood.EMPTY
        val defaultState = repository.resetPetState()
        _petState.value = defaultState
        startIdleLifeCycle()
        startStatsTicker()
    }

    // ── NỘI BỘ ───────────────────────────────────────────────────────────────

    private fun startSleepCycle() {
        sleepJob?.cancel()
        sleepJob = null
    }

    private fun persistAndScheduleMoodReset() {
        repository.persistPetState(_petState.value)
        temporaryMoodJob?.cancel()
        temporaryMoodJob = viewModelScope.launch {
            delay(2500L)
            _petState.update { it.copy(temporaryMood = null) }
        }
    }

    /**
     * Dùng lại `gainExp` từ `PetState` (internal fun) để không lặp logic — nguyên tắc DRY.
     * Hàm này đơn giản là delegate để ViewModel gọi tiện hơn.
     */
    private fun gainExp(state: PetState, amount: Int): Pair<Int, Int> = state.gainExp(amount)

    override fun onCleared() {
        super.onCleared()
        statsJob?.cancel()
        sleepJob?.cancel()
        temporaryMoodJob?.cancel()
        walkJob?.cancel()
        eatJob?.cancel()
        wakeJob?.cancel()
        idleLifeJob?.cancel()
        feedbackDismissJob?.cancel()
        scratchJob?.cancel()
        pettingJob?.cancel()
    }
}
