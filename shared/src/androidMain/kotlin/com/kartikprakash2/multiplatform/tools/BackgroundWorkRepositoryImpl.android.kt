/*
 * Copyright 2025 Kartik Prakash
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.kartikprakash2.multiplatform.tools

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import co.touchlab.kermit.Logger
import com.kartikprakash2.multiplatform.tools.models.BackgroundJobType
import com.kartikprakash2.multiplatform.tools.models.SupportedPlatform
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.concurrent.TimeUnit
import kotlin.getValue

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
internal actual class BackgroundWorkRepositoryImpl : BackgroundWorkRepository, KoinComponent {
    private lateinit var workManager: WorkManager
    private val context: Context by inject()

    private val logger = Logger.withTag(this::class.java.simpleName)

    private fun ensureWorkManagerInitialized() {
        if (!this::workManager.isInitialized) {
            workManager = WorkManager.getInstance(context)
        }
    }

    actual override suspend fun cancelJob(identifier: String) {
        ensureWorkManagerInitialized()
        workManager.cancelAllWorkByTag(identifier)
    }

    actual override suspend fun scheduleJob(identifier: String) {
        val jobType = BackgroundWorkRepository.getJobType(identifier)
        checkPlatformAndRun(jobType) {
            ensureWorkManagerInitialized()
            logger.i("Scheduling Job: $jobType")
            cancelJob(identifier)
            val inputData: Data = workDataOf(
                BG_JOB_TAG to identifier
            )
            when {
                jobType.periodic.not() -> {
                    scheduleOneTimeJob(inputData, identifier)
                }
                else -> {
                    scheduleLowFrequencyJob(inputData, jobType, identifier)
                }
            }
        }
    }

    private fun scheduleOneTimeJob(
        inputData: Data,
        identifier: String
    ) {
        workManager.beginUniqueWork(
            identifier,
            ExistingWorkPolicy.KEEP,
            OneTimeWorkRequestBuilder<AndroidBackgroundJobWorker>()
                .setInputData(inputData)
                .addTag(identifier)
                .build()
        ).enqueue()
    }

    private fun scheduleLowFrequencyJob(
        inputData: Data,
        job: BackgroundJobType,
        identifier: String
    ) {
        workManager.enqueueUniquePeriodicWork(
            identifier,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<AndroidBackgroundJobWorker>(
                job.intervalInMillis,
                TimeUnit.MILLISECONDS
            )
                .setInputData(inputData)
                .addTag(identifier)
                .build()
        )
    }

    private inline fun checkPlatformAndRun(job: BackgroundJobType, block: () -> Unit) {
        if (job.supportedPlatform == SupportedPlatform.ALL || job.supportedPlatform == SupportedPlatform.ANDROID_ONLY) {
            block.invoke()
        }
    }
}
