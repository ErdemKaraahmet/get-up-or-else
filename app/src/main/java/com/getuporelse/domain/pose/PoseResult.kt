package com.getuporelse.domain.pose

/**
 * Represent the result of a pose estimation.
 * This should be a domain-level representation, decoupled from MediaPipe.
 */
data class PoseResult(
    val landmarks: List<PoseLandmark>,
    val timestamp: Long
)

data class PoseLandmark(
    val x: Float,
    val y: Float,
    val z: Float,
    val presence: Float,
    val visibility: Float
)
