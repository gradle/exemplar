plugins {
    id("exemplar.java-conventions")
}

dependencies {
    implementation(project(":samples-check"))
}

tasks.test {
    useJUnit()
    inputs.file("README.adoc").withPathSensitivity(PathSensitivity.RELATIVE)
}
