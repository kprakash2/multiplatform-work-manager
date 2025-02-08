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

import com.kartikprakash2.multiplatform.tools.models.BackgroundJobType

interface BackgroundWorkRepository {
    suspend fun cancelJob(identifier: String)
    suspend fun scheduleJob(identifier: String)

    suspend fun registerJobs(
        jobs: Map<String, BackgroundJobType>
    ) {
        jobsMap.clear()
        jobsMap.putAll(jobs)
    }

    companion object {
        private val jobsMap = mutableMapOf<String, BackgroundJobType>()
        private lateinit var repository: BackgroundWorkRepository

        internal fun getJobType(identifier: String): BackgroundJobType {
            return jobsMap[identifier] ?: throw IllegalStateException(
                "Register job [$identifier] first using [registerJobs()]."
            )
        }

        fun initialize() {
            if (!(::repository.isInitialized)) {
                repository = BackgroundWorkRepositoryImpl()
            }
        }

        fun getInstance(): BackgroundWorkRepository {
            if (!(::repository.isInitialized)) {
                throw IllegalStateException("Call [BackgroundWorkRepository.initialize()] first.")
            }
            return repository
        }
    }
}
