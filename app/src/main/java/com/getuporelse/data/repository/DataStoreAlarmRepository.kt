package com.getuporelse.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.getuporelse.core.constants.Constants
import com.getuporelse.domain.alarm.AlarmRepository
import com.getuporelse.domain.models.AlarmSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = Constants.ALARM_DATASTORE_NAME)

@Singleton
class DataStoreAlarmRepository @Inject constructor(
    private val context: Context
) : AlarmRepository {

    private object PreferencesKeys {
        val HOUR = intPreferencesKey(Constants.KEY_ALARM_HOUR)
        val MINUTE = intPreferencesKey(Constants.KEY_ALARM_MINUTE)
        val TARGET_REPS = intPreferencesKey(Constants.KEY_TARGET_REPS)
        val ENABLED = booleanPreferencesKey(Constants.KEY_ALARM_ENABLED)
        val USE_24HOUR = booleanPreferencesKey(Constants.KEY_USE_24HOUR_FORMAT)
    }

    override fun getAlarmSettings(): Flow<AlarmSettings> {
        return context.dataStore.data.map { preferences ->
            AlarmSettings(
                hour = preferences[PreferencesKeys.HOUR] ?: 7,
                minute = preferences[PreferencesKeys.MINUTE] ?: 0,
                targetReps = preferences[PreferencesKeys.TARGET_REPS] ?: 10,
                isEnabled = preferences[PreferencesKeys.ENABLED] ?: false,
                use24HourFormat = preferences[PreferencesKeys.USE_24HOUR] ?: true
            )
        }
    }

    override suspend fun updateAlarmSettings(settings: AlarmSettings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.HOUR] = settings.hour
            preferences[PreferencesKeys.MINUTE] = settings.minute
            preferences[PreferencesKeys.TARGET_REPS] = settings.targetReps
            preferences[PreferencesKeys.ENABLED] = settings.isEnabled
            preferences[PreferencesKeys.USE_24HOUR] = settings.use24HourFormat
        }
    }
}
