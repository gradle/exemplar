package org.gradle.exemplar

import org.gradle.api.Plugin
import org.gradle.api.Project

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
