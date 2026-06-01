package com.getuporelse.domain.exercise

import android.util.Log
import com.getuporelse.data.ndk.NativePushUpEngine
import com.getuporelse.domain.pose.PoseResult

// Set to true for C++ Native engine, false for Kotlin engine
private const val USE_NATIVE_ENGINE = true 

/**
 * Benchmarking and dynamic routing harness that runs either the Kotlin or C++ native engine
 * based on the USE_NATIVE_ENGINE code constant.
 * Logs performance in microseconds and tracks renderer FPS in a separate log.
 */
class DualEngineExerciseDetector : ExerciseDetector {

    private val kotlinEngine = PushUpDetector()
    private val nativeEngine = NativePushUpEngine()

    private var lastFrameTimeNs = 0L
    private var isFirstFrame = true

    override fun processPose(result: PoseResult): ExerciseState {
        if (isFirstFrame) {
            val engineType = if (USE_NATIVE_ENGINE) "Native" else "Kotlin"
            Log.d(TAG, "perf log of $engineType")
            Log.d(TAG_FPS, "fps log of $engineType")
            isFirstFrame = false
        }

        // Calculate and log renderer FPS under a separate tag RENDERER_FPS
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

        return if (USE_NATIVE_ENGINE) {
            // --- Native engine execution & timing ---
            val natStart = System.nanoTime()
            val nativeResult = nativeEngine.processFrame(flat)
            val natEnd = System.nanoTime()

            val natDurationUs = (natEnd - natStart) / 1000
            Log.d(TAG, "$natDurationUs")

            val nativeReps = IPushUpEngine.unpackRepCount(nativeResult)
            val nativePhase = IPushUpEngine.unpackPhase(nativeResult)

            val feedback = when (nativePhase) {
                0 -> "Get into position" // WAITING_FOR_TOP
                1 -> "Lower your body"   // TOP
                2 -> "Push up!"          // BOTTOM
                else -> ""
            }

            val nativeAngle = IPushUpEngine.unpackAngle(nativeResult)

            ExerciseState(
                repCount = nativeReps,
                phase = ExercisePhase.ACTIVE,
                feedback = feedback,
                debugAngle = nativeAngle
            )
        } else {
            // --- Kotlin engine execution & timing ---
            val ktStart = System.nanoTime()
            val exerciseState = kotlinEngine.processPose(result)
            val ktEnd = System.nanoTime()

            val ktDurationUs = (ktEnd - ktStart) / 1000
            Log.d(TAG, "$ktDurationUs")

            exerciseState
        }
    }

    fun reset() {
        kotlinEngine.reset()
        nativeEngine.reset()
        isFirstFrame = true
    }

    fun destroy() {
        nativeEngine.destroy()
    }

    companion object {
        private const val TAG = "PERF_BENCHMARK"
        private const val TAG_FPS = "RENDERER_FPS"
        private const val TAG_PARITY = "PARITY_CHECK"
    }
}
