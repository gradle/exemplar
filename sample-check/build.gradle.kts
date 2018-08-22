plugins {
    groovy
}

dependencies {
    constraints {
        compile("org.codehaus.groovy:groovy-all:2.4.15")
    }
    api(project(":sample-discovery"))
    api(Libraries.JUNIT)
    compileOnly(Libraries.JSR305)
    implementation(Libraries.COMMONS_IO)
    implementation(Libraries.COMMONS_LANG3)
    implementation(gradleTestKit())
    testImplementation(Libraries.SPOCK_CORE)
}

// Add samples as inputs for testing
sourceSets["test"].resources.srcDirs("src/test/samples")
