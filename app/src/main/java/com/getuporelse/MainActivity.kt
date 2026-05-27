package com.getuporelse

import android.app.KeyguardManager
import android.content.Context
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.getuporelse.presentation.screens.AlarmRingingScreen
import com.getuporelse.presentation.screens.AlarmSetupScreen
import com.getuporelse.presentation.viewmodels.AlarmViewModel
import com.getuporelse.ui.theme.GetUpOrElseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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
                    
                    val isRinging = intent.getBooleanExtra("is_ringing", false)
                    if (isRinging) {
                        viewModel.setRinging(true)
                    }

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        if (uiState.isRinging) {
                            AlarmRingingScreen(
                                viewModel = viewModel,
                                showDebugActions = BuildConfig.DEBUG,
                                onTriggerAlarm = viewModel::triggerDebugAlarm,
                                onStopAlarm = viewModel::stopDebugAlarm
                            )
                        } else {
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
