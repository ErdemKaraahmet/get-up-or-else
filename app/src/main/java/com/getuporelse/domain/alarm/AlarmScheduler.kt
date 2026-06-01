package com.getuporelse.domain.alarm

import com.getuporelse.domain.models.Alarm

interface AlarmScheduler {
    fun schedule(alarm: Alarm, targetReps: Int)
    fun cancel(alarmId: Int)
}
