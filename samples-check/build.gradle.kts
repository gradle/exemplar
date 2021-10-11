plugins {
    id("exemplar.java-conventions")
    id("exemplar.publishing-conventions")
    id("groovy")
}

dependencies {
    api(project(":samples-discovery"))
    implementation(Libraries.JUNIT)
    implementation("org.junit.platform:junit-platform-engine:1.8.1")
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
        excludeCategories.add("org.gradle.exemplar.test.runner.CoveredByTests")
    }
}
