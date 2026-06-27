package com.getuporelse.domain.alarm

/**
 * Controls the alarm audio lifecycle.
 */
interface AlarmController {
    fun triggerAlarm(targetReps: Int)
    fun stopAlarm()
}
