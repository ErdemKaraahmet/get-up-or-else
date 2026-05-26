package com.getuporelse.domain.exercise

import com.getuporelse.domain.pose.PoseResult

class PushUpDetector : ExerciseDetector {
    private var repCount = 0
    private var isDown = false

    override fun processPose(result: PoseResult): ExerciseState {
        // TODO: Implement push-up detection logic
        return ExerciseState.IDLE
    }
    
    fun getRepCount() = repCount
}
