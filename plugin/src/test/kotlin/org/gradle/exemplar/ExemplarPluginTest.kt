package org.gradle.exemplar

import org.assertj.core.api.Assertions.assertThat
import org.gradle.exemplar.internal.AbstractTest
import org.junit.jupiter.api.Test

class ExemplarPluginTest : AbstractTest {

    override val tempDir = createTempDir()

    @Test
    fun `applied plugin add sample-check dependency`() {
        val buildScript =
            """
                plugins {
                    id("org.gradle.exemplar")
                    `java-library`
                }
            """.trimIndent()

        withGradleRunner(buildScript, "dependencies") {
            // TODO: Is there a better way to test this? 🤔
            // We could also check that it is inside the `testRuntimeClasspath`
            assertThat(output).contains("org.gradle:sample-check:0.6.1")
        }
    }

    @Test
    fun `applied plugin add gradleExemplar to DependencyHandler`() {
        val buildScript =
            """
                import org.gradle.exemplar.gradleExemplar

                plugins {
                    id("org.gradle.exemplar")
                    `java-library`
                }

                dependencies {
                    gradleExemplar()
                }
            """.trimIndent()

        withGradleRunner(buildScript, "dependencies") {
            // TODO: Is there a better way to test this? 🤔
            // We could also check that it is inside the `testRuntimeClasspath`
            assertThat(output).contains("org.slf4j:slf4j-simple:1.7.16")
            // FIXME: Also gradleTestKit() should be checked
        }
    }
}
