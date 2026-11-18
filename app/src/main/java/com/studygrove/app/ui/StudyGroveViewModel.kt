package com.studygrove.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.studygrove.app.data.local.StudyGroveLocalDataSource
import com.studygrove.app.data.model.HouseItemEdit
import com.studygrove.app.data.model.HouseObject
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StudyGroveViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = StudyGroveRepository(StudyGroveLocalDataSource(application.applicationContext))
    private val notificationScheduler = NotificationScheduler(application.applicationContext)
    private val soundEngine = SoundEngine(application.applicationContext)

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

    private val _gameStateLoaded = MutableStateFlow(false)
    val gameStateLoaded: StateFlow<Boolean> = _gameStateLoaded.asStateFlow()

    private val _sessionReviewPending = MutableStateFlow(false)
    val sessionReviewPending: StateFlow<Boolean> = _sessionReviewPending.asStateFlow()

    private val _selectedGardenItemId = MutableStateFlow<String?>(null)
    val selectedGardenItemId: StateFlow<String?> = _selectedGardenItemId.asStateFlow()

    private val _selectedHouseItemId = MutableStateFlow<String?>(null)
    val selectedHouseItemId: StateFlow<String?> = _selectedHouseItemId.asStateFlow()

    private val appVisible = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            val minimumSplash = launch { delay(1_100) }
            val loadedData = repository.gameData.first()
            minimumSplash.join()
            if (loadedData.activeTimer != null) {
                _screen.value = StudyGroveScreen.TIMER
            }
            _gameStateLoaded.value = true
        }
        viewModelScope.launch {
            combine(gameData, appVisible) { data, visible -> data to visible }.collectLatest { (data, visible) ->
                soundEngine.updateMusic(
                    enabled = data.settings.soundEnabled && visible,
                    themeSong = data.settings.themeSong,
                    timerActive = data.activeTimer != null
                )
            }
        }
        viewModelScope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                _nowMillis.value = now
                val timer = gameData.value.activeTimer
                if (timer != null && !timer.isPaused && !_sessionReviewPending.value && FocusTimerEngine.isComplete(timer, now)) {
                    _sessionReviewPending.value = true
                    _screen.value = StudyGroveScreen.TIMER
                    val settings = gameData.value.settings
                    notificationScheduler.showTimerCompleteNow(
                        enabled = settings.notificationsEnabled,
                        soundEnabled = settings.soundEnabled
                    )
                    notificationScheduler.cancelTimerComplete()
                }
                delay(1_000)
            }
        }
    }

    fun setAppVisible(visible: Boolean) {
        appVisible.value = visible
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
                _screen.value = StudyGroveScreen.TIMER
                notificationScheduler.scheduleTimerComplete(
                    triggerAtMillis = FocusTimerEngine.finishEpochMillis(timer, System.currentTimeMillis()),
                    enabled = data.settings.notificationsEnabled,
                    soundEnabled = data.settings.soundEnabled
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
                    enabled = data.settings.notificationsEnabled,
                    soundEnabled = data.settings.soundEnabled
                )
            }
            _message.value = result.message
            soundEngine.click(data.settings.soundEnabled)
        }
    }

    fun cancelTimer() {
        viewModelScope.launch {
            _sessionReviewPending.value = false
            val hadTimer = repository.current().activeTimer != null
            val result = repository.cancelTimer()
            notificationScheduler.cancelTimerComplete()
            _message.value = result.message
            if (hadTimer) soundEngine.cancelSession(gameData.value.settings.soundEnabled) else soundEngine.click(gameData.value.settings.soundEnabled)
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
                soundEngine.cancelSession(repository.current().settings.soundEnabled)
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
            when {
                result.success && selected != null -> soundEngine.planting(enabled)
                result.success && (result.message.startsWith("Moved") || result.message.startsWith("Removed")) -> {
                    soundEngine.takeOut(enabled)
                }
                else -> soundEngine.click(enabled)
            }
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
            val enabled = repository.current().settings.soundEnabled
            if (result.success && selected != null) soundEngine.planting(enabled) else soundEngine.click(enabled)
        }
    }

    fun editHouseItem(target: HouseObject, edit: HouseItemEdit) {
        viewModelScope.launch {
            val result = repository.editHouseItem(target, edit)
            _message.value = result.message
            val sound = repository.current().settings.soundEnabled
            if (result.success && edit == HouseItemEdit.Delete) soundEngine.takeOut(sound) else soundEngine.click(sound)
        }
    }

    fun equipCosmetic(itemId: String) {
        viewModelScope.launch {
            val result = repository.equipCosmetic(itemId)
            _message.value = result.message
            soundEngine.click(repository.current().settings.soundEnabled)
        }
    }

    fun setHouseThemeEnabled(themeId: String, enabled: Boolean) {
        viewModelScope.launch {
            val result = repository.setHouseThemeEnabled(themeId, enabled)
            _message.value = result.message
            soundEngine.click(repository.current().settings.soundEnabled)
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { data ->
                data.copy(settings = data.settings.copy(soundEnabled = enabled))
            }
            val data = repository.current()
            val timer = data.activeTimer
            if (timer != null && data.settings.notificationsEnabled) {
                notificationScheduler.scheduleTimerComplete(
                    triggerAtMillis = FocusTimerEngine.finishEpochMillis(timer, System.currentTimeMillis()),
                    enabled = true,
                    soundEnabled = enabled
                )
            }
            _message.value = if (enabled) "Sound on." else "Sound off."
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            repository.updateSettings { data ->
                data.copy(settings = data.settings.copy(notificationsEnabled = enabled))
            }
            val data = repository.current()
            val timer = data.activeTimer
            if (enabled && timer != null) {
                notificationScheduler.scheduleTimerComplete(
                    triggerAtMillis = FocusTimerEngine.finishEpochMillis(timer, System.currentTimeMillis()),
                    enabled = true,
                    soundEnabled = data.settings.soundEnabled
                )
            } else if (!enabled) {
                notificationScheduler.cancelTimerComplete()
            }
            _message.value = if (enabled) "Timer notifications enabled." else "Timer notifications disabled."
        }
    }

    fun setThemeSong(themeSong: String) {
        viewModelScope.launch {
            val safeTheme = themeSong.takeIf { it in setOf("theme_1", "theme_2", "theme_3") } ?: "theme_1"
            repository.updateSettings { data ->
                data.copy(settings = data.settings.copy(themeSong = safeTheme))
            }
            _message.value = "Theme ${safeTheme.removePrefix("theme_")} selected."
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
        val previousLevel = repository.current().player.level
        val completedReward = repository.completeActiveTimer(nowMillis) ?: return
        notificationScheduler.cancelTimerComplete()
        _reward.value = completedReward
        _screen.value = StudyGroveScreen.GARDEN
        val data = repository.current()
        soundEngine.sessionFinished(data.settings.soundEnabled)
        if (data.player.level > previousLevel) {
            soundEngine.levelUp(data.settings.soundEnabled)
        }
    }

    override fun onCleared() {
        soundEngine.release()
        super.onCleared()
    }
}
