package com.getuporelse.data.mediapipe

import android.util.Log
import com.google.mediapipe.tasks.core.Delegate
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.getuporelse.core.constants.PoseConstants
import com.getuporelse.domain.pose.PoseAnalyzer
import com.getuporelse.domain.pose.PoseLandmark
import com.getuporelse.domain.pose.PoseResult
import com.getuporelse.domain.exercise.IPushUpEngine
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MediaPipe-backed implementation of [PoseAnalyzer].
 *
 * - Runs in LIVE_STREAM mode for real-time camera analysis
 * - Applies EMA smoothing to raw landmarks via [LandmarkSmoother]
 * - Maps MediaPipe results to domain-level [PoseResult]
 * - All MediaPipe API access is encapsulated here per CLAUDE.md rules
 */
@Singleton
class MediaPipePoseAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) : PoseAnalyzer {

    private var resultListener: ((PoseResult) -> Unit)? = null
    private var errorListener: ((Exception) -> Unit)? = null

    // Pre-allocated cache structures for zero-allocation performance, reuse to prevent GC thrashing
    private val landmarkCache = List(IPushUpEngine.LANDMARKS_COUNT) { PoseLandmark(0f, 0f, 0f, 0f, 0f) }
    private val flatLandmarkCache = FloatArray(IPushUpEngine.TOTAL_FLOAT_COUNT)


    private var poseLandmarker: PoseLandmarker? = null
    private var currentUseGpu: Boolean = false
    private var isInitialized: Boolean = false

    @Synchronized
    private fun getOrCreateLandmarker(): PoseLandmarker? {
        if (isInitialized) return poseLandmarker
        isInitialized = true
        
        poseLandmarker = try {
            if (currentUseGpu) {
                try {
                    createLandmarker(Delegate.GPU)
                } catch (e: Exception) {
                    Log.w("MediaPipePoseAnalyzer", "GPU delegate failed, falling back to CPU", e)
                    createLandmarker()
                }
            } else {
                createLandmarker()
            }
        } catch (e: Exception) {
            errorListener?.invoke(e)
            null
        }
        return poseLandmarker
    }
    private fun createLandmarker(delegate: Delegate? = null): PoseLandmarker {
        val baseOptionsBuilder = BaseOptions.builder()
            .setModelAssetPath(PoseConstants.POSE_MODEL_ASSET_PATH)
        if (delegate != null) {
            baseOptionsBuilder.setDelegate(delegate)
        }
        val baseOptions = baseOptionsBuilder.build()

        val options = PoseLandmarker.PoseLandmarkerOptions.builder()
            .setBaseOptions(baseOptions)
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setNumPoses(1)
            .setMinPoseDetectionConfidence(PoseConstants.MIN_LANDMARK_VISIBILITY)
            .setMinTrackingConfidence(PoseConstants.MIN_LANDMARK_VISIBILITY)
            .setResultListener { result, _ ->
                handleResult(result)
            }
            .setErrorListener { error ->
                errorListener?.invoke(
                    Exception("MediaPipe pose analysis error: ${error.message}", error)
                )
            }
            .build()

        return PoseLandmarker.createFromOptions(context, options)
    }

    override fun analyzeFrame(imageProxy: ImageProxy, rotationDegrees: Int) {
        val landmarker = getOrCreateLandmarker()
        if (landmarker == null) {
            imageProxy.close()
            return
        }

        try {
            val bitmap = imageProxyToBitmap(imageProxy, rotationDegrees)
            if (bitmap != null) {
                val mpImage = BitmapImageBuilder(bitmap).build()
                val timestampMs = imageProxy.imageInfo.timestamp / 1_000_000 // Convert ns to ms
                landmarker.detectAsync(mpImage, timestampMs)
            }
        } catch (e: Exception) {
            errorListener?.invoke(e)
        } finally {
            imageProxy.close()
        }
    }

    /**
     * Converts an ImageProxy to a Bitmap, applying the required rotation.
     * Returns null if conversion fails.
     */
    @Suppress("UnsafeOptInUsageError")
    private fun imageProxyToBitmap(imageProxy: ImageProxy, rotationDegrees: Int): Bitmap? {
        val bitmap = imageProxy.toBitmap()
        return if (rotationDegrees != 0) {
            val matrix = Matrix().apply {
                postRotate(rotationDegrees.toFloat())
            }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } else {
            bitmap
        }
    }

    override fun setResultListener(listener: (PoseResult) -> Unit) {
        resultListener = listener
    }

    @Synchronized
    override fun updateGpuSetting(useGpu: Boolean) {
        if (currentUseGpu != useGpu) {
            currentUseGpu = useGpu
            poseLandmarker?.close()
            poseLandmarker = null
            isInitialized = false
        }
    }

    override fun setErrorListener(listener: (Exception) -> Unit) {
        errorListener = listener
    }

    @Synchronized
    override fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
        isInitialized = false
    }

    private fun handleResult(result: PoseLandmarkerResult) {
        val landmarks = result.landmarks()
        if (landmarks.isEmpty() || landmarks[0].isEmpty()) {
            resultListener?.invoke(PoseResult.EMPTY)
            return
        }

        // Write directly to the pre-allocated cache arrays
        val mpList = landmarks[0]
        for (i in 0 until IPushUpEngine.LANDMARKS_COUNT) {
            val lm = mpList.getOrNull(i)
            val cacheLm = landmarkCache[i]
            if (lm != null) {
                cacheLm.x = lm.x()
                cacheLm.y = lm.y()
                cacheLm.z = lm.z()
                cacheLm.presence = lm.presence().orElse(0f)
                cacheLm.visibility = lm.visibility().orElse(0f)
            } else {
                cacheLm.x = 0f
                cacheLm.y = 0f
                cacheLm.z = 0f
                cacheLm.presence = 0f
                cacheLm.visibility = 0f
            }

            val base = i * 5
            flatLandmarkCache[base] = cacheLm.x
            flatLandmarkCache[base + 1] = cacheLm.y
            flatLandmarkCache[base + 2] = cacheLm.z
            flatLandmarkCache[base + 3] = cacheLm.presence
            flatLandmarkCache[base + 4] = cacheLm.visibility
        }

        resultListener?.invoke(
            PoseResult(
                landmarks = landmarkCache,
                flatLandmarks = flatLandmarkCache,
                timestamp = result.timestampMs()
            )
        )
    }
}
