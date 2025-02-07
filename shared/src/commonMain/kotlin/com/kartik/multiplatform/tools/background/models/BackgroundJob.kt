package com.kartik.multiplatform.tools.background.models

interface BackgroundJob {
    suspend fun run(): Boolean
    suspend fun validate(): Boolean
}
