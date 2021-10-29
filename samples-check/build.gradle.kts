plugins {
    id("exemplar.java-conventions")
    id("exemplar.publishing-conventions")
}

dependencies {
    api(project(":samples-discovery"))
    api(libs.junit)
    compileOnly(libs.jsr305)
    implementation(libs.commons.io)
    implementation(libs.commons.lang3)
    implementation(gradleTestKit())
    testImplementation(libs.cglib)
    testImplementation(libs.groovy)
    testImplementation(libs.objenesis)
    testImplementation(libs.bundles.spock)
    testRuntimeOnly(libs.junit.vintage.engine)
}

// Add samples as inputs for testing
sourceSets["test"].resources.srcDirs("src/test/samples")

tasks.test {
    useJUnitPlatform {
        excludeTags.add("org.gradle.exemplar.test.runner.CoveredByTests")
    }
}
