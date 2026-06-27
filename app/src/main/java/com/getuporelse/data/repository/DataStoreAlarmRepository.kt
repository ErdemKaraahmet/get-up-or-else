package com.getuporelse.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.getuporelse.core.constants.Constants
import com.getuporelse.domain.alarm.AlarmRepository
import com.getuporelse.domain.models.Alarm
import com.getuporelse.domain.models.AlarmSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.ALARM_DATASTORE_NAME)

@Singleton
class DataStoreAlarmRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : AlarmRepository {

    private object PreferencesKeys {
        val HOUR = intPreferencesKey(Constants.KEY_ALARM_HOUR)
        val MINUTE = intPreferencesKey(Constants.KEY_ALARM_MINUTE)
        val TARGET_REPS = intPreferencesKey(Constants.KEY_TARGET_REPS)
        val ENABLED = booleanPreferencesKey(Constants.KEY_ALARM_ENABLED)
        val USE_GPU = booleanPreferencesKey(Constants.KEY_USE_GPU)
        val ALARMS_LIST = stringPreferencesKey("alarms_list")
    }

    override fun getAlarmSettings(): Flow<AlarmSettings> {
        return context.dataStore.data.map { preferences ->
            val alarms = if (preferences.contains(PreferencesKeys.ALARMS_LIST)) {
                val alarmsListStr = preferences[PreferencesKeys.ALARMS_LIST]
                deserializeAlarms(alarmsListStr)
            } else {
                val oldHour = preferences[PreferencesKeys.HOUR] ?: 7
                val oldMinute = preferences[PreferencesKeys.MINUTE] ?: 0
                val oldEnabled = preferences[PreferencesKeys.ENABLED] ?: false
                listOf(Alarm(id = 1, hour = oldHour, minute = oldMinute, isEnabled = oldEnabled))
            }
            
            AlarmSettings(
                alarms = alarms,
                targetReps = preferences[PreferencesKeys.TARGET_REPS] ?: 10,
                useGpu = preferences[PreferencesKeys.USE_GPU] ?: false
            )
        }
    }

    override suspend fun updateAlarmSettings(settings: AlarmSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ALARMS_LIST] = serializeAlarms(settings.alarms)
            preferences[PreferencesKeys.TARGET_REPS] = settings.targetReps
            preferences[PreferencesKeys.USE_GPU] = settings.useGpu
        }
    }

    private fun serializeAlarms(alarms: List<Alarm>): String {
        return alarms.joinToString("|") { "${it.id}:${it.hour}:${it.minute}:${it.isEnabled}" }
    }

    private fun deserializeAlarms(serialized: String?): List<Alarm> {
        if (serialized.isNullOrEmpty()) {
            return emptyList()
        }
        return try {
            serialized.split("|").map { alarmStr ->
                val parts = alarmStr.split(":")
                Alarm(
                    id = parts[0].toInt(),
                    hour = parts[1].toInt(),
                    minute = parts[2].toInt(),
                    isEnabled = parts[3].toBoolean()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
