plugins {
    id("exemplar.java-conventions")
}

dependencies {
    implementation(project(":sample-check"))
}

tasks.test {
    inputs.file("README.adoc").withPathSensitivity(PathSensitivity.RELATIVE)
}
