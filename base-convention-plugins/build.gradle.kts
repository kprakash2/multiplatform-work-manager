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

plugins {
    `kotlin-dsl`
}

dependencies {
    fun plugin(id: String, version: String) = "$id:$id.gradle.plugin:$version"

    implementation(libs.kotlin.gradle)
    implementation(libs.kotlin.compiler.embeddable)
    implementation(libs.android.gradle)

    implementation(plugin(libs.plugins.maven.publish.get().toString(), libs.versions.maven.publish.get()))
}
