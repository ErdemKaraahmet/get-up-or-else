package com.getuporelse.presentation.viewmodels

data class AlarmUiState(
    val isRinging: Boolean = false,
    val isExercising: Boolean = false,
    val repCount: Int = 0,
    val targetReps: Int = 10,
    val feedback: String = "",
    val isComplete: Boolean = false
)
