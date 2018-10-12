package org.gradle.exemplar

import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.net.URI

internal fun RepositoryHandler.gradleArtifactory() {
    maven {
        it.url = URI("https://repo.gradle.org/gradle/libs")
    }
}
