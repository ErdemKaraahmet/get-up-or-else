package com.getuporelse.domain.models

data class AlarmSettings(
    val hour: Int = 7,
    val minute: Int = 0,
    val targetReps: Int = 10,
    val isEnabled: Boolean = false
)
