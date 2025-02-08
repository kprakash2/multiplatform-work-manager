package com.kartikprakash2.multiplatform.tools.models

data class BackgroundJobType(
    val intervalInMillis: Long,
    val periodic: Boolean,
    val supportedPlatform: SupportedPlatform = SupportedPlatform.ALL,
)