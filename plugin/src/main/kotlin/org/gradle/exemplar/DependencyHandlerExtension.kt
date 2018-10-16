package org.gradle.exemplar

import org.gradle.api.artifacts.dsl.DependencyHandler

internal fun DependencyHandler.addExemplarDependencies() {
    // TODO: We have to inject the project-version into it and replace the hardcoded "0.6.1"
    // how can we do that?
    add("testImplementation", "org.gradle:sample-check:0.6.1")
    add("testImplementation", gradleTestKit())
    // FIXME: Hardcoding of the version is a bad idea.. Try to change this.. somehow.
    add("testRuntimeOnly", "org.slf4j:slf4j-simple:1.7.16")
}
