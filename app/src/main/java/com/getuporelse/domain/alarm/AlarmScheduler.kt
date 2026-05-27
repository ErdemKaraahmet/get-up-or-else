package com.getuporelse.domain.alarm

import com.getuporelse.domain.models.AlarmSettings

interface AlarmScheduler {
    fun schedule(settings: AlarmSettings)
    fun cancel()
}
