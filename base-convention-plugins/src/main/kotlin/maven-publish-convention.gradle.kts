import com.vanniktech.maven.publish.SonatypeHost

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
    id("com.vanniktech.maven.publish")
    id("signing")
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name = "Multiplatform Work Manager"
        description = "Multiplatform Work Manager."
        inceptionYear = "2025"
        url = "https://github.com/kprakash2/multiplatform-work-manager/"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "kprakash2"
                name = "Kartik Prakash"
                url = "https://github.com/kprakash2/"
            }
        }
        scm {
            url = "https://github.com/kprakash2/multiplatform-work-manager/"
        }
    }
}

signing {
    val signingKey = findProperty("signing.key")?.toString()?.replace("\\n", "\n")

    if (signingKey != null) {
        val signingKeyPassword = findProperty("signing.password")?.toString()

        useInMemoryPgpKeys(
            signingKey,
            signingKeyPassword
        )
        sign(publishing.publications)
    }
}
