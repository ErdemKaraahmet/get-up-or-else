package com.getuporelse.domain.exercise

import com.getuporelse.domain.pose.PoseResult

/**
 * Every exercise type must implement this interface.
 * Each detector owns its own thresholds, constants, and state machine.
 *
 * Current: PushUpDetector
 * Future (do not implement yet): SquatDetector, JumpingJackDetector, BurpeeDetector
 */
interface ExerciseDetector {
    fun processPose(result: PoseResult): ExerciseState
}

/**
 * The phase of the exercise session.
 */
enum class ExercisePhase {
    IDLE,
    ACTIVE,
    FINISHED
}

/**
 * Result of processing a single pose frame through the exercise detector.
 */
data class ExerciseState(
    val repCount: Int = 0,
    val phase: ExercisePhase = ExercisePhase.IDLE,
    val feedback: String = "",
    val debugAngle: Double = 0.0
)
