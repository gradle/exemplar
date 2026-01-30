plugins {
    id("exemplar.java-conventions")
    id("jvm-test-suite")
    id("exemplar.publishing-conventions")
}

dependencies {
    implementation(project(":samples-check"))
    implementation(project(":samples-discovery"))
    implementation(libs.junit.engine)
    implementation(libs.commons.io)
    implementation(libs.commons.lang3)
}

testing {
    suites {
        register("integTest", JvmTestSuite::class) {
            useJUnitJupiter()
            dependencies {
                implementation(project())
                implementation(project(":samples-check"))
            }

            targets {
                all {
                    testTask.configure {
                        mustRunAfter(tasks.named("jar"))
                        testDefinitionDirs.from("src/test-definitions")
                        systemProperty("exemplar.sample.modifiers", "test.TestSampleModifier")
                        systemProperty("exemplar.output.normalizers", "test.TestOutputNormalizer")
                    }
                }
            }
        }
    }
}

tasks.named("test", Test::class) {
    useJUnitPlatform()
}
