plugins {
    id("exemplar.java-conventions")
    id("exemplar.publishing-conventions")
    id("groovy")
}

dependencies {
    api(project(":sample-discovery"))
    api(Libraries.JUNIT)
    compileOnly(Libraries.JSR305)
    implementation(Libraries.COMMONS_IO)
    implementation(Libraries.COMMONS_LANG3)
    implementation(gradleTestKit())
    testImplementation(Libraries.SPOCK_CORE)
    testImplementation(Libraries.CGLIB)
    testImplementation(Libraries.OBJENESIS)
    testImplementation(Libraries.GROOVY)
}

// Add samples as inputs for testing
sourceSets["test"].resources.srcDirs("src/test/samples")

tasks.test {
    useJUnit {
        excludeCategories.add("org.gradle.samples.test.runner.CoveredByTests")
    }
}
