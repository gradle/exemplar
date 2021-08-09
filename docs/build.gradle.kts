plugins {
    id("exemplar.java-conventions")
}

dependencies {
    implementation(project(":check"))
}

tasks.test {
    inputs.file("README.adoc").withPathSensitivity(PathSensitivity.RELATIVE)
}
