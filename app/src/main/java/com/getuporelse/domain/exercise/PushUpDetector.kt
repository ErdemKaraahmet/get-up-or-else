package com.getuporelse.domain.exercise

import com.getuporelse.core.constants.PoseConstants
import com.getuporelse.domain.pose.PoseLandmark
import com.getuporelse.domain.pose.PoseResult

/**
 * Push-up detection state machine.
 *
 * Valid rep = full TOP → BOTTOM → TOP cycle where:
 * - Both arms are visible (anti-cheat)
 * - Elbow angle transitions from extended (>160°) to flexed (<100°) and back
 * - Sufficient normalized vertical shoulder-to-wrist displacement at bottom
 * - Minimum angle delta to reject micro-movements
 *
 * All coordinates are normalized against inter-shoulder distance for scale invariance.
 */
class PushUpDetector : ExerciseDetector {

    private var repCount = 0
    private var phase = Phase.WAITING_FOR_TOP
    private var lastAverageAngle = 0.0

    /**
     * Internal state machine phases.
     * WAITING_FOR_TOP: waiting for user to be in the "up" position to start
     * TOP: user is in the up position, waiting for descent
     * BOTTOM: user has descended, waiting for ascent to complete the rep
     */
    private enum class Phase {
        WAITING_FOR_TOP,
        TOP,
        BOTTOM
    }

    override fun processPose(result: PoseResult): ExerciseState {
        // Anti-cheat: reject if either arm is not visible
        if (!result.areBothArmsVisible()) {
            return ExerciseState(
                repCount = repCount,
                phase = ExercisePhase.ACTIVE,
                feedback = "Body not fully visible"
            )
        }

        // Get landmarks
        val leftShoulder = result.landmarkAt(PoseConstants.LEFT_SHOULDER)!!
        val rightShoulder = result.landmarkAt(PoseConstants.RIGHT_SHOULDER)!!
        val leftElbow = result.landmarkAt(PoseConstants.LEFT_ELBOW)!!
        val rightElbow = result.landmarkAt(PoseConstants.RIGHT_ELBOW)!!
        val leftWrist = result.landmarkAt(PoseConstants.LEFT_WRIST)!!
        val rightWrist = result.landmarkAt(PoseConstants.RIGHT_WRIST)!!

        // Calculate elbow angles (Shoulder-Elbow-Wrist angle)
        val leftAngle = PoseResult.calculateAngle(leftShoulder, leftElbow, leftWrist)
        val rightAngle = PoseResult.calculateAngle(rightShoulder, rightElbow, rightWrist)
        val averageAngle = (leftAngle + rightAngle) / 2.0

        // Normalization: inter-shoulder distance for scale invariance
        val interShoulderDist = result.interShoulderDistance() ?: return ExerciseState(
            repCount = repCount,
            phase = ExercisePhase.ACTIVE,
            feedback = "Move closer to camera"
        )

        // Anti-cheat: reject micro-movements
        val angleDelta = kotlin.math.abs(averageAngle - lastAverageAngle)
        lastAverageAngle = averageAngle

        // Calculate normalized vertical displacement (shoulder Y vs wrist Y)
        val avgShoulderY = (leftShoulder.y + rightShoulder.y) / 2f
        val avgWristY = (leftWrist.y + rightWrist.y) / 2f
        val normalizedYDisplacement = if (interShoulderDist > 0f) {
            kotlin.math.abs(avgShoulderY - avgWristY) / interShoulderDist
        } else {
            0f
        }

        // State machine
        return when (phase) {
            Phase.WAITING_FOR_TOP -> {
                if (averageAngle > PoseConstants.ELBOW_EXTENSION_THRESHOLD_DEGREES) {
                    phase = Phase.TOP
                    ExerciseState(
                        repCount = repCount,
                        phase = ExercisePhase.ACTIVE,
                        feedback = "Ready — go down",
                        debugAngle = averageAngle
                    )
                } else {
                    ExerciseState(
                        repCount = repCount,
                        phase = ExercisePhase.ACTIVE,
                        feedback = "Extend your arms fully",
                        debugAngle = averageAngle
                    )
                }
            }

            Phase.TOP -> {
                if (averageAngle < PoseConstants.ELBOW_FLEXION_THRESHOLD_DEGREES
                    && normalizedYDisplacement >= PoseConstants.MIN_NORMALIZED_Y_DISPLACEMENT
                    && angleDelta >= PoseConstants.MIN_ANGLE_DELTA_DEGREES
                ) {
                    phase = Phase.BOTTOM
                    ExerciseState(
                        repCount = repCount,
                        phase = ExercisePhase.ACTIVE,
                        feedback = "Good — now push up!",
                        debugAngle = averageAngle
                    )
                } else {
                    ExerciseState(
                        repCount = repCount,
                        phase = ExercisePhase.ACTIVE,
                        feedback = "Lower more",
                        debugAngle = averageAngle
                    )
                }
            }

            Phase.BOTTOM -> {
                if (averageAngle > PoseConstants.ELBOW_EXTENSION_THRESHOLD_DEGREES) {
                    // Full cycle complete: TOP → BOTTOM → TOP
                    repCount++
                    phase = Phase.TOP
                    ExerciseState(
                        repCount = repCount,
                        phase = ExercisePhase.ACTIVE,
                        feedback = "Good rep!",
                        debugAngle = averageAngle
                    )
                } else {
                    ExerciseState(
                        repCount = repCount,
                        phase = ExercisePhase.ACTIVE,
                        feedback = "Push up!",
                        debugAngle = averageAngle
                    )
                }
            }
        }
    }

    fun getRepCount() = repCount

    fun reset() {
        repCount = 0
        phase = Phase.WAITING_FOR_TOP
        lastAverageAngle = 0.0
    }
}
