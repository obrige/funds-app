package com.fundhelper.app.service

import android.content.Context
import androidx.work.*
import com.fundhelper.app.util.TradingTimeUtil
import java.util.concurrent.TimeUnit

class FundRefreshWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!TradingTimeUtil.isTradingTime()) return Result.success()
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "fund_refresh_periodic"

        fun enqueue(context: Context, intervalMinutes: Int = 2) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<FundRefreshWorker>(
                intervalMinutes.toLong(), TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
