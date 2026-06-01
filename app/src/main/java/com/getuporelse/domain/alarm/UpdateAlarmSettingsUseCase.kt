package com.getuporelse.domain.alarm

import com.getuporelse.domain.models.AlarmSettings
import javax.inject.Inject

class UpdateAlarmSettingsUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    suspend operator fun invoke(settings: AlarmSettings) {
        repository.updateAlarmSettings(settings)
    }
}
