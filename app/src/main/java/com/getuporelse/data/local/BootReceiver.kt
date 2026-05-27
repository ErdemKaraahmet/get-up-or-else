package com.getuporelse.data.local

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.getuporelse.domain.alarm.AlarmRepository
import com.getuporelse.domain.alarm.AlarmScheduler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var alarmRepository: AlarmRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val settings = alarmRepository.getAlarmSettings().first()
                if (settings.isEnabled) {
                    alarmScheduler.schedule(settings)
                }
            }
        }
    }
}
