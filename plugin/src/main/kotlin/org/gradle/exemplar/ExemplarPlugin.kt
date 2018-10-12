package org.gradle.exemplar

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * This plugin will by default:
 * * add the `org.gradle.exemplar` dependency to the `testImplementation` configuration
 *
 * It will also provide a extension function which you can call inside the `dependencies {}` block
 * to add additional dependencies when you want to run the tests with the **Gradle TestKit**.
 * Call either `gradleExemplar()` or `DependencyHandlerExtensionKt.gradleExemplar(this)`.
 */
class ExemplarPlugin : Plugin<Project> {

    override fun apply(project: Project) = project.runWhenJavaPluginAvailable {
        with(project) {
            addGradleArtifactoryRepo()
            addSampleCheckDependency()
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

    private fun Project.addSampleCheckDependency() =
        dependencies.sampleCheck()
}
