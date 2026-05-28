package com.getuporelse.presentation.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.getuporelse.core.constants.ExerciseUiConstants
import com.getuporelse.presentation.viewmodels.AlarmViewModel
import com.getuporelse.ui.components.CameraPreview

@Composable
fun ExerciseScreen(
    viewModel: AlarmViewModel,
    showDebugActions: Boolean = false,
    onDebugIncrementRep: () -> Unit = {},
    onExerciseComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Trigger completion when target reps are reached
    LaunchedEffect(uiState.isComplete) {
        if (uiState.isComplete) {
            onExerciseComplete()
        }
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (hasCameraPermission) {
            CameraPreview(
                modifier = Modifier.fillMaxSize()
            )

            // Rep counter overlay — top-end
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(ExerciseUiConstants.REP_COUNTER_PADDING_DP.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface.copy(
                            alpha = ExerciseUiConstants.REP_COUNTER_BACKGROUND_ALPHA
                        ),
                        shape = RoundedCornerShape(ExerciseUiConstants.REP_COUNTER_CORNER_RADIUS_DP.dp)
                    )
                    .padding(
                        horizontal = ExerciseUiConstants.REP_COUNTER_PADDING_DP.dp,
                        vertical = ExerciseUiConstants.FEEDBACK_PADDING_VERTICAL_DP.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${uiState.repCount}/${uiState.targetReps}",
                        fontSize = ExerciseUiConstants.REP_COUNTER_FONT_SIZE_SP.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "REPS",
                        fontSize = ExerciseUiConstants.REP_COUNTER_LABEL_FONT_SIZE_SP.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Feedback overlay — bottom center
            if (uiState.feedback.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = ExerciseUiConstants.FEEDBACK_BOTTOM_OFFSET_DP.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(
                                alpha = ExerciseUiConstants.FEEDBACK_BACKGROUND_ALPHA
                            ),
                            shape = RoundedCornerShape(ExerciseUiConstants.FEEDBACK_CORNER_RADIUS_DP.dp)
                        )
                        .padding(
                            horizontal = ExerciseUiConstants.FEEDBACK_PADDING_HORIZONTAL_DP.dp,
                            vertical = ExerciseUiConstants.FEEDBACK_PADDING_VERTICAL_DP.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.feedback,
                        fontSize = ExerciseUiConstants.FEEDBACK_FONT_SIZE_SP.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Debug-only: artificial rep increment button
            if (showDebugActions) {
                Button(
                    onClick = onDebugIncrementRep,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(ExerciseUiConstants.REP_COUNTER_PADDING_DP.dp)
                        .widthIn(min = 120.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(ExerciseUiConstants.FEEDBACK_CORNER_RADIUS_DP.dp)
                ) {
                    Text(
                        text = "DEBUG +1 Rep",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            // Camera permission denied — show rationale
            CameraPermissionRationale(
                onRequestPermission = {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            )
        }
    }
}

@Composable
private fun CameraPermissionRationale(
    onRequestPermission: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(ExerciseUiConstants.REP_COUNTER_PADDING_DP.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Info,
            contentDescription = "Camera permission required",
            modifier = Modifier.size(ExerciseUiConstants.PERMISSION_ICON_SIZE_DP.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(ExerciseUiConstants.PERMISSION_SPACING_DP.dp))

        Text(
            text = "Camera Access Required",
            fontSize = ExerciseUiConstants.PERMISSION_TITLE_FONT_SIZE_SP.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ExerciseUiConstants.PERMISSION_SPACING_DP.dp))

        Text(
            text = "The camera is needed to track your push-ups.\nNo images are stored or sent anywhere.",
            fontSize = ExerciseUiConstants.PERMISSION_BODY_FONT_SIZE_SP.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(ExerciseUiConstants.PERMISSION_SPACING_DP.dp * 2))

        Button(
            onClick = onRequestPermission,
            modifier = Modifier.height(ExerciseUiConstants.PERMISSION_BUTTON_HEIGHT_DP.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(
                text = "Grant Camera Access",
                fontSize = ExerciseUiConstants.PERMISSION_BUTTON_FONT_SIZE_SP.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
