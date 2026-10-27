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
 * ViewModel quản lý logic tương tác, hoạt ảnh và trạng thái của thú cưng.
 */
class PetViewModel(
    private val repository: PetRepository
) : ViewModel() {

    private val _petState = MutableStateFlow(repository.getInitialPetState())
    val petState: StateFlow<PetState> = _petState.asStateFlow()

    
    private val _actionState = MutableStateFlow(PetActionState.IDLE)
    val actionState: StateFlow<PetActionState> = _actionState.asStateFlow()

    // Vị trí ngang (0.0: cạnh trái, 1.0: cạnh phải)
    private val _catPositionX = MutableStateFlow(0.5f)
    val catPositionX: StateFlow<Float> = _catPositionX.asStateFlow()

    // 1f: nhìn phải, -1f: nhìn trái
    private val _facingDirection = MutableStateFlow(1f)
    val facingDirection: StateFlow<Float> = _facingDirection.asStateFlow()

    
    private val _bowlFood = MutableStateFlow(BowlFood.EMPTY)
    val bowlFood: StateFlow<BowlFood> = _bowlFood.asStateFlow()

    
    private val _showWardrobe = MutableStateFlow(false)
    val showWardrobe: StateFlow<Boolean> = _showWardrobe.asStateFlow()

    // Trạng thái live preview trong tủ đồ
    private val _previewWardrobe = MutableStateFlow(repository.getInitialPetState().wardrobe)
    val previewWardrobe: StateFlow<com.littlebuddy.pet.wardrobe.EquippedWardrobe> = _previewWardrobe.asStateFlow()

    
    private val _showFoodPicker = MutableStateFlow(false)
    val showFoodPicker: StateFlow<Boolean> = _showFoodPicker.asStateFlow()

    
    private val _showGamePicker = MutableStateFlow(false)
    val showGamePicker: StateFlow<Boolean> = _showGamePicker.asStateFlow()

    private var sleepJob: Job? = null
    private var temporaryMoodJob: Job? = null
    private var walkJob: Job? = null
    private var eatJob: Job? = null
    private var wakeJob: Job? = null
    private var idleLifeJob: Job? = null
    private var scratchJob: Job? = null
    private var pettingJob: Job? = null
    private var statsJob: Job? = null

    /**
     * Thời điểm lần cuối app đang ở foreground (được ghi khi Activity vào onPause).
     * Dùng để tính thời gian trôi qua khi app quay lại sau khi bị background.
     */
    private var lastForegroundTimestamp: Long = System.currentTimeMillis()

    
    private var lastSleepPetAt: Long = 0L

    // Chống spam tương tác
    private var lastDrinkTimestamp: Long = 0L
    private var lastYarnTimestamp: Long = 0L

    /**
     * Thức ăn đang trong phiên ăn hiện tại. Được khoá lại khi bắt đầu ăn để tránh
     * race condition với [selectFood] ghi đè [_bowlFood] trong lúc animation đang chạy.
     */
    private var activeMeal: BowlFood? = null

    val bowlPositionX = 0.72f
    val bedPositionX = 0.02f

    init {
        // Tự động ẩn thông báo sau 3.5s
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

        // Chu kỳ 60s cập nhật chỉ số
        startStatsTicker()

        if (_petState.value.isSleeping) {
            _actionState.value = PetActionState.SLEEPING
            _catPositionX.value = bedPositionX
            _facingDirection.value = -1f
            sleepJob?.cancel()
            sleepJob = null
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

    fun clearFeedback() {
        _petState.update { it.copy(feedbackMessage = "") }
    }

    fun showFeedback(msg: String, autoDismissMs: Long = 3000L) {
        _petState.update { it.copy(feedbackMessage = msg) }
    }

    // Hành vi ngẫu nhiên khi mèo ở trạng thái rảnh
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

    // Kiểm tra mèo có rảnh để thực hiện tương tác phụ không
    private fun isFreeForSideAction(): Boolean {
        if (_petState.value.isSleeping || _actionState.value == PetActionState.SLEEPING) return false
        return _actionState.value == PetActionState.IDLE || _actionState.value == PetActionState.PETTING
    }

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

    // Đặt thức ăn vào bát và điều hướng mèo tới ăn nếu đang rảnh
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

    // Xử lý ăn tại bát và quay về giữa phòng
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
            _petState.update { it.feed(lockedFood) }
            _bowlFood.value = BowlFood.EMPTY
            repository.persistPetState(_petState.value)

            // Quay đầu sang trái và đi về giữa phòng
            _facingDirection.value = -1f
            walkBackToCenter()
        }
    }

    
    private suspend fun walkBackToCenter() {
        _actionState.value = PetActionState.WALKING
        moveCatTo(0.5f, speed = 0.20f)
        _facingDirection.value = 1f // Quay lại nhìn thẳng / sang phải
        _actionState.value = PetActionState.IDLE
    }

    // Vuốt ve mèo; chạm liên tục chỉ gia hạn hiệu ứng chứ không cộng dồn chỉ số
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

    fun startPlaying() {
        idleLifeJob?.cancel()
        walkJob?.cancel()
        _actionState.value = PetActionState.PLAYING
    }

    fun cancelPlaying() {
        if (_actionState.value == PetActionState.PLAYING) {
            _actionState.value = PetActionState.IDLE
            startIdleLifeCycle()
        }
    }

    fun finishPlaying(
        happinessGain: Float = 25f,
        energyCost: Float = 18f,
        hungerCost: Float = 12f,
        expGain: Int = 15
    ) {
        _actionState.value = PetActionState.IDLE
        _petState.update { it.play(happinessGain, energyCost, hungerCost, expGain) }
        persistAndScheduleMoodReset()
        startIdleLifeCycle()
    }

    // Chuyển đổi trạng thái ngủ với chuỗi cử động di chuyển và vươn vai
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
                sleepJob?.cancel()
                sleepJob = null
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

    fun openWardrobe() {
        if (_petState.value.isSleeping || _actionState.value == PetActionState.SLEEPING) {
            showFeedback("${_petState.value.petName} đang ngủ say, đợi bé dậy rồi thay đồ nha! 💤", 2500L)
            return
        }
        if (_actionState.value == PetActionState.WALKING ||
            _actionState.value == PetActionState.EATING ||
            _actionState.value == PetActionState.STRETCHING
        ) {
            notifyBusy()
            return
        }
        _previewWardrobe.value = _petState.value.wardrobe
        _showWardrobe.value = true
    }

    fun closeWardrobe() {
        _previewWardrobe.value = _petState.value.wardrobe
        _showWardrobe.value = false
    }

    // Thử trang phục trong tủ đồ (có kiểm tra cấp độ mở khóa)
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

    fun resetPreviewToCurrent() {
        _previewWardrobe.value = _petState.value.wardrobe
    }

    fun resetPreviewToDefault() {
        _previewWardrobe.value = com.littlebuddy.pet.wardrobe.EquippedWardrobe()
    }

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

    private fun persistAndScheduleMoodReset() {
        repository.persistPetState(_petState.value)
        temporaryMoodJob?.cancel()
        temporaryMoodJob = viewModelScope.launch {
            delay(2500L)
            _petState.update { it.copy(temporaryMood = null) }
        }
    }

    /**
     * Gọi từ Activity.onResume() để bù trừ thời gian app bị background.
     * Xử lý trường hợp Android Doze Mode có thể đình chỉ statsJob trong khi app ở background.
     */
    fun onAppResume() {
        val now = System.currentTimeMillis()
        val elapsedMinutes = (now - lastForegroundTimestamp) / 60_000L
        if (elapsedMinutes >= 1L) {
            _petState.update { it.applyTimeElapsed(elapsedMinutes) }
            repository.persistPetState(_petState.value)
        }
        lastForegroundTimestamp = now
    }

    /**
     * Gọi từ Activity.onPause() để ghi lại thời điểm app vào background.
     */
    fun onAppPause() {
        lastForegroundTimestamp = System.currentTimeMillis()
        repository.persistPetState(_petState.value)
    }

    override fun onCleared() {
        super.onCleared()
        statsJob?.cancel()
        sleepJob?.cancel()
        temporaryMoodJob?.cancel()
        walkJob?.cancel()
        eatJob?.cancel()
        wakeJob?.cancel()
        idleLifeJob?.cancel()
        scratchJob?.cancel()
        pettingJob?.cancel()
    }
}
