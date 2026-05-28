package com.getuporelse.domain.pose

import com.getuporelse.core.constants.PoseConstants
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * Domain-level representation of a pose estimation result.
 * Decoupled from MediaPipe — all consumers use this.
 */
data class PoseResult(
    val landmarks: List<PoseLandmark>,
    val timestamp: Long
) {

    /**
     * Returns the landmark at the given index, or null if the index is out of bounds.
     */
    fun landmarkAt(index: Int): PoseLandmark? =
        landmarks.getOrNull(index)

    /**
     * Checks that all 6 upper-body arm landmarks (shoulders, elbows, wrists)
     * are present and have sufficient visibility confidence.
     */
    fun areBothArmsVisible(): Boolean {
        val requiredIndices = listOf(
            PoseConstants.LEFT_SHOULDER, PoseConstants.RIGHT_SHOULDER,
            PoseConstants.LEFT_ELBOW, PoseConstants.RIGHT_ELBOW,
            PoseConstants.LEFT_WRIST, PoseConstants.RIGHT_WRIST
        )
        return requiredIndices.all { index ->
            val landmark = landmarkAt(index) ?: return false
            landmark.visibility >= PoseConstants.MIN_LANDMARK_VISIBILITY
        }
    }

    /**
     * Calculates the inter-shoulder distance for normalization.
     * Returns null if either shoulder is not visible.
     */
    fun interShoulderDistance(): Float? {
        val left = landmarkAt(PoseConstants.LEFT_SHOULDER) ?: return null
        val right = landmarkAt(PoseConstants.RIGHT_SHOULDER) ?: return null
        val dx = left.x - right.x
        val dy = left.y - right.y
        return sqrt(dx * dx + dy * dy)
    }

    companion object {
        /** Empty result for initialization */
        val EMPTY = PoseResult(emptyList(), 0L)

        /**
         * Calculates the angle at point [b] formed by the vectors b→a and b→c.
         * Returns the angle in degrees [0, 180].
         */
        fun calculateAngle(a: PoseLandmark, b: PoseLandmark, c: PoseLandmark): Double {
            val radians = atan2(
                (c.y - b.y).toDouble(), (c.x - b.x).toDouble()
            ) - atan2(
                (a.y - b.y).toDouble(), (a.x - b.x).toDouble()
            )
            var degrees = Math.toDegrees(radians)
            degrees = kotlin.math.abs(degrees)
            if (degrees > 180.0) {
                degrees = 360.0 - degrees
            }
            return degrees
        }
    }
}

data class PoseLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
    val presence: Float,
    val visibility: Float
)
