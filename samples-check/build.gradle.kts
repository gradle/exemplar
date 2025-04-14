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
    implementation(libs.junit.platform.engine)
    implementation(libs.junit.platform.launcher)
    implementation(gradleTestKit())
    testImplementation(libs.groovy)
    testImplementation(libs.objenesis)
    testImplementation(libs.bundles.spock)
    testRuntimeOnly(libs.junit.vintage.engine)
}

tasks.test {
    inputs.dir(layout.projectDirectory.dir("src/test/samples"))
        .withPropertyName("samplesDir")
        .withPathSensitivity(PathSensitivity.RELATIVE)

    useJUnitPlatform {
        excludeTags.add("org.gradle.exemplar.test.runner.CoveredByTests")
    }
}
