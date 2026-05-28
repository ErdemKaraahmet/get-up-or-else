package com.getuporelse

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.getuporelse.core.constants.Constants
import com.getuporelse.presentation.screens.AlarmRingingScreen
import com.getuporelse.presentation.screens.AlarmSetupScreen
import com.getuporelse.presentation.screens.ExerciseScreen
import com.getuporelse.presentation.viewmodels.AlarmViewModel
import com.getuporelse.ui.theme.GetUpOrElseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var ringingIntentRequests by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        trackRingingIntent(intent)
        
        setupLockScreenFlags()
        
        enableEdgeToEdge()
        setContent {
            GetUpOrElseTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: AlarmViewModel = hiltViewModel()
                    val uiState by viewModel.uiState.collectAsState()

                    LaunchedEffect(ringingIntentRequests) {
                        if (ringingIntentRequests > 0 || com.getuporelse.data.local.AlarmForegroundService.isServiceRunning) {
                            viewModel.setRinging(true)
                        }
                    }

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        when {
                            uiState.isExercising -> {
                                ExerciseScreen(
                                    viewModel = viewModel,
                                    showDebugActions = BuildConfig.DEBUG,
                                    onDebugIncrementRep = viewModel::debugIncrementRep,
                                    onExerciseComplete = {
                                        viewModel.completeExercise()
                                    }
                                )
                            }
                            uiState.isRinging -> {
                                AlarmRingingScreen(
                                    viewModel = viewModel,
                                    showDebugActions = BuildConfig.DEBUG,
                                    onTriggerAlarm = viewModel::triggerDebugAlarm,
                                    onStopAlarm = viewModel::stopDebugAlarm
                                )
                            }
                            else -> {
                                AlarmSetupScreen(
                                    viewModel = viewModel,
                                    showDebugActions = BuildConfig.DEBUG,
                                    onTriggerAlarm = viewModel::triggerDebugAlarm,
                                    onStopAlarm = viewModel::stopDebugAlarm
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        trackRingingIntent(intent)
    }

    private fun trackRingingIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(Constants.EXTRA_IS_RINGING, false) == true) {
            ringingIntentRequests += 1
        }
    }

    private fun setupLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}
