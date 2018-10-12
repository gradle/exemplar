package org.gradle.exemplar

import org.gradle.api.artifacts.dsl.DependencyHandler

internal fun DependencyHandler.sampleCheck() =
    add("testImplementation", "org.gradle:sample-check:0.6.1")

fun DependencyHandler.gradleExampler() {
    add("testImplementation", gradleTestKit())
    add("testRuntime", "org.slf4j:slf4j-simple:1.7.16")
}
