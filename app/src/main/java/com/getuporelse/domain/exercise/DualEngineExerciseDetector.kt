package com.getuporelse.domain.exercise

import android.util.Log
import com.getuporelse.data.ndk.NativePushUpEngine
import com.getuporelse.domain.pose.PoseResult

/**
 * Native C++ exercise detector.
 * Logs end-to-end FPS under the RENDERER_FPS logcat tag for benchmarking.
 */
class DualEngineExerciseDetector : ExerciseDetector {

    private val nativeEngine = NativePushUpEngine()

    private var lastFrameTimeNs = 0L
    private var isFirstFrame = true

    override fun processPose(result: PoseResult): ExerciseState {
        if (isFirstFrame) {
            Log.d(TAG_FPS, "fps log")
            isFirstFrame = false
        }

        // Log end-to-end FPS
        val currentFrameTimeNs = System.nanoTime()
        if (lastFrameTimeNs != 0L) {
            val frameIntervalNs = currentFrameTimeNs - lastFrameTimeNs
            if (frameIntervalNs > 0) {
                val fps = 1_000_000_000.0 / frameIntervalNs
                Log.d(TAG_FPS, String.format("%.2f", fps))
            }
        }
        lastFrameTimeNs = currentFrameTimeNs

        // Serialize landmarks to flat FloatArray
        val flat = FloatArray(IPushUpEngine.TOTAL_FLOAT_COUNT)
        result.landmarks.forEachIndexed { i, lm ->
            val base = i * IPushUpEngine.PARAMS_PER_LANDMARK
            flat[base] = lm.x
            flat[base + 1] = lm.y
            flat[base + 2] = lm.z
            flat[base + 3] = lm.presence
            flat[base + 4] = lm.visibility
        }

        val nativeResult = nativeEngine.processFrame(flat)

        val nativeReps = IPushUpEngine.unpackRepCount(nativeResult)
        val nativePhase = IPushUpEngine.unpackPhase(nativeResult)

        val feedback = when (nativePhase) {
            0 -> "Get into position" // WAITING_FOR_TOP
            1 -> "Lower your body"   // TOP
            2 -> "Push up!"          // BOTTOM
            else -> ""
        }

        val nativeAngle = IPushUpEngine.unpackAngle(nativeResult)

        return ExerciseState(
            repCount = nativeReps,
            phase = ExercisePhase.ACTIVE,
            feedback = feedback,
            debugAngle = nativeAngle
        )
    }

    fun reset() {
        nativeEngine.reset()
        isFirstFrame = true
    }

    fun destroy() {
        nativeEngine.destroy()
    }

    companion object {
        private const val TAG_FPS = "RENDERER_FPS"
    }
}
