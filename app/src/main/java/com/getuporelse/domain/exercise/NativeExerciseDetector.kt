package com.getuporelse.domain.exercise

import android.util.Log
import com.getuporelse.data.ndk.NativePushUpEngine
import com.getuporelse.domain.pose.PoseResult

/**
 * Native C++ exercise detector.
 * Logs end-to-end FPS under the RENDERER_FPS logcat tag for benchmarking.
 */
class NativeExerciseDetector : ExerciseDetector {

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

        // Forward pre-serialized landmarks directly to NDK, zero heap allocations
        val nativeResult = nativeEngine.processFrame(result.flatLandmarks)

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
