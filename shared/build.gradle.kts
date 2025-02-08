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
            implementation(dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
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
