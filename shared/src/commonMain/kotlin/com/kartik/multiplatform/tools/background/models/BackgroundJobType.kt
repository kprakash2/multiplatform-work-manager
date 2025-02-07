package com.kartik.multiplatform.tools.background.models

data class BackgroundJobType(
    val intervalInMillis: Long,
    val periodic: Boolean,
    val supportedPlatform: SupportedPlatform = SupportedPlatform.ALL,
)