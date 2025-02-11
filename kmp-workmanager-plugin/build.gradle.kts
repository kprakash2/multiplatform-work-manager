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

import com.vanniktech.maven.publish.GradlePublishPlugin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    `java-gradle-plugin`
    alias(libs.plugins.gradle.publish)
    id("maven-publish-convention")
    id("signing")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile> {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(gradleApi())
    implementation(libs.kotlin.poet)

    testImplementation(libs.junit)
}

gradlePlugin {
    website = "https://github.com/kprakash2/kmp-workmanager"
    vcsUrl = "https://github.com/kprakash2/kmp-workmanager"

    plugins.create("kmpWorkManagerPlugin") {
        displayName = "Plugin for Kotlin Multiplatform Work Manager"
        description = "A plugin that helps setting up Kotlin Multiplatform Work Manager"
        tags = listOf("multiplatform", "kotlin", "work-manager", "android", "ios")
        id = "io.github.kprakash2.kmp-workmanager-gradle"
        implementationClass = "com.kprakash2.kmp.gradle.KmpWorkManagerPlugin"
    }
}


mavenPublishing {
    configure(GradlePublishPlugin())
    coordinates(group.toString(), "kmp-workmanager-gradle", version.toString())
}
