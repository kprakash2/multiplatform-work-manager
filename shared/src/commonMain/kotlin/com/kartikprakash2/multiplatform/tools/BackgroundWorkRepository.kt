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

import com.kartikprakash2.multiplatform.tools.models.BackgroundJobConfiguration
import com.kartikprakash2.multiplatform.tools.models.BackgroundJobType

interface BackgroundWorkRepository {
    suspend fun cancelJob(type: BackgroundJobType)
    suspend fun scheduleJob(type: BackgroundJobType)

    suspend fun registerJobs(
        jobs: Map<BackgroundJobType, BackgroundJobConfiguration>
    ) {
        jobsMap.clear()
        jobsMap.putAll(jobs)
    }

    companion object {
        private val jobsMap = mutableMapOf<BackgroundJobType, BackgroundJobConfiguration>()
        private lateinit var repository: BackgroundWorkRepository

        internal fun getJobConfiguration(type: BackgroundJobType): BackgroundJobConfiguration {
            return jobsMap[type] ?: throw IllegalStateException(
                "Register job [${type.identifier}] first using [registerJobs()]."
            )
        }

        internal fun getJobTypeByIdentifier(id: String): BackgroundJobType {
            return jobsMap.keys.find { it.identifier == id } ?: throw IllegalStateException(
                "Register job [${id}] first using [registerJobs()]."
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
