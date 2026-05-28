package com.getuporelse.core.constants

/**
 * Constants for pose estimation and push-up detection.
 * All thresholds and landmark indices are declared here — no magic numbers elsewhere.
 */
object PoseConstants {

    // --- MediaPipe landmark indices (BlazePose 33-point model) ---
    const val LEFT_SHOULDER = 11
    const val RIGHT_SHOULDER = 12
    const val LEFT_ELBOW = 13
    const val RIGHT_ELBOW = 14
    const val LEFT_WRIST = 15
    const val RIGHT_WRIST = 16

    // --- Landmark confidence ---
    const val MIN_LANDMARK_VISIBILITY = 0.5f

    // --- Push-up state machine thresholds ---
    /** Elbow angle above this = arms extended (top position) */
    const val ELBOW_EXTENSION_THRESHOLD_DEGREES = 160.0

    /** Elbow angle below this = arms flexed (bottom position) */
    const val ELBOW_FLEXION_THRESHOLD_DEGREES = 100.0

    /**
     * Minimum normalized vertical displacement of shoulders relative to wrists.
     * Normalized against inter-shoulder distance to be scale-invariant.
     */
    const val MIN_NORMALIZED_Y_DISPLACEMENT = 0.3

    // --- Signal smoothing (Exponential Moving Average) ---
    /** EMA alpha: higher = less smoothing, lower = more smoothing. Range (0, 1]. */
    const val EMA_ALPHA = 0.4f

    // --- Anti-cheat ---
    /** Minimum elbow angle change (degrees) to consider as real movement */
    const val MIN_ANGLE_DELTA_DEGREES = 15.0

    // --- MediaPipe model ---
    const val POSE_MODEL_ASSET_PATH = "pose_landmarker_lite.task"

    // --- Analysis frame resolution ---
    const val ANALYSIS_TARGET_WIDTH = 640
    const val ANALYSIS_TARGET_HEIGHT = 480
}
