package com.kartik.multiplatform.tools.background

import co.touchlab.kermit.Logger
import com.kartik.multiplatform.tools.background.models.BackgroundJob
import com.kartik.multiplatform.tools.background.models.BackgroundJobType
import com.kartik.multiplatform.tools.background.models.SupportedPlatform
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import platform.BackgroundTasks.BGAppRefreshTask
import platform.BackgroundTasks.BGAppRefreshTaskRequest
import platform.BackgroundTasks.BGTaskScheduler
import platform.Foundation.NSDate
import platform.Foundation.dateWithTimeIntervalSince1970
import kotlin.time.Duration.Companion.milliseconds

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual class BackgroundWorkRepositoryImpl : BackgroundWorkRepository, KoinComponent {
    private val logger = Logger.withTag(this::class.simpleName ?: "BackgroundWorkRepositoryImpl")

    actual override suspend fun cancelJob(identifier: String) {
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(identifier)
    }

    actual override suspend fun scheduleJob(identifier: String) {
        val jobType = BackgroundWorkRepository.getJobType(identifier)
        checkPlatformAndRun(jobType) {
            callJobImmediately(identifier).also {
                logger.d("Job: $identifier result: $it")
            }
            scheduleNextAttempt(identifier)
        }
    }

    override suspend fun registerJobs(jobs: Map<String, BackgroundJobType>) {
        super.registerJobs(jobs)
        // TODO check if it's needed
//        jobs.forEach {
//            registerJob(it.key)
//        }
    }

    private fun registerJob(identifier: String) {
        val result = BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
            identifier,
            null
        ) { task ->
            try {
                logger.d("Executing job: ${task?.identifier}")
                runExecutionBlock((task as BGAppRefreshTask))
            } catch (e: Exception) {
                logger.e("Exception registering job", e)
                return@registerForTaskWithIdentifier
            }
        }
        logger.d("Result registering background task: $result")
    }

    private fun runExecutionBlock(task: BGAppRefreshTask) {
        logger.d("Running from execution block")
        task.setExpirationHandler {
            task.setTaskCompletedWithSuccess(false)
            runBlocking {
                scheduleNextAttempt(task.identifier)
            }
        }

        runBlocking {
            val result = callJobImmediately(task.identifier())
            logger.d("Run Execution block result: $result")
            task.setTaskCompletedWithSuccess(result)
            scheduleNextAttempt(task.identifier())
        }
    }

    private fun callJobImmediately(identifier: String): Boolean {
        val backgroundJob = get<BackgroundJob>(
            qualifier = named(identifier)
        )
        return try {
            runBlocking {
                runBackgroundJob(backgroundJob)
            }
        } catch (e: Exception) {
            logger.e("Error running background job $identifier", e)
            false
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

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun scheduleNextAttempt(identifier: String) {
        cancelJob(identifier)

        val job = BackgroundWorkRepository.getJobType(identifier)
        val request = BGAppRefreshTaskRequest(identifier)
        val utcNow = Clock.System.now().toEpochMilliseconds().milliseconds
        val nextTriggerTime = utcNow + job.intervalInMillis.milliseconds
        val nextTriggerDate =
            NSDate.dateWithTimeIntervalSince1970(nextTriggerTime.inWholeSeconds.toDouble())

        request.setEarliestBeginDate(nextTriggerDate)

        logger.d("Setting up next trigger at $nextTriggerDate")

        try {
            throwError { errorPointer ->
                BGTaskScheduler.sharedScheduler.submitTaskRequest(request, errorPointer)
                    .also {
                        logger.d("Result submitting task request: $it")
                    }
            }
        } catch (e: Exception) {
            logger.e("Error scheduling task $identifier", e)
        }
    }

    private inline fun checkPlatformAndRun(job: BackgroundJobType, block: () -> Unit) {
        if (job.supportedPlatform == SupportedPlatform.ALL || job.supportedPlatform == SupportedPlatform.IOS_ONLY) {
            block.invoke()
        }
    }
}
