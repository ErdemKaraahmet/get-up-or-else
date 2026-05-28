package com.getuporelse.data.local

import android.content.Context
import android.content.Intent
import com.getuporelse.domain.alarm.AlarmController
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidAlarmController @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmController {

    override fun stopAlarm() {
        context.stopService(Intent(context, AlarmForegroundService::class.java))
    }
}
