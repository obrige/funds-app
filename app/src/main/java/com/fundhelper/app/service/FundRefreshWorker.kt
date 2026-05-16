package com.fundhelper.app.service

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.fundhelper.app.data.db.FundDao
import com.fundhelper.app.data.repository.FundRepository
import com.fundhelper.app.util.TradingTimeUtil
import com.fundhelper.app.widget.FundWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

@HiltWorker
class FundRefreshWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: FundRepository,
    private val fundDao: FundDao
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!TradingTimeUtil.isTradingTime()) return Result.success()

        try {
            val funds = fundDao.getAllFunds().first()
            if (funds.isNotEmpty()) {
                val codes = funds.joinToString(",") { it.code }
                repository.getFundRealtimeData(codes)
            }

            try {
                FundWidget().updateAll(GlanceAppWidgetManager(applicationContext))
            } catch (_: Exception) {}

            return Result.success()
        } catch (e: Exception) {
            return Result.retry()
        }
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
