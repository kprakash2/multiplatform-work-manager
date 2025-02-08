/*
 * Copyright 2025 Kartik Prakash
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.kartikprakash2.kmp.workmanager

import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("UnnecessaryAbstractClass")
abstract class KmpWorkManagerPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("kmpworkmanager", KmpWorkManagerExtension::class.java, project)

        project.tasks.register("prepareKmpWorkManagerConfig", KmpWorkManagerConfigTask::class.java) {
            it.jobIdentifiers.set(extension.jobIdentifiers)
            it.iosAppInfoPlistPath.set(extension.iosAppInfoPlistPath)
            it.sourceSetDirectory.set(extension.sourceSetDirectory)
            it.packageName.set(extension.packageName)
            it.className.set(extension.className)
        }
    }
}
