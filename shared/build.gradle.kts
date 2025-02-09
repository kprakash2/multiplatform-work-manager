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

import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.maven.publish)
    id("signing")
}

group = "com.kartikprakash2.multiplatform.tools"
version = rootProject.version.toString()

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    androidTarget {
        publishAllLibraryVariants()
        compilations.all {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_17)
                }
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    
    sourceSets {
        commonMain.dependencies {
            implementation(libs.coroutines.core)
            implementation(libs.kermit)
            implementation(libs.kotlinx.datetime)
        }
        androidMain.dependencies {
            implementation(libs.androidx.work)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.kartikprakash2.multiplatform.tools"
    compileSdk = 35
    defaultConfig {
        minSdk = 24
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    coordinates(group.toString(), "multiplatform-work-manager", version.toString())

    pom {
        name = "Multiplatform Work Manager library"
        description = "Multiplatform Work Manager library."
        inceptionYear = "2025"
        url = "https://github.com/kartikprakash2/multiplatform-work-manager/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "kartikprakash2"
                name = "Kartik Prakash"
                url = "https://github.com/kartikprakash2/"
            }
        }
        scm {
            url = "https://github.com/kartikprakash2/multiplatform-work-manager/"
        }
    }
}

val publishingProperties = Properties().apply {
    load(File(rootDir, "publishing.properties").inputStream())
}

signing {
    val signingKeyId = publishingProperties.getProperty("signing.keyId")?.toString()?.replace("\\n", "\n")

    if (signingKeyId != null) {
        val signingKeyPassword = publishingProperties.getProperty("signing.password")?.toString()

        useInMemoryPgpKeys(
            signingKeyId,
            signingKeyPassword
        )
        sign(publishing.publications)
    }
}
