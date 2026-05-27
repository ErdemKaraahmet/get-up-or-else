package com.getuporelse.domain.alarm

import com.getuporelse.domain.models.AlarmSettings
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAlarmSettingsUseCase @Inject constructor(
    private val repository: AlarmRepository
) {
    operator fun invoke(): Flow<AlarmSettings> = repository.getAlarmSettings()
}
