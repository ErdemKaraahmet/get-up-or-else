package com.getuporelse.domain.exercise

import com.getuporelse.domain.pose.PoseResult

interface ExerciseDetector {
    fun processPose(result: PoseResult): ExerciseState
}

enum class ExerciseState {
    IDLE,
    ACTIVE,
    REP_COMPLETE,
    FINISHED
}
