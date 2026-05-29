package com.getuporelse.domain.exercise

/**
 * Shared contract for push-up detection engines.
 * Both the Kotlin and C++ implementations conform to this interface.
 * Input: flat FloatArray of 33 landmarks × 5 params (x, y, z, presence, visibility).
 * Output: packed Int — lower 16 bits = repCount, bits 16-17 = phase.
 */
interface IPushUpEngine {
    fun processFrame(landmarks: FloatArray): Int
    fun getRepCount(): Int
    fun reset()
    fun destroy() {}

    companion object {
        const val LANDMARKS_COUNT = 33
        const val PARAMS_PER_LANDMARK = 5
        const val TOTAL_FLOAT_COUNT = LANDMARKS_COUNT * PARAMS_PER_LANDMARK

        fun unpackRepCount(packed: Int): Int = packed and 0xFFFF
        fun unpackPhase(packed: Int): Int = (packed shr 16) and 0x3
    }
}
