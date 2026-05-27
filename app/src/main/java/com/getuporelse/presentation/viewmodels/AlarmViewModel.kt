package com.getuporelse.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.getuporelse.BuildConfig
import com.getuporelse.domain.alarm.DebugAlarmController
import com.getuporelse.domain.alarm.GetAlarmSettingsUseCase
import com.getuporelse.domain.alarm.ScheduleAlarmUseCase
import com.getuporelse.domain.models.AlarmSettings
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val debugAlarmController: DebugAlarmController
) : ViewModel() {

    private val _settings = MutableStateFlow(AlarmSettings())
    val settings: StateFlow<AlarmSettings> = _settings.asStateFlow()

    private val _uiState = MutableStateFlow(AlarmUiState())
    val uiState: StateFlow<AlarmUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getAlarmSettingsUseCase().collectLatest { settings ->
                _settings.value = settings
                _uiState.update { it.copy(targetReps = settings.targetReps) }
            }
        }
    }

    fun setRinging(isRinging: Boolean) {
        _uiState.update { it.copy(isRinging = isRinging) }
    }

    fun updateAlarm(hour: Int, minute: Int, targetReps: Int, isEnabled: Boolean) {
        viewModelScope.launch {
            scheduleAlarmUseCase(
                AlarmSettings(
                    hour = hour,
                    minute = minute,
                    targetReps = targetReps,
                    isEnabled = isEnabled
                )
            )
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
}
