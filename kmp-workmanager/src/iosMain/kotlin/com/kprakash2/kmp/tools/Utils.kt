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

package com.kprakash2.kmp.tools

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCObjectVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.pointed
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.Foundation.NSError

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun <T> throwError(block: (errorPointer: CPointer<ObjCObjectVar<NSError?>>) -> T): T {
    memScoped {
        val errorPointer: CPointer<ObjCObjectVar<NSError?>> = alloc<ObjCObjectVar<NSError?>>().ptr
        val result: T = block(errorPointer)
        val error: NSError? = errorPointer.pointed.value
        if (error != null) {
            throw NSErrorException(error)
        } else {
            return result
        }
    }
}

class NSErrorException(nsError: NSError) : Exception(nsError.toString())