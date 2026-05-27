package com.getuporelse.data.local

import android.content.Context
import android.content.Intent
import android.os.Build
import com.getuporelse.BuildConfig
import com.getuporelse.domain.alarm.DebugAlarmController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidDebugAlarmController @Inject constructor(
    @ApplicationContext private val context: Context
) : DebugAlarmController {

    override fun triggerAlarm() {
        if (!BuildConfig.DEBUG) return

        val serviceIntent = Intent(context, AlarmForegroundService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }

    override fun stopAlarm() {
        if (!BuildConfig.DEBUG) return

        context.stopService(Intent(context, AlarmForegroundService::class.java))
    }
}
