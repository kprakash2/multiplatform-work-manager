package com.kartik.multiplatform.tools.background

internal expect class BackgroundWorkRepositoryImpl() : BackgroundWorkRepository {
    override suspend fun cancelJob(identifier: String)
    override suspend fun scheduleJob(identifier: String)
}