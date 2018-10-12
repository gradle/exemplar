package org.gradle.exemplar.internal

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File

internal interface AbstractTest {

    val tempDir: File

    fun withGradleRunner(
        buildScriptContent: String,
        vararg args: String,
        fail: Boolean = false,
        block: BuildResult.() -> Unit
    ) {
        createBuildScript(buildScriptContent)

        GradleRunner.create()
            .withProjectDir(tempDir)
            .withPluginClasspath()
            .withArguments(*args)
            .run { if (fail) buildAndFail() else build() }
            .also { block(it) }
    }

    private fun createBuildScript(content: String) =
        File(tempDir, "build.gradle.kts").apply {
            parentFile.mkdirs()
            createNewFile()
            writeText(content)
        }
}
