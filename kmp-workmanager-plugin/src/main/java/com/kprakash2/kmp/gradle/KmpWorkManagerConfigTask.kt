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

package com.kprakash2.kmp.gradle

import com.kprakash2.kmp.gradle.utils.writeKmpBackgroundJobTypes
import org.gradle.api.DefaultTask
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject


abstract class KmpWorkManagerConfigTask : DefaultTask() {
    init {
        description = "Task to create KMP Work Manager Configuration"
        group = BasePlugin.BUILD_GROUP
    }

    @get:Input
    @get:Option(option = "jobIdentifiers", description = "Job Identifiers")
    abstract val jobIdentifiers: SetProperty<String>

    @get:Input
    @get:Option(option = "sourceDirectory", description = "Project's source directory")
    abstract val sourceSetDirectory: Property<File>

    @get:Input
    @get:Option(option = "packageName", description = "Package name where the config should be created")
    abstract val packageName: Property<String>

    @get:Input
    @get:Option(option = "className", description = "Config enum class name")
    @get:Optional
    abstract val className: Property<String>

    @get:Input
    @get:Option(option = "iosAppInfoPlistPath", description = "Path to iOS App's Info.plist file")
    abstract val iosAppInfoPlistPath: Property<String>

    @get:Inject
    abstract val execOps: ExecOperations


    @TaskAction
    fun sampleAction() {
        if (jobIdentifiers.orNull == null || !jobIdentifiers.isPresent) {
            throw IllegalStateException("jobIdentifiers set cannot be null or empty")
        }
        if (iosAppInfoPlistPath.orNull == null || !iosAppInfoPlistPath.isPresent) {
            throw IllegalStateException("iosAppInfoPlistPath cannot be null or empty")
        }
        if (packageName.orNull == null || !packageName.isPresent) {
            throw IllegalStateException("packageName cannot be null or empty")
        }

        logger.lifecycle("KMPWorkManager job identifiers are: ${jobIdentifiers.orNull}")
        logger.lifecycle("KMPWorkManager ios plist path: ${iosAppInfoPlistPath.get()}")
        logger.lifecycle("KMPWorkManager package name: ${packageName.get()}")
        logger.lifecycle("KMPWorkManager class name: ${className.orNull}")
        logger.lifecycle("KMPWorkManager source set dir: ${sourceSetDirectory.orNull}")

        jobIdentifiers.get().verifyIdentifiers()
        iosAppInfoPlistPath.get().verifyPlistFileExists()

        writeKmpBackgroundJobTypes(
            sourceSetDirectory = sourceSetDirectory.get(),
            packageName = packageName.get(),
            className = className.getOrElse(ENUM_CLASS_NAME_DEFAULT),
            jobIdentifiers = jobIdentifiers.get()
        )

        if (isMacOS()) {
            logger.lifecycle("KMPWorkManager updating Plist file with background job identifiers")

            val deletingExistingIdentifiersExitCode = execOps.exec {
                it.commandLine(
                    PLIST_BUDDY_PATH,
                    "-c",
                    "Delete :$PLIST_BG_TASK_IDS_KEY",
                    iosAppInfoPlistPath.get()
                )
            }.exitValue
            if (deletingExistingIdentifiersExitCode != 0) {
                throw IllegalStateException("Unable to remove existing identifiers from ${iosAppInfoPlistPath.get()}")
            }

            val settingIdentifiersExitCode = execOps.exec {
                it.commandLine(
                    PLIST_BUDDY_PATH,
                    "-c",
                    "Add :$PLIST_BG_TASK_IDS_KEY: string '${jobIdentifiers.get().first()}'",
                    iosAppInfoPlistPath.get()
                )
            }.exitValue
            if (settingIdentifiersExitCode != 0) {
                throw IllegalStateException("Unable to set identifiers in ${iosAppInfoPlistPath.get()}")
            }
        } else {
            logger.lifecycle("skip updating Plist file.")
        }
    }

    private fun String.verifyPlistFileExists() {
        val file = File(this)
        if (file.exists().not()) {
            throw IllegalStateException("Plist file does not exists at path $this")
        }
        if (file.extension != "plist") {
            throw IllegalStateException("Invalid Plist file. $this")
        }
    }

    private fun Set<String>.verifyIdentifiers() {
        forEach {
            if (it.uppercase() != it ) {
                throw IllegalStateException("Job Identifiers should be all uppercase ($it)")
            }
            if (it.isBlank()) {
                throw IllegalStateException("Job Identifier cannot be empty.")
            }

            if (it.contains(" ")) {
                throw IllegalStateException("Job Identifier cannot have spaces.")
            }
        }
    }

    private fun isMacOS(): Boolean {
        val osName = System.getProperty("os.name").lowercase()
        return osName.contains("mac") || osName.contains("darwin")
    }

    private companion object {
        const val ENUM_CLASS_NAME_DEFAULT = "BackgroundJobIdentifiers"
        const val PLIST_BG_TASK_IDS_KEY = "BGTaskSchedulerPermittedIdentifiers"
        const val PLIST_BUDDY_PATH = "/usr/libexec/PlistBuddy"
    }
}
