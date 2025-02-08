package com.kartikprakash2.multiplatform.tools.models

interface BackgroundJob {
    suspend fun run(): Boolean
    suspend fun validate(): Boolean
}
