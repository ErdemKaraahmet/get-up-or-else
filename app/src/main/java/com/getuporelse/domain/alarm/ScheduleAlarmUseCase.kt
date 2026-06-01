package com.getuporelse.domain.alarm

import com.getuporelse.domain.models.AlarmSettings
import javax.inject.Inject

class ScheduleAlarmUseCase @Inject constructor(
    private val repository: AlarmRepository,
    private val scheduler: AlarmScheduler
) {
    suspend operator fun invoke(settings: AlarmSettings, alarmToCancelId: Int? = null) {
        repository.updateAlarmSettings(settings)
        
        if (alarmToCancelId != null) {
            scheduler.cancel(alarmToCancelId)
        }
        
        settings.alarms.forEach { alarm ->
            if (alarm.isEnabled) {
                scheduler.schedule(alarm, settings.targetReps)
            } else {
                scheduler.cancel(alarm.id)
            }
        }
    }
}
