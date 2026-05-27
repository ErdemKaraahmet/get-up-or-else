package com.getuporelse.domain.alarm

import com.getuporelse.domain.models.AlarmSettings
import javax.inject.Inject

class ScheduleAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler
) {
    suspend operator fun invoke(settings: AlarmSettings) {
        repository.updateAlarmSettings(settings)
        if (settings.isEnabled) {
            scheduler.schedule(settings)
        } else {
            scheduler.cancel()
        }
    }
}
