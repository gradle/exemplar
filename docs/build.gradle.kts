plugins {
    id("exemplar.java-conventions")
}

dependencies {
    implementation(project(":samples-check"))
    implementation(Libraries.JUNIT)
}

tasks.test {
    useJUnit()
    inputs.file("README.adoc").withPathSensitivity(PathSensitivity.RELATIVE)
}
