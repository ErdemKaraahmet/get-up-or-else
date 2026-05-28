package com.getuporelse.domain.pose

import androidx.camera.core.ImageProxy

/**
 * Interface for pose analysis.
 * Implementations wrap the actual ML framework (e.g. MediaPipe).
 * UI and ViewModels interact only through this interface.
 */
interface PoseAnalyzer {
    /**
     * Submit a camera frame for asynchronous pose analysis.
     * The implementation must close the [imageProxy] after processing.
     */
    fun analyzeFrame(imageProxy: ImageProxy, rotationDegrees: Int)

    /**
     * Register a listener for pose analysis results.
     * Called on a background thread — consumers must dispatch to Main if needed.
     */
    fun setResultListener(listener: (PoseResult) -> Unit)

    /**
     * Register a listener for analysis errors.
     */
    fun setErrorListener(listener: (Exception) -> Unit)

    /**
     * Release all resources held by the analyzer.
     */
    fun close()
}
