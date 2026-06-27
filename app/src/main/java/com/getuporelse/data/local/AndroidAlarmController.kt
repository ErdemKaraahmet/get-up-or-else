package com.getuporelse.data.local

import android.content.Context
import android.content.Intent
import android.os.Build
import com.getuporelse.domain.alarm.AlarmController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAlarmController @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmController {

    override fun triggerAlarm(targetReps: Int) {
        val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
            putExtra("TARGET_REPS", targetReps)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    override fun stopAlarm() {
        context.stopService(Intent(context, AlarmForegroundService::class.java))
    }
}
