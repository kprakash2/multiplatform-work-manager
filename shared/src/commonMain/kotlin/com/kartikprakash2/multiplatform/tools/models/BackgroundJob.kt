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

package com.kartikprakash2.multiplatform.tools.models

/**
 * Background Job interface.
 *
 * @sample com.kartikprakash2.multiplatform.tools.models.MyBackgroundJobSample
 *
 */
interface BackgroundJob {
    /**
     * Executes the actual background work.
     *
     * @return Returns True if the work was completed successfully. False otherwise.
     */
    suspend fun run(): Boolean

    /**
     * Validates the background work before executing it.
     *
     * @return Returns True if the background work should be executed. False otherwise.
     */
    suspend fun validate(): Boolean
}

class MyBackgroundJobSample : BackgroundJob {
    override suspend fun run(): Boolean {
        // do long running background work
        return true
    }

    override suspend fun validate(): Boolean {
        // validate if the background work/task should be executed or not.
        return true
    }
}