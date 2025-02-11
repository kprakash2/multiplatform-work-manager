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

import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import java.io.File
import javax.inject.Inject

abstract class KmpWorkManagerExtension
@Inject
constructor(project: Project) {
    private val objects = project.objects

    val jobIdentifiers: SetProperty<String> = objects.setProperty(String::class.java)
    val iosAppInfoPlistPath: Property<String> = objects.property(String::class.java)
    val sourceSetDirectory: Property<File> = objects.property(File::class.java)
    val packageName: Property<String> = objects.property(String::class.java)
    val className: Property<String> = objects.property(String::class.java)
}