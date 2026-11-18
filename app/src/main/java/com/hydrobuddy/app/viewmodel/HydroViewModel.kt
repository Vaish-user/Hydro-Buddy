package com.hydrobuddy.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.hydrobuddy.app.data.database.WaterLogRepository
import com.hydrobuddy.app.data.datastore.HydroDataStore
import com.hydrobuddy.app.data.model.AnimationSpeed
import com.hydrobuddy.app.data.model.AppDayOfWeek
import com.hydrobuddy.app.data.model.AppearanceSettings
import com.hydrobuddy.app.data.model.AppThemeMode
import com.hydrobuddy.app.data.model.CharacterCustomization
import com.hydrobuddy.app.data.model.CharacterPosition
import com.hydrobuddy.app.data.model.CharacterSize
import com.hydrobuddy.app.data.model.DailyActivityRecord
import com.hydrobuddy.app.data.model.HydrationStatistics
import com.hydrobuddy.app.data.model.LoggedIntakeItem
import com.hydrobuddy.app.data.model.PipPersonality
import com.hydrobuddy.app.data.model.ReminderSettings
import com.hydrobuddy.app.service.HydrationForegroundTracker
import com.hydrobuddy.app.service.HydrationReminderScheduler
import com.hydrobuddy.app.ui.mascot.PipInteractionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray

class HydroViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStore = HydroDataStore(application)
    val waterLogRepository = WaterLogRepository(application, dataStore)

    val dailyGoalMl: StateFlow<Int> = dataStore.dailyGoalMlFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 2000)

    val goalUnit: StateFlow<String> = dataStore.goalUnitFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "ml")

    val reminderSettings: StateFlow<ReminderSettings> = dataStore.reminderSettingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, ReminderSettings())

    val nextReminderAt: StateFlow<Long> = dataStore.nextReminderAtFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0L)

    val themeMode: StateFlow<AppThemeMode> = dataStore.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppThemeMode.SYSTEM)

    private val _characterCustomization = MutableStateFlow(CharacterCustomization())
    val characterCustomization: StateFlow<CharacterCustomization> = _characterCustomization.asStateFlow()

    private val _appearanceSettings = MutableStateFlow(AppearanceSettings())
    val appearanceSettings: StateFlow<AppearanceSettings> = _appearanceSettings.asStateFlow()

    val todayIntakeMl: StateFlow<Int> = waterLogRepository.todayTotalMlFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val currentStreakDays: StateFlow<Int> = waterLogRepository.getStreakFlow(dataStore.dailyGoalMlFlow)
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val todayLogs: StateFlow<List<LoggedIntakeItem>> = waterLogRepository.todayLogsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val weeklyActivity: StateFlow<List<DailyActivityRecord>> = waterLogRepository.get7DayActivityFlow(dataStore.dailyGoalMlFlow)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Tracks last deleted log for undo functionality
    private val _lastDeletedLog = MutableStateFlow<LoggedIntakeItem?>(null)
    val lastDeletedLog: StateFlow<LoggedIntakeItem?> = _lastDeletedLog.asStateFlow()

    private val _statistics = MutableStateFlow(HydrationStatistics(todayIntakeMl = 0, currentStreakDays = 0, isMockData = false))
    val statistics: StateFlow<HydrationStatistics> = _statistics.asStateFlow()

    // Temporary active preview message tab on character customization screen
    private val _selectedPreviewTab = MutableStateFlow(PreviewMessageType.GREETING)
    val selectedPreviewTab: StateFlow<PreviewMessageType> = _selectedPreviewTab.asStateFlow()

    // Persistent onboarding completion state (null while reading DataStore)
    private val _isOnboardingCompleted = MutableStateFlow<Boolean?>(null)
    val isOnboardingCompleted: StateFlow<Boolean?> = _isOnboardingCompleted.asStateFlow()

    // Pip interactive reminder state machine
    private val _pipInteractionState = MutableStateFlow(PipInteractionState.HIDDEN)
    val pipInteractionState: StateFlow<PipInteractionState> = _pipInteractionState.asStateFlow()

    private fun parseLogs(json: String): List<LoggedIntakeItem> {
        val list = mutableListOf<LoggedIntakeItem>()
        if (json.isBlank() || json == "[]") return list
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    LoggedIntakeItem(
                        title = obj.optString("title", "Water"),
                        amountMl = obj.optInt("amount", 250),
                        timeFormatted = obj.optString("time", "12:00")
                    )
                )
            }
        } catch (e: Exception) {
            // ignore
        }
        return list
    }

    init {
        // Collect onboarding state
        viewModelScope.launch {
            dataStore.onboardingCompletedFlow.collect { completed ->
                android.util.Log.d("HydroBuddyLaunch", "dataStore emitted onboardingCompleted: $completed")
                _isOnboardingCompleted.value = completed
            }
        }

        viewModelScope.launch {
            combine(
                waterLogRepository.todayTotalMlFlow,
                waterLogRepository.getStreakFlow(dataStore.dailyGoalMlFlow),
                waterLogRepository.get7DayActivityFlow(dataStore.dailyGoalMlFlow),
                dataStore.dailyGoalMlFlow
            ) { todayMl, streak, weekly, goal ->
                _statistics.update { current ->
                    current.copy(
                        todayIntakeMl = todayMl,
                        dailyTargetMl = goal,
                        currentStreakDays = streak,
                        weeklyActivity = weekly,
                        isMockData = false
                    )
                }
            }.collect {}
        }

        viewModelScope.launch {
            dataStore.characterCustomizationFlow.collect { cust ->
                _characterCustomization.value = cust
            }
        }

        viewModelScope.launch {
            dataStore.appearanceSettingsFlow.collect { app ->
                _appearanceSettings.value = app
            }
        }

        // Listen for foreground reminder triggers from alarms/receivers
        viewModelScope.launch {
            HydrationForegroundTracker.foregroundTriggerEvents.collect {
                android.util.Log.d("HydroViewModel", "Received foregroundTriggerEvent! isEnabled=${reminderSettings.value.isEnabled}, isRunning=${reminderSettings.value.isRunning}")
                triggerReminderInteraction()
            }
        }
    }

    // --- PIP INTERACTIVE REMINDER ACTIONS ---

    fun triggerReminderInteraction() {
        android.util.Log.d("HydroViewModel", "triggerReminderInteraction invoked! current state=${_pipInteractionState.value}")
        _pipInteractionState.value = PipInteractionState.WALKING_IN
    }

    fun onPipReachedPosition() {
        if (_pipInteractionState.value == PipInteractionState.WALKING_IN) {
            _pipInteractionState.value = PipInteractionState.IDLE_REMINDER
        }
    }

    fun onUserReminderResponse(accepted: Boolean) {
        if (_pipInteractionState.value != PipInteractionState.IDLE_REMINDER) return
        recordReminderResponse(accepted)
        if (accepted) {
            logIntake(250, "Glass of water")
        }
        _pipInteractionState.value = if (accepted) {
            PipInteractionState.HAPPY_RESPONSE
        } else {
            PipInteractionState.SAD_RESPONSE
        }
    }

    fun startPipWalkOut() {
        if (_pipInteractionState.value == PipInteractionState.HAPPY_RESPONSE ||
            _pipInteractionState.value == PipInteractionState.SAD_RESPONSE ||
            _pipInteractionState.value == PipInteractionState.IDLE_REMINDER
        ) {
            _pipInteractionState.value = PipInteractionState.WALKING_OUT
        }
    }

    fun onPipWalkOutFinished() {
        _pipInteractionState.value = PipInteractionState.HIDDEN
        scheduleNextReminderAfterInteraction()
    }

    fun dismissPipInteraction() {
        _pipInteractionState.value = PipInteractionState.HIDDEN
        scheduleNextReminderAfterInteraction()
    }

    fun triggerReminderNow() {
        val app = getApplication<Application>()
        HydrationReminderScheduler.cancelReminders(app)
        triggerReminderInteraction()
        viewModelScope.launch {
            val settings = reminderSettings.value
            HydrationReminderScheduler.scheduleNextReminder(app, settings)
        }
    }

    private fun scheduleNextReminderAfterInteraction() {
        viewModelScope.launch {
            val settings = reminderSettings.value
            if (settings.isEnabled && settings.isRunning) {
                HydrationReminderScheduler.scheduleNextReminder(getApplication(), settings)
            }
        }
    }

    // --- REMINDER SETTINGS ACTIONS ---

    fun toggleReminderEnabled(enabled: Boolean) {
        val updated = reminderSettings.value.copy(isEnabled = enabled)
        persistReminderSettings(updated)
    }

    fun toggleReminderRunning() {
        val updated = reminderSettings.value.copy(isRunning = !reminderSettings.value.isRunning)
        persistReminderSettings(updated)
    }

    fun setPresetInterval(minutes: Int) {
        val updated = reminderSettings.value.copy(
            intervalMinutes = minutes,
            isCustomInterval = false
        )
        persistReminderSettings(updated)
    }

    fun setCustomInterval(minutes: Int) {
        val updated = reminderSettings.value.copy(
            intervalMinutes = minutes,
            isCustomInterval = true
        )
        persistReminderSettings(updated)
    }

    fun setStartTime(time: String) {
        val updated = reminderSettings.value.copy(startTime = time)
        persistReminderSettings(updated)
    }

    fun setEndTime(time: String) {
        val updated = reminderSettings.value.copy(endTime = time)
        persistReminderSettings(updated)
    }

    fun toggleActiveDay(day: AppDayOfWeek) {
        val current = reminderSettings.value
        val updatedDays = if (current.activeDays.contains(day)) {
            if (current.activeDays.size > 1) current.activeDays - day else current.activeDays
        } else {
            current.activeDays + day
        }
        val updated = current.copy(activeDays = updatedDays)
        persistReminderSettings(updated)
    }

    fun toggleSound(enabled: Boolean) {
        val updated = reminderSettings.value.copy(soundEnabled = enabled)
        persistReminderSettings(updated)
    }

    fun toggleVibration(enabled: Boolean) {
        val updated = reminderSettings.value.copy(vibrationEnabled = enabled)
        persistReminderSettings(updated)
    }

    private fun persistReminderSettings(settings: ReminderSettings) {
        viewModelScope.launch {
            dataStore.saveReminderSettings(settings)
            HydrationReminderScheduler.scheduleNextReminder(getApplication(), settings)
        }
    }

    // --- CHARACTER CUSTOMIZATION ACTIONS ---

    fun updateCharacterName(name: String) {
        _characterCustomization.update { it.copy(characterName = name) }
        persistCharacterCustomization()
    }

    fun updatePersonality(personality: com.hydrobuddy.app.data.model.PipPersonality) {
        _characterCustomization.update { it.copy(personality = personality) }
        persistCharacterCustomization()
    }

    fun updateWaterReminderMessage(msg: String) {
        _characterCustomization.update { it.copy(waterReminderMessage = msg) }
        persistCharacterCustomization()
    }

    fun updateYesButtonText(text: String) {
        _characterCustomization.update { it.copy(yesButtonText = text) }
        persistCharacterCustomization()
    }

    fun updateNoButtonText(text: String) {
        _characterCustomization.update { it.copy(noButtonText = text) }
        persistCharacterCustomization()
    }

    fun updateYesResponseMessage(msg: String) {
        _characterCustomization.update { it.copy(yesResponseMessage = msg) }
        persistCharacterCustomization()
    }

    fun updateHappyEncouragementMessage(msg: String) {
        _characterCustomization.update { it.copy(happyEncouragementMessage = msg) }
        persistCharacterCustomization()
    }

    fun updateNoResponseMessage(msg: String) {
        _characterCustomization.update { it.copy(noResponseMessage = msg) }
        persistCharacterCustomization()
    }

    fun updateMischievousKnifeMessage(msg: String) {
        _characterCustomization.update { it.copy(mischievousKnifeMessage = msg) }
        persistCharacterCustomization()
    }

    fun updateEntranceMessage(msg: String) {
        _characterCustomization.update { it.copy(entranceMessage = msg) }
        persistCharacterCustomization()
    }

    fun updateExitMessage(msg: String) {
        _characterCustomization.update { it.copy(exitMessage = msg) }
        persistCharacterCustomization()
    }

    fun saveFullCharacterCustomization(customization: CharacterCustomization) {
        _characterCustomization.value = customization
        persistCharacterCustomization()
    }

    fun restoreDefaultCharacterCustomization() {
        val defaults = CharacterCustomization.default()
        _characterCustomization.value = defaults
        viewModelScope.launch {
            dataStore.restoreDefaultCharacterCustomization()
        }
    }

    // Legacy aliases
    fun updateGreetingMessage(msg: String) = updateEntranceMessage(msg)
    fun updateSadEncouragementMessage(msg: String) = updateMischievousKnifeMessage(msg)

    fun setPreviewTab(tab: PreviewMessageType) {
        _selectedPreviewTab.value = tab
    }

    private fun persistCharacterCustomization() {
        viewModelScope.launch {
            dataStore.saveCharacterCustomization(_characterCustomization.value)
        }
    }

    // --- SETTINGS ACTIONS ---

    fun updateDailyGoal(goalMl: Int, unit: String = goalUnit.value): Boolean {
        if (goalMl !in 500..6000) return false
        viewModelScope.launch {
            dataStore.saveDailyGoal(goalMl, unit)
        }
        return true
    }

    fun updateGoalUnit(unit: String) {
        viewModelScope.launch {
            dataStore.saveDailyGoal(dailyGoalMl.value, unit)
        }
    }

    fun updateReminderInterval(minutes: Int) {
        val clamped = minutes.coerceIn(15, 180)
        val updated = reminderSettings.value.copy(intervalMinutes = clamped)
        persistReminderSettings(updated)
    }

    fun updateActiveHours(start: String, end: String) {
        val updated = reminderSettings.value.copy(startTime = start, endTime = end)
        persistReminderSettings(updated)
    }

    fun updatePipScale(scale: Float) {
        val clamped = scale.coerceIn(0.5f, 1.5f)
        _appearanceSettings.update { it.copy(pipScale = clamped) }
        viewModelScope.launch {
            dataStore.savePipScale(clamped)
        }
    }

    fun updateThemeMode(mode: AppThemeMode) {
        viewModelScope.launch {
            dataStore.saveThemeMode(mode)
        }
    }

    // --- APPEARANCE SETTINGS ACTIONS ---

    fun updateCharacterSize(size: CharacterSize) {
        _appearanceSettings.update { it.copy(characterSize = size) }
        persistAppearanceSettings()
    }

    fun updateCharacterPosition(position: CharacterPosition) {
        _appearanceSettings.update { it.copy(characterPosition = position) }
        persistAppearanceSettings()
    }

    fun updateAnimationSpeed(speed: AnimationSpeed) {
        _appearanceSettings.update { it.copy(animationSpeed = speed) }
        persistAppearanceSettings()
    }

    fun toggleSoundEffects(enabled: Boolean) {
        _appearanceSettings.update { it.copy(soundEffectsEnabled = enabled) }
        persistAppearanceSettings()
    }

    private fun persistAppearanceSettings() {
        viewModelScope.launch {
            dataStore.saveAppearanceSettings(_appearanceSettings.value)
        }
    }

    // --- HYDRATION STATS & ACTIONS ---

    fun logIntake(amountMl: Int, title: String = "Glass of water") {
        viewModelScope.launch {
            waterLogRepository.logIntake(amountMl, title)
        }
    }

    fun deleteWaterLog(item: LoggedIntakeItem) {
        viewModelScope.launch {
            _lastDeletedLog.value = item
            waterLogRepository.deleteLog(item.id)
        }
    }

    fun undoDeleteLog() {
        val item = _lastDeletedLog.value ?: return
        viewModelScope.launch {
            waterLogRepository.restoreLog(item)
            _lastDeletedLog.value = null
        }
    }

    fun dismissUndoSnackbar() {
        _lastDeletedLog.value = null
    }

    fun recordReminderResponse(isYes: Boolean) {
        _statistics.update { current ->
            val newYes = if (isYes) current.yesResponses + 1 else current.yesResponses
            val newNo = if (!isYes) current.noResponses + 1 else current.noResponses
            val newTotal = current.totalRemindersToday + 1
            val consistency = if (newTotal > 0) ((newYes.toFloat() / newTotal.toFloat()) * 100).toInt() else 0
            current.copy(
                totalRemindersToday = newTotal,
                yesResponses = newYes,
                noResponses = newNo,
                dailyConsistencyPercentage = consistency
            )
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            dataStore.setOnboardingCompleted(true)
            _isOnboardingCompleted.value = true
        }
    }
}

enum class PreviewMessageType(val label: String) {
    REMINDER("Reminder"),
    YES_RESPONSE("Yes Response"),
    NO_RESPONSE("No Response"),
    MISCHIEVOUS("Rude Notice"),
    HAPPY("Cheer"),
    ENTRANCE("Entrance"),
    EXIT("Exit"),
    GREETING("Greeting"),
    SAD("Sad Notice")
}

