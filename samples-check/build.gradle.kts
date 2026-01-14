plugins {
    id("exemplar.java-conventions")
    id("exemplar.publishing-conventions")
}

dependencies {
    api(project(":samples-discovery"))
    api(libs.junit4)
    compileOnly(libs.jsr305)
    implementation(libs.commons.io)
    implementation(libs.commons.lang3)
    implementation(gradleTestKit())
    testImplementation(libs.groovy)
    testImplementation(libs.objenesis)
    testImplementation(libs.junit.launcher)
    testImplementation(libs.bundles.spock)
    testRuntimeOnly(libs.junit.vintage)
}

tasks.test {
    inputs.dir(layout.projectDirectory.dir("src/test/samples"))
        .withPropertyName("samplesDir")
        .withPathSensitivity(PathSensitivity.RELATIVE)

    useJUnitPlatform {
        excludeTags.add("org.gradle.exemplar.test.runner.CoveredByTests")
    }
}
