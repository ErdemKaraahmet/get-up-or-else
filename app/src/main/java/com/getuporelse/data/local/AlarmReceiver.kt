package com.getuporelse.data.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getIntExtra("ALARM_ID", -1)
        val targetReps = intent.getIntExtra("TARGET_REPS", 10)

        val serviceIntent = Intent(context, AlarmForegroundService::class.java).apply {
            putExtra("ALARM_ID", alarmId)
            putExtra("TARGET_REPS", targetReps)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
    }
}
