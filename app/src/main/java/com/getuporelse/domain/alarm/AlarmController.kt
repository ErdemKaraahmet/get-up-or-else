package com.getuporelse.domain.alarm

/**
 * Controls the alarm audio lifecycle.
 * Used to stop the alarm service when exercise is completed.
 */
interface AlarmController {
    fun stopAlarm()
}
