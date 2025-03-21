# Multiplatform Work Manager

[![Kotlin Alpha](https://kotl.in/badges/alpha.svg)](https://kotlinlang.org/docs/components-stability.html)
[![License](https://img.shields.io/badge/license-Apache--2.0-green)](LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.kprakash2/kmp-workmanager)](https://central.sonatype.com/artifact/io.github.kprakash2/kmp-workmanager)

![Android](https://img.shields.io/badge/android-green)
![iOS](https://img.shields.io/badge/iOS-grey)

A Kotlin Multiplatform library that simplifies scheduling and executing periodic background jobs across different platforms (iOS, and Android). This library provides an easy-to-use API for setting up jobs that run at specified intervals, ensuring that your application can perform tasks in the background efficiently.

> NOTE: This library is in pre-release phase. Stable release publishing is expected to be by July 20th 2025.

## Features

- **Cross-Platform Support**: Works on Android & iOS
- **Simple API**: Easily define periodic jobs with customizable intervals.
- **Flexible Interval Setting**: Supports flexible interval configuration for job execution.
- **Flexible Platform Configuration**: Choose between running on Android only, iOS only or on both platforms.
- **Platform-Specific Implementations**: Adapts to platform-specific background task mechanisms (e.g., WorkManager on Android, background task APIs on iOS).
- **Customizable Job Logic**: Define custom logic to be executed in background tasks.

## Setup

For android this library uses Android Work Manager to schedule jobs, which has an easy setup and it's all at runtime. However on iOS side of things, the job identifiers need to be registered at compile/build time of the application. For iOS these unique identifiers are setup in the target's Info.plist file.

To achieve a common solution this uses a gradle plugin, that let you define your background job identifiers in your build setup which stays common between the platforms and provides a solution where you don't have to manually setup the identifiers in your iOS setup and copy that on Android for reference in your common/shared code.

### 1.Setup Gradle plugin

Add the following gradle plugin in your `build.gradle.kts` of `shared`/common gradle module that is shared between Android & iOS

```
plugins {
    id ("io.github.kprakash2.kmp-workmanager-gradle") version "<version>"
}
```

### 2. Setup background job identifiers

Add the following block in your `build.gradle.kts` of `shared`/common gradle module that is shared between Android & iOS

```
kmpworkmanager {
    jobIdentifiers = setOf<String>(<JOB_ID1>, <JOB_ID2>, ...)
    iosAppInfoPlistPath = <Path to your iOSApp's Info.plist file>
    packageName = <root package name for your module's source set>
    sourceSetDirectory = <path to your modules source set>
    className = <background job type enum class name> // default: `BackgroundJobIdentifiers`
}

```

* `jobIdentifiers`: Set of identifiers for your background jobs. Identifiers should be all upper case and without any whitespaces.
* `iosAppInfoPlistPath`: Path to your iOS App's Info.plist file. Usually it will be similar to `../iosApp/iosApp/Info.plist`
* `packageName`: Root Package name of your module's sourceset.
* `sourceSetDirectory`: File property pointing to your module's common source set. Usually it will be similar to `project.file("src/commonMain/kotlin")`
* `classNme`: This is optional, if you'd like to use a custom name of the background job type enum class name.

The above extension will configure all `compile` type tasks in your module to run `prepareKmpWorkManagerConfig` which will create a config file in your source set which you can use to register your jobs at runtime.

### 3. Setup the dependencies

To include the library in your Kotlin Multiplatform project, add the following dependencies in your `shared` module's `build.gradle.kts` file:

```
kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.kprakash2:kmp-workmanager:<version>")
            }
        }
    }
}
```

## Usage

Once you have compiled your project with above setup, in your module's sourceset there would be enum class (`BackgroundJobIdentifiers` or `className` if you provided the parameter in `kmpWorkManager` configuration) available with all your job identifiers.

### 1. Declare a provider for your background Jobs

This provider should return a `BackgroundJob` type for all your background job identifiers.

```
class MyBackgroundJobProvider(): BackgroundJobProvider<BackgroundJobIdentifiers> {
    override suspend fun <T: BackgroundJobType> getBackgroundJob(type: T): BackgroundJob {
        return when (type as BackgroundJobIdentifiers) {
            ...
            ...
        }
    }
}
```

### 2. Initialize Background Work Repository

You can declare the following function or also tie it to your dependency injection framework if you're using any and then call this from each platform specific code.

Note: `context` can be null for iOS, it's only required for Android platform.

```
// In commonMain
fun initializeBackgroundWorkRepository(context: Any?) {
    BackgroundWorkRepository.initialize(
        context, // This can be null for iOS
        MyBackgroundJobProvider()
    )
}

// In androidMain
initializeBackgroundWorkRepository(context)


// In iosMain
initializeBackgroundWorkRepository(null)
```

### 3. Register Jobs Configuration

You can configure each of your background job type with multiple parameters using `BackgroundJobConfiguration`

```
val backgroundWorkRepository = BackgroundWorkRepository.getInstance()
val jobsConfiguration = BackgroundJobIdentifiers.entries.map {
    it to when (it) {
        BackgroundJobIdentifiers.<id> -> BackgroundJobConfiguration(...)
        ....
    }
}
backgroundWorkRepository.registerJobs(jobsConfiguration)
```

1. `BackgroundWorkRepository.getInstance()`: Provide an instance of `BackgroundWorkRepository`. Make sure to call `BackgroundWorkRepository.initialize()` before this. [See Background Job Configuration](#backgrouind-job-configuration)
2. `BackgroundJobConfiguration`: Represents the configuration for your background job
3. `backgroundWorkRepository.registerJobs(..)`: Registers all of your background job. Make sure to call this before you call `backgroundWorkRepository.schedule(...)`. This is specially required for iOS platform as we need to register all BGApp tasks before they can be scheduled. [See Apple documentation for more details.](https://developer.apple.com/documentation/backgroundtasks/bgtaskscheduler/register(fortaskwithidentifier:using:launchhandler:))

### Schedule job

```
val backgroundWorkRepository = BackgroundWorkRepository.getInstance()
backgroundWorkRepository.schedule(...)
```

### Cancel job

```
val backgroundWorkRepository = BackgroundWorkRepository.getInstance()
backgroundWorkRepository.cancelJob(...)
```

#### Background Job Configuration

Example:

```
BackgroundJobConfiguration(
    intervalInMillis = 900000,
    periodic = true,
    supportedPlatform = SupportedPlatform.ALL
)
```

1. `intervalInMillis`: Represents interval between each periodic task run in milliseconds. Both Android and iOS only support 15 minutes of minimum interval.
2. `periodic`: If task is recurring, this should be set to `true`. If the task needs to be run just once, this should be set to `false`.
3. `supportedPlatform`: Represents the platform on which the task should run. It can be `ANRDOID_ONLY`, `IOS_ONLY`, `ALL`. If you want your task to run on both Android and IOS, use `ALL`.

## TODO

- [x] Setup Github Actions
- [x] Add publishing and publish 1st version
- [ ] Add test for `shared` module
- [ ] Add test for `kmp-workmanager` plugin
- [ ] Add support for high frequency periodic jobs (recurring at frequency of less than 15 mins.)
- [ ] Add support for backoff strategies
- [ ] Add support for Network constraints
- [ ] Add support for Phone Battery constraints

## License

    Copyright 2025 Kartik Prakash

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
#
