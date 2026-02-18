plugins {
    id("exemplar.java-conventions")
}

dependencies {
    testImplementation(project(":samples-check"))
    testImplementation(libs.junit4)
}

tasks.test {
    useJUnit()
    inputs.file("README.adoc").withPathSensitivity(PathSensitivity.RELATIVE)
}
