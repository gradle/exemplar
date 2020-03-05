package org.gradle.exemplar

import org.assertj.core.api.Assertions.assertThat
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class ExemplarPluginTest {

    @Test
    fun `apply plugin should add repository`() {
        withProjectBuilder {
            pluginManager.apply("org.gradle.exemplar")
            pluginManager.apply("java-library")

            val gradleArtifactoryRepo = repositories.find {
                (it as MavenArtifactRepository).url == uri("https://repo.gradle.org/gradle/libs")
            }
            assertThat(gradleArtifactoryRepo).isNotNull
        }
    }

    @Test
    fun `apply plugin should add dependencies`() {
        withProjectBuilder {
            pluginManager.apply("org.gradle.exemplar")
            pluginManager.apply("java-library")

            val sampleCheckDependency = dependenciesForConfiguration("testImplementation").find {
                it.group == "org.gradle" && it.name == "sample-check" && it.version == "0.6.1"
            }
            assertThat(sampleCheckDependency).isNotNull

            // Check for gradleTestKit
            assertThat(dependenciesForConfiguration("testImplementation").size).isEqualTo(2)

            val slf4jDependency = dependenciesForConfiguration("testRuntimeOnly").find {
                it.group == "org.slf4j" && it.name == "slf4j-simple" && it.version == "1.7.16"
            }
            assertThat(slf4jDependency).isNotNull
        }
    }
}

private fun withProjectBuilder(
    block: Project.() -> Unit
) {
    ProjectBuilder.builder()
        .build()
        .also { block(it) }
}

private fun Project.dependenciesForConfiguration(configuration: String) =
    configurations.getByName(configuration).dependencies
