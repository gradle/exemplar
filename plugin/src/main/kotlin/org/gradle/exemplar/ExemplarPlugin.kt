package org.gradle.exemplar

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.artifacts.dsl.RepositoryHandler
import java.net.URI

/**
 * This plugin will add all necessary dependencies to the project.
 * So you can direct start using Exemplar.
 */
class ExemplarPlugin : Plugin<Project> {

    override fun apply(project: Project) = project.runWhenJavaPluginAvailable {
        with(project) {
            addGradleArtifactoryRepo()
            addDependencies()
        }
    }

    private fun Project.runWhenJavaPluginAvailable(block: () -> Unit) {
        // TODO: Do we really need java-library?
        // Or is java-base enough?
        pluginManager.withPlugin("java-library") {
            block()
        }
    }

    private fun Project.addGradleArtifactoryRepo() =
        repositories.gradleArtifactory()

    private fun Project.addDependencies() =
        dependencies.addExemplarDependencies()
}

internal fun DependencyHandler.addExemplarDependencies() {
    // TODO: We have to inject the project-version into it and replace the hardcoded "0.6.1"
    // how can we do that?
    add("testImplementation", "org.gradle:sample-check:0.6.1")
    add("testImplementation", gradleTestKit())
    // FIXME: Hardcoding of the version is a bad idea.. Try to change this.. somehow.
    add("testRuntimeOnly", "org.slf4j:slf4j-simple:1.7.16")
}


internal fun RepositoryHandler.gradleArtifactory() {
    maven {
        it.url = URI("https://repo.gradle.org/gradle/libs")
    }
}
