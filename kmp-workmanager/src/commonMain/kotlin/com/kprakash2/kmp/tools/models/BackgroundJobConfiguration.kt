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

package com.kprakash2.kmp.tools.models

/**
 * Represents configuration of a background job.
 *
 * @property intervalInMillis Represents interval between each periodic execution in milliseconds.
 * @property periodic Represents if the task is periodic.
 * @property supportedPlatform Represents which platform the task should execute on.
 *
 * @see [SupportedPlatform]
 */
data class BackgroundJobConfiguration(
    val intervalInMillis: Long,
    val periodic: Boolean,
    val supportedPlatform: SupportedPlatform = SupportedPlatform.ALL,
)