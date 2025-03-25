plugins {
    id("exemplar.java-conventions")
}

dependencies {
    implementation(project(":samples-check"))
}

tasks.test {
    inputs.file("README.adoc").withPathSensitivity(PathSensitivity.RELATIVE)
}
