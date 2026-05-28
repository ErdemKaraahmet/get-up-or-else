package com.getuporelse.data.mediapipe

import com.getuporelse.core.constants.PoseConstants
import com.getuporelse.domain.pose.PoseLandmark

/**
 * Exponential Moving Average (EMA) filter for pose landmarks.
 * Smooths jittery keypoint coordinates caused by depth ambiguity and hand-forearm overlapping.
 *
 * EMA formula: smoothed = alpha * raw + (1 - alpha) * previous_smoothed
 *
 * Thread-safe: called from the MediaPipe callback thread.
 */
class LandmarkSmoother(
    private val alpha: Float = PoseConstants.EMA_ALPHA
) {

    private val smoothedLandmarks = mutableMapOf<Int, PoseLandmark>()

    /**
     * Apply EMA smoothing to a raw landmark at the given [index].
     * On the first call for a given index, the raw value is used directly.
     */
    @Synchronized
    fun smooth(index: Int, raw: PoseLandmark): PoseLandmark {
        val previous = smoothedLandmarks[index]
        val smoothed = if (previous == null) {
            // First frame — no previous data to smooth against
            raw
        } else {
            PoseLandmark(
                x = alpha * raw.x + (1f - alpha) * previous.x,
                y = alpha * raw.y + (1f - alpha) * previous.y,
                z = alpha * raw.z + (1f - alpha) * previous.z,
                presence = raw.presence,
                visibility = raw.visibility
            )
        }
        smoothedLandmarks[index] = smoothed
        return smoothed
    }

    /**
     * Smooth an entire list of landmarks and return the smoothed list.
     */
    @Synchronized
    fun smoothAll(rawLandmarks: List<PoseLandmark>): List<PoseLandmark> {
        return rawLandmarks.mapIndexed { index, landmark ->
            smooth(index, landmark)
        }
    }

    /**
     * Reset all smoothed state (e.g. when the pose is lost).
     */
    @Synchronized
    fun reset() {
        smoothedLandmarks.clear()
    }
}
