package com.getuporelse.data.mediapipe

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageProxy
import com.getuporelse.core.constants.PoseConstants
import com.getuporelse.domain.pose.PoseAnalyzer
import com.getuporelse.domain.pose.PoseLandmark
import com.getuporelse.domain.pose.PoseResult
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
    private val smoother = LandmarkSmoother()

    private val poseLandmarker: PoseLandmarker? by lazy {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath(PoseConstants.POSE_MODEL_ASSET_PATH)
                .build()

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

            PoseLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            errorListener?.invoke(e)
            null
        }
    }

    override fun analyzeFrame(imageProxy: ImageProxy, rotationDegrees: Int) {
        val landmarker = poseLandmarker
        if (landmarker == null) {
            imageProxy.close()
            return
        }

        try {
            val bitmap = imageProxyToBitmap(imageProxy, rotationDegrees)
            if (bitmap != null) {
                val mpImage = BitmapImageBuilder(bitmap).build()
                val timestampMs = imageProxy.imageInfo.timestamp / 1000 // Convert to ms
                landmarker.detectAsync(mpImage, timestampMs)
            }
        } catch (e: Exception) {
            errorListener?.invoke(e)
        } finally {
            imageProxy.close()
        }
    }

    override fun setResultListener(listener: (PoseResult) -> Unit) {
        resultListener = listener
    }

    override fun setErrorListener(listener: (Exception) -> Unit) {
        errorListener = listener
    }

    override fun close() {
        poseLandmarker?.close()
        smoother.reset()
    }

    private fun handleResult(result: PoseLandmarkerResult) {
        val landmarks = result.landmarks()
        if (landmarks.isEmpty() || landmarks[0].isEmpty()) {
            smoother.reset()
            resultListener?.invoke(PoseResult.EMPTY)
            return
        }

        // Map MediaPipe landmarks to domain model
        val rawLandmarks = landmarks[0].map { landmark ->
            PoseLandmark(
                x = landmark.x(),
                y = landmark.y(),
                z = landmark.z(),
                presence = landmark.presence().orElse(0f),
                visibility = landmark.visibility().orElse(0f)
            )
        }

        // Apply EMA smoothing
        val smoothedLandmarks = smoother.smoothAll(rawLandmarks)

        val poseResult = PoseResult(
            landmarks = smoothedLandmarks,
            timestamp = result.timestampMs()
        )

        resultListener?.invoke(poseResult)
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
}
