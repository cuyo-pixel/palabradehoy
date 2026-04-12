package com.palabradeldia.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.palabradeldia.worker.DailyWordWorker
import java.time.Duration
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Schedules or cancels the daily WorkManager job.
 * The notification fires every day at 09:00. If the app is started after 09:00,
 * the first notification will fire the following day.
 */
@Singleton
class NotificationScheduler @Inject constructor() {

    fun schedule(context: Context) {
        val target    = LocalTime.of(9, 0)
        val now       = LocalDateTime.now()
        val nextFire  = if (now.toLocalTime().isBefore(target))
            now.toLocalDate().atTime(target)
        else
            now.toLocalDate().plusDays(1).atTime(target)
        val delay = Duration.between(now, nextFire)

        val request = PeriodicWorkRequestBuilder<DailyWordWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay.toMinutes(), TimeUnit.MINUTES)
            .setConstraints(Constraints.NONE)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DailyWordWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    fun cancel(context: Context) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(DailyWordWorker.WORK_NAME)
    }
}
