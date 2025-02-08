/*
 * Copyright 2025 Kartik Prakash
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.kartikprakash2.multiplatform.tools

import co.touchlab.kermit.Logger
import com.kartikprakash2.multiplatform.tools.models.BackgroundJob
import com.kartikprakash2.multiplatform.tools.models.BackgroundJobConfiguration
import com.kartikprakash2.multiplatform.tools.models.BackgroundJobType
import com.kartikprakash2.multiplatform.tools.models.SupportedPlatform
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

    actual override suspend fun cancelJob(type: BackgroundJobType) {
        BGTaskScheduler.sharedScheduler.cancelTaskRequestWithIdentifier(type.identifier)
    }

    actual override suspend fun scheduleJob(type: BackgroundJobType) {
        val jobType = BackgroundWorkRepository.getJobConfiguration(type)
        checkPlatformAndRun(jobType) {
            callJobImmediately(type.identifier).also {
                logger.d("Job: ${type.identifier} result: $it")
            }
            scheduleNextAttempt(type.identifier)
        }
    }

    override suspend fun registerJobs(jobs: Map<BackgroundJobType, BackgroundJobConfiguration>) {
        super.registerJobs(jobs)
        jobs.forEach {
            registerJob(it.key)
        }
    }

    private fun registerJob(type: BackgroundJobType) {
        val result = BGTaskScheduler.sharedScheduler.registerForTaskWithIdentifier(
            type.identifier,
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
        val type = BackgroundWorkRepository.getJobTypeByIdentifier(identifier)
        val job = BackgroundWorkRepository.getJobConfiguration(type)

        cancelJob(type)

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

    private inline fun checkPlatformAndRun(job: BackgroundJobConfiguration, block: () -> Unit) {
        if (job.supportedPlatform == SupportedPlatform.ALL || job.supportedPlatform == SupportedPlatform.IOS_ONLY) {
            block.invoke()
        }
    }
}
