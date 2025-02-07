package com.kartik.multiplatform.tools.background

import com.kartik.multiplatform.tools.background.models.BackgroundJobType

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
