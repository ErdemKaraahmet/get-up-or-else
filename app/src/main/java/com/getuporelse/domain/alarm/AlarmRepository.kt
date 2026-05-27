package com.getuporelse.domain.alarm

import com.getuporelse.domain.models.AlarmSettings
import kotlinx.coroutines.flow.Flow

interface AlarmRepository {
    fun getAlarmSettings(): Flow<AlarmSettings>
    suspend fun updateAlarmSettings(settings: AlarmSettings)
}
