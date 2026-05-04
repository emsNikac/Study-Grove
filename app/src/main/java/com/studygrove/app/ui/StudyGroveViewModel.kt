package com.studygrove.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.studygrove.app.data.local.StudyGroveLocalDataSource
import com.studygrove.app.data.model.GameData
import com.studygrove.app.data.model.ShopCatalog
import com.studygrove.app.data.model.StudyReward
import com.studygrove.app.data.model.TimerPreset
import com.studygrove.app.data.model.normalized
import com.studygrove.app.data.repository.StudyGroveRepository
import com.studygrove.app.domain.timer.FocusTimerEngine
import com.studygrove.app.util.NotificationScheduler
import com.studygrove.app.util.SoundEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudyGroveViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StudyGroveRepository(StudyGroveLocalDataSource(application.applicationContext))
    private val notificationScheduler = NotificationScheduler(application.applicationContext)
    private val soundEngine = SoundEngine()

    val gameData: StateFlow<GameData> = repository.gameData.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GameData.default().normalized()
    )

    private val _screen = MutableStateFlow(StudyGroveScreen.GARDEN)
    val screen: StateFlow<StudyGroveScreen> = _screen.asStateFlow()

    private val _nowMillis = MutableStateFlow(System.currentTimeMillis())
    val nowMillis: StateFlow<Long> = _nowMillis.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _reward = MutableStateFlow<StudyReward?>(null)
    val reward: StateFlow<StudyReward?> = _reward.asStateFlow()

    private val _sessionReviewPending = MutableStateFlow(false)
    val sessionReviewPending: StateFlow<Boolean> = _sessionReviewPending.asStateFlow()

    private val _selectedGardenItemId = MutableStateFlow<String?>(null)
    val selectedGardenItemId: StateFlow<String?> = _selectedGardenItemId.asStateFlow()

    private val _selectedHouseItemId = MutableStateFlow<String?>(null)
    val selectedHouseItemId: StateFlow<String?> = _selectedHouseItemId.asStateFlow()

    init {
        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                _nowMillis.value = now
                val timer = gameData.value.activeTimer
                if (timer != null && !timer.isPaused && !_sessionReviewPending.value && FocusTimerEngine.isComplete(timer, now)) {
                    _sessionReviewPending.value = true
                    _screen.value = StudyGroveScreen.TIMER
                    notificationScheduler.cancelTimerComplete()
                    soundEngine.complete(gameData.value.settings.soundEnabled)
                }
                delay(1_000)
            }
        }
    }

    fun navigate(screen: StudyGroveScreen) {
        _screen.value = screen
        soundEngine.click(gameData.value.settings.soundEnabled)
    }

    fun startTimer(preset: TimerPreset, customMinutes: Int? = null) {
        viewModelScope.launch {
            _sessionReviewPending.value = false
            val timer = repository.startTimer(preset, customMinutes)
            val data = repository.current()
            if (timer != null) {
                notificationScheduler.scheduleTimerComplete(
                    triggerAtMillis = FocusTimerEngine.finishEpochMillis(timer, System.currentTimeMillis()),
                    enabled = data.settings.notificationsEnabled
                )
                _message.value = "Timer started. Your grove is waiting patiently."
                soundEngine.click(data.settings.soundEnabled)
            }
        }
    }

    fun togglePause() {
        viewModelScope.launch {
            val result = repository.togglePause()
            val data = repository.current()
            val timer = data.activeTimer
            if (timer == null || timer.isPaused || !result.success) {
                notificationScheduler.cancelTimerComplete()
            } else {
                notificationScheduler.scheduleTimerComplete(
                    triggerAtMillis = FocusTimerEngine.finishEpochMillis(timer, System.currentTimeMillis()),
                    enabled = data.settings.notificationsEnabled
                )
            }
            _message.value = result.message
            soundEngine.click(data.settings.soundEnabled)
        }
    }

    fun cancelTimer() {
        viewModelScope.launch {
            _sessionReviewPending.value = false
            val result = repository.cancelTimer()
            notificationScheduler.cancelTimerComplete()
            _message.value = result.message
            soundEngine.click(gameData.value.settings.soundEnabled)
        }
    }

    fun confirmFinishedSession(countSession: Boolean) {
        viewModelScope.launch {
            _sessionReviewPending.value = false
            if (countSession) {
                completeTimer(System.currentTimeMillis())
            } else {
                val result = repository.discardFinishedTimer()
                notificationScheduler.cancelTimerComplete()
                _message.value = result.message
                soundEngine.click(repository.current().settings.soundEnabled)
            }
        }
    }

    fun buyItem(itemId: String) {
        viewModelScope.launch {
            val result = repository.buyItem(itemId)
            _message.value = result.message
            val enabled = repository.current().settings.soundEnabled
            if (result.success) soundEngine.reward(enabled) else soundEngine.click(enabled)
        }
    }

    fun selectGardenItem(itemId: String) {
        _selectedGardenItemId.value = itemId
        _selectedHouseItemId.value = null
        _screen.value = StudyGroveScreen.GARDEN
        _message.value = "Tap a garden tile to place ${ShopCatalog.byId(itemId)?.name.orEmpty()}."
    }

    fun clearGardenSelection() {
        _selectedGardenItemId.value = null
    }

    fun onGardenTileTapped(x: Int, y: Int) {
        viewModelScope.launch {
            val selected = _selectedGardenItemId.value
            val result = if (selected != null) {
                repository.placeGardenItem(selected, x, y)
            } else {
                repository.interactGardenTile(x, y)
            }
            if (result.success && selected != null) _selectedGardenItemId.value = null
            _message.value = result.message
            val enabled = repository.current().settings.soundEnabled
            if (result.success) soundEngine.reward(enabled) else soundEngine.click(enabled)
        }
    }

    fun selectHouseItem(itemId: String) {
        _selectedHouseItemId.value = itemId
        _selectedGardenItemId.value = null
        _screen.value = StudyGroveScreen.HOUSE
        _message.value = "Tap a room tile to place ${ShopCatalog.byId(itemId)?.name.orEmpty()}."
    }

    fun clearHouseSelection() {
        _selectedHouseItemId.value = null
    }

    fun onHouseTileTapped(x: Int, y: Int) {
        viewModelScope.launch {
            val selected = _selectedHouseItemId.value
            val result = if (selected != null) {
                repository.placeHouseItem(selected, x, y)
            } else {
                repository.interactHouseTile(x, y)
            }
            if (result.success && selected != null) _selectedHouseItemId.value = null
            _message.value = result.message
            soundEngine.click(repository.current().settings.soundEnabled)
        }
    }

    fun equipCosmetic(itemId: String) {
        viewModelScope.launch {
            val result = repository.equipCosmetic(itemId)
            _message.value = result.message
            soundEngine.click(repository.current().settings.soundEnabled)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { data ->
                data.copy(settings = data.settings.copy(soundEnabled = enabled))
            }
            _message.value = if (enabled) "Sound on." else "Sound off."
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { data ->
                data.copy(settings = data.settings.copy(notificationsEnabled = enabled))
            }
            val timer = repository.current().activeTimer
            if (enabled && timer != null) {
                notificationScheduler.scheduleTimerComplete(
                    triggerAtMillis = FocusTimerEngine.finishEpochMillis(timer, System.currentTimeMillis()),
                    enabled = true
                )
            } else if (!enabled) {
                notificationScheduler.cancelTimerComplete()
            }
            _message.value = if (enabled) "Timer notifications enabled." else "Timer notifications disabled."
        }
    }

    fun setGentleRemindersEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { data ->
                data.copy(settings = data.settings.copy(gentleRemindersEnabled = enabled))
            }
            notificationScheduler.scheduleDailyReminder(enabled && repository.current().settings.notificationsEnabled)
            _message.value = if (enabled) "Daily study reminder set for evenings." else "Daily reminder off."
        }
    }

    fun setTestModeEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { data ->
                data.copy(settings = data.settings.copy(testModeEnabled = enabled))
            }
            _message.value = if (enabled) {
                "Test mode on. All items are unlocked and unlimited."
            } else {
                "Test mode off. Normal shop and inventory rules restored."
            }
        }
    }

    fun setDefaultTimerMinutes(minutes: Int) {
        viewModelScope.launch {
            repository.updateSettings { data ->
                data.copy(settings = data.settings.copy(defaultTimerMinutes = minutes.coerceIn(5, 180)))
            }
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            repository.resetProgress()
            _selectedGardenItemId.value = null
            _selectedHouseItemId.value = null
            _reward.value = null
            _sessionReviewPending.value = false
            notificationScheduler.cancelTimerComplete()
            _message.value = "Progress reset. A fresh grove is ready."
        }
    }

    fun dismissMessage() {
        _message.value = null
    }

    fun dismissReward() {
        _reward.value = null
    }

    private suspend fun completeTimer(nowMillis: Long) {
        val completedReward = repository.completeActiveTimer(nowMillis) ?: return
        notificationScheduler.cancelTimerComplete()
        _reward.value = completedReward
        _screen.value = StudyGroveScreen.GARDEN
        soundEngine.complete(repository.current().settings.soundEnabled)
    }

    override fun onCleared() {
        soundEngine.release()
        super.onCleared()
    }
}
