package com.getuporelse.domain.models

data class Alarm(
    val id: Int,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean
)

data class AlarmSettings(
    val alarms: List<Alarm> = listOf(
        Alarm(id = 1, hour = 7, minute = 0, isEnabled = false)
    ),
    val targetReps: Int = 10,
    val useGpu: Boolean = false
)
