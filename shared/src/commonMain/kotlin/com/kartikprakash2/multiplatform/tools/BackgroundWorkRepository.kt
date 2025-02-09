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

import com.kartikprakash2.multiplatform.tools.models.BackgroundJob
import com.kartikprakash2.multiplatform.tools.models.BackgroundJobConfiguration
import com.kartikprakash2.multiplatform.tools.models.BackgroundJobType

/**
 * Repository to schedule one time or periodic background work.
 *
 */
interface BackgroundWorkRepository {
    /**
     * Cancel a background job of type [BackgroundJobType]
     *
     * @param type Represents background job type
     */
    suspend fun cancelJob(type: BackgroundJobType)

    /**
     * Scheduled a background job type [BackgroundJobType].
     * Make sure to call [registerJobs] before scheduling any job.
     *
     *  @param type Represents background job type
     */
    suspend fun scheduleJob(type: BackgroundJobType)

    /**
     * Register jobs with their configuration of type [BackgroundJobConfiguration]
     *
     *  @param jobs Mapping of [BackgroundJobType] to it's configuration of type [BackgroundJobConfiguration]
     */
    suspend fun registerJobs(
        jobs: Map<BackgroundJobType, BackgroundJobConfiguration>
    ) {
        jobsMap.clear()
        jobsMap.putAll(jobs)
    }

    companion object {
        private val jobsMap = mutableMapOf<BackgroundJobType, BackgroundJobConfiguration>()
        private lateinit var repository: BackgroundWorkRepository
        private lateinit var provider: BackgroundJobProvider<*>
        private var context: Any? = null

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

        internal suspend fun getBackgroundJob(type: BackgroundJobType): BackgroundJob {
            if (::provider.isInitialized.not()) {
                throw IllegalStateException("Register job provider first, using [BackgroundWorkRepository.initialize()]")
            }
            return provider.getBackgroundJob(type)
        }

        /**
         * Initializes the [BackgroundWorkRepository].
         *
         * @param context Context for Android platform. Provide `null` for iOS.
         * @param provider Background job provider of type [BackgroundJobProvider].
         * Used by [BackgroundWorkRepository] to get [BackgroundJob] for execution of background work.
         */
        fun initialize(
            context: Any?,
            provider: BackgroundJobProvider<*>
        ) {
            this.context = context
            this.provider = provider
            if (!(::repository.isInitialized)) {
                repository = BackgroundWorkRepositoryImpl(context)
            }
        }

        /**
         * Returns an instance of [BackgroundWorkRepository].
         * Call [BackgroundWorkRepository.initialize] before this.
         *
         * Throws [IllegalStateException] if [BackgroundWorkRepository] is not initalized.
         */
        fun getInstance(): BackgroundWorkRepository {
            if (!(::repository.isInitialized)) {
                throw IllegalStateException("Call [BackgroundWorkRepository.initialize()] first.")
            }
            return repository
        }
    }
}
