package com.kartik.multiplatform.tools.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import com.kartik.multiplatform.tools.background.models.BackgroundJob
import org.koin.core.component.KoinComponent
import org.koin.core.qualifier.named

internal class AndroidBackgroundJobWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {
    private val logger = Logger.withTag(this::class.java.simpleName)

    override suspend fun doWork(): Result {
        val tag = inputData.getString(BG_JOB_TAG) ?: return Result.failure()
        val backgroundJob = getKoin().get<BackgroundJob>(qualifier = named(tag))
        val res = runBackgroundJob(backgroundJob)
        return if (res) {
            Result.success()
        } else {
            Result.failure()
        }
    }

    private suspend fun runBackgroundJob(backgroundJob: BackgroundJob): Boolean {
        return try {
            if (!backgroundJob.validate()) {
                throw Exception("Validation has failed")
            }
            if (!backgroundJob.run()) {
                throw Exception("Execute code not passed")
            }
            true
        } catch (error: Throwable) {
             logger.w("Error running background job", error)
            false
        }
    }
}
