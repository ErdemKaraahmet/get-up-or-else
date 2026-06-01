package com.getuporelse.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.getuporelse.BuildConfig
import com.getuporelse.domain.alarm.AlarmController
import com.getuporelse.domain.alarm.DebugAlarmController
import com.getuporelse.domain.alarm.GetAlarmSettingsUseCase
import com.getuporelse.domain.alarm.ScheduleAlarmUseCase
import com.getuporelse.domain.alarm.UpdateAlarmSettingsUseCase
import com.getuporelse.domain.exercise.ExerciseDetector
import com.getuporelse.domain.models.Alarm
import com.getuporelse.domain.models.AlarmSettings
import com.getuporelse.domain.pose.PoseAnalyzer
import com.getuporelse.domain.pose.PoseResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlarmViewModel @Inject constructor(
    private val getAlarmSettingsUseCase: GetAlarmSettingsUseCase,
    private val scheduleAlarmUseCase: ScheduleAlarmUseCase,
    private val updateAlarmSettingsUseCase: UpdateAlarmSettingsUseCase,
    private val alarmController: AlarmController,
    private val debugAlarmController: DebugAlarmController,
    val poseAnalyzer: PoseAnalyzer,
    private val exerciseDetector: ExerciseDetector
) : ViewModel() {

    private val _settings = MutableStateFlow(AlarmSettings())
    val settings: StateFlow<AlarmSettings> = _settings.asStateFlow()

    private val _isSettingsLoaded = MutableStateFlow(false)
    val isSettingsLoaded: StateFlow<Boolean> = _isSettingsLoaded.asStateFlow()

    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAlarmSettingsUseCase().collectLatest { settings ->
                _settings.value = settings
                _uiState.update { it.copy(targetReps = settings.targetReps) }
                poseAnalyzer.updateGpuSetting(settings.useGpu)
                _isSettingsLoaded.value = true
            }
        }

        setupPoseAnalysis()
    }

    private var debugRepOffset = 0

    private fun setupPoseAnalysis() {
        poseAnalyzer.setResultListener { poseResult ->
            processPoseResult(poseResult)
        }
        poseAnalyzer.setErrorListener { error ->
            _uiState.update { it.copy(feedback = "Pose detection error") }
        }
    }

    private var lastFrameTimeNs = 0L

    /**
     * Process a pose result through the exercise detector.
     * Called from the MediaPipe callback thread — UI state updates are thread-safe via StateFlow.
     */
    private fun processPoseResult(result: PoseResult) {
        if (!_uiState.value.isExercising) return

        val exerciseState = exerciseDetector.processPose(result)
        val targetReps = _uiState.value.targetReps
        
        // Add the debug offset so manual increments aren't overwritten
        val adjustedRepCount = exerciseState.repCount + debugRepOffset
        val isComplete = adjustedRepCount >= targetReps

        // Calculate FPS
        val currentFrameTimeNs = System.nanoTime()
        var currentFps = _uiState.value.fps
        if (lastFrameTimeNs != 0L) {
            val frameIntervalNs = currentFrameTimeNs - lastFrameTimeNs
            if (frameIntervalNs > 0) {
                currentFps = 1_000_000_000.0 / frameIntervalNs
            }
        }
        lastFrameTimeNs = currentFrameTimeNs

        _uiState.update {
            it.copy(
                repCount = adjustedRepCount,
                feedback = exerciseState.feedback,
                isComplete = isComplete,
                debugElbowAngle = exerciseState.debugAngle,
                fps = currentFps,
                currentLandmarks = result.landmarks
            )
        }
    }

    fun setRinging(isRinging: Boolean, targetReps: Int? = null) {
        _uiState.update { state ->
            state.copy(
                isRinging = isRinging,
                isSettingsOpen = if (isRinging) false else state.isSettingsOpen,
                targetReps = targetReps ?: state.targetReps
            )
        }
    }

    fun setSettingsOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isSettingsOpen = isOpen) }
    }

    fun startExercise() {
        debugRepOffset = 0
        _uiState.update { it.copy(isExercising = true, isSettingsOpen = false) }
    }

    fun completeExercise() {
        alarmController.stopAlarm()
        debugRepOffset = 0
        _uiState.update {
            it.copy(
                isRinging = false,
                isExercising = false,
                isComplete = false,
                repCount = 0
            )
        }
    }

    fun addAlarm(hour: Int, minute: Int) {
        viewModelScope.launch {
            val currentSettings = _settings.value
            val newId = (currentSettings.alarms.maxOfOrNull { it.id } ?: 0) + 1
            val newAlarm = Alarm(id = newId, hour = hour, minute = minute, isEnabled = true)
            val newSettings = currentSettings.copy(alarms = currentSettings.alarms + newAlarm)
            scheduleAlarmUseCase(newSettings)
        }
    }

    fun updateAlarm(alarmId: Int, hour: Int, minute: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = _settings.value
            val updatedAlarms = currentSettings.alarms.map { alarm ->
                if (alarm.id == alarmId) {
                    alarm.copy(hour = hour, minute = minute, isEnabled = isEnabled)
                } else {
                    alarm
                }
            }
            val newSettings = currentSettings.copy(alarms = updatedAlarms)
            scheduleAlarmUseCase(newSettings)
        }
    }

    fun deleteAlarm(alarmId: Int) {
        viewModelScope.launch {
            val currentSettings = _settings.value
            val updatedAlarms = currentSettings.alarms.filter { it.id != alarmId }
            val newSettings = currentSettings.copy(alarms = updatedAlarms)
            scheduleAlarmUseCase(newSettings, alarmToCancelId = alarmId)
        }
    }

    fun toggleAlarm(alarmId: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            val currentSettings = _settings.value
            val updatedAlarms = currentSettings.alarms.map { alarm ->
                if (alarm.id == alarmId) {
                    alarm.copy(isEnabled = isEnabled)
                } else {
                    alarm
                }
            }
            val newSettings = currentSettings.copy(alarms = updatedAlarms)
            scheduleAlarmUseCase(newSettings)
        }
    }

    fun updateGlobalReps(targetReps: Int) {
        viewModelScope.launch {
            val currentSettings = _settings.value
            val newSettings = currentSettings.copy(targetReps = targetReps)
            scheduleAlarmUseCase(newSettings)
        }
    }

    fun updateGpuSetting(useGpu: Boolean) {
        viewModelScope.launch {
            val currentSettings = _settings.value
            val newSettings = currentSettings.copy(useGpu = useGpu)
            updateAlarmSettingsUseCase(newSettings)
        }
    }

    fun triggerDebugAlarm() {
        if (!BuildConfig.DEBUG) return

        debugAlarmController.triggerAlarm()
        setRinging(true)
    }

    fun stopDebugAlarm() {
        if (!BuildConfig.DEBUG) return

        debugAlarmController.stopAlarm()
        setRinging(false)
    }

    fun debugIncrementRep() {
        if (!BuildConfig.DEBUG) return

        debugRepOffset++

        _uiState.update { state ->
            val newCount = (state.repCount + 1).coerceAtMost(state.targetReps)
            state.copy(
                repCount = newCount,
                isComplete = newCount >= state.targetReps
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        poseAnalyzer.close()
    }
}
