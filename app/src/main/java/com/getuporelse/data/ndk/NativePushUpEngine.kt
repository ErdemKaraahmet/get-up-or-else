package com.getuporelse.data.ndk

import com.getuporelse.domain.exercise.IPushUpEngine
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * JNI wrapper for the native C++ push-up detection engine.
 * Manages a native pointer to a heap-allocated ExerciseEngine instance.
 * Uses a Direct ByteBuffer to avoid JNI array copying overhead.
 */
class NativePushUpEngine : IPushUpEngine {

    private var nativePtr: Long = 0L
    private val directBuffer: ByteBuffer
    private val floatBuffer: FloatBuffer

    init {
        nativePtr = nativeCreate()
        directBuffer = ByteBuffer.allocateDirect(IPushUpEngine.TOTAL_FLOAT_COUNT * 4)
            .order(ByteOrder.nativeOrder())
        floatBuffer = directBuffer.asFloatBuffer()
    }

    override fun processFrame(landmarks: FloatArray): Int {
        check(nativePtr != 0L) { "NativePushUpEngine has been destroyed" }
        floatBuffer.clear()
        floatBuffer.put(landmarks)
        return nativeProcessFrame(nativePtr, directBuffer)
    }

    override fun getRepCount(): Int {
        check(nativePtr != 0L) { "NativePushUpEngine has been destroyed" }
        return nativeGetRepCount(nativePtr)
    }

    override fun reset() {
        check(nativePtr != 0L) { "NativePushUpEngine has been destroyed" }
        nativeReset(nativePtr)
    }

    override fun destroy() {
        if (nativePtr != 0L) {
            nativeDestroy(nativePtr)
            nativePtr = 0L
        }
    }

    // Prevent leaks
    protected fun finalize() {
        destroy()
    }

    // --- JNI externals ---
    private external fun nativeCreate(): Long
    private external fun nativeProcessFrame(ptr: Long, buffer: ByteBuffer): Int
    private external fun nativeGetRepCount(ptr: Long): Int
    private external fun nativeReset(ptr: Long)
    private external fun nativeDestroy(ptr: Long)

    companion object {
        init {
            System.loadLibrary("native-exercise-engine")
        }
    }
}
