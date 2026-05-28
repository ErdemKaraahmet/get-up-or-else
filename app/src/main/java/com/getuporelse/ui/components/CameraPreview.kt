package com.getuporelse.ui.components

import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.getuporelse.core.constants.ExerciseUiConstants
import com.getuporelse.domain.pose.PoseAnalyzer
import java.util.concurrent.Executors

/**
 * CameraX front camera preview composable with optional pose analysis.
 */
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    poseAnalyzer: PoseAnalyzer? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Initialize PreviewView only once
    val previewView = remember {
        PreviewView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            scaleType = PreviewView.ScaleType.FILL_CENTER
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
        }
    }

    val analysisExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(poseAnalyzer, lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()

                provider.unbindAll()

                if (poseAnalyzer != null) {
                    val imageAnalysis = ImageAnalysis.Builder()
                        // Removing explicit resolution to let CameraX choose the best supported combination
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .build()
                        .also { analysis ->
                            analysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                poseAnalyzer.analyzeFrame(
                                    imageProxy,
                                    imageProxy.imageInfo.rotationDegrees
                                )
                            }
                        }

                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview,
                        imageAnalysis
                    )
                } else {
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                }
            } catch (e: Exception) {
                Log.e("CameraPreview", "Camera binding failed", e)
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            try {
                val provider = cameraProviderFuture.get()
                provider.unbindAll()
            } catch (e: Exception) {
                Log.e("CameraPreview", "Failed to unbind camera", e)
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            analysisExecutor.shutdown()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(ExerciseUiConstants.CAMERA_PREVIEW_CORNER_RADIUS_DP.dp))
    )
}
