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
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import co.touchlab.kermit.Logger
import com.kartikprakash2.multiplatform.tools.models.BackgroundJob
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
