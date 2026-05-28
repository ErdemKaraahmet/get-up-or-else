package com.getuporelse.core.di

import android.content.Context
import com.getuporelse.data.local.AndroidAlarmController
import com.getuporelse.data.local.AndroidAlarmScheduler
import com.getuporelse.data.local.AndroidDebugAlarmController
import com.getuporelse.data.repository.DataStoreAlarmRepository
import com.getuporelse.domain.alarm.AlarmController
import com.getuporelse.domain.alarm.AlarmRepository
import com.getuporelse.domain.alarm.AlarmScheduler
import com.getuporelse.domain.alarm.DebugAlarmController
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AlarmModule {

    @Binds
    @Singleton
    abstract fun bindAlarmRepository(
        dataStoreAlarmRepository: DataStoreAlarmRepository
    ): AlarmRepository

    @Binds
    @Singleton
    abstract fun bindAlarmScheduler(
        androidAlarmScheduler: AndroidAlarmScheduler
    ): AlarmScheduler

    @Binds
    @Singleton
    abstract fun bindAlarmController(
        androidAlarmController: AndroidAlarmController
    ): AlarmController

    @Binds
    @Singleton
    abstract fun bindDebugAlarmController(
        androidDebugAlarmController: AndroidDebugAlarmController
    ): DebugAlarmController
}

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
}
