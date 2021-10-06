plugins {
    id("exemplar.java-conventions")
}

dependencies {
    implementation(project(":samples-check"))
    implementation(Libraries.JUNIT)
}

tasks.test {
    inputs.file("README.adoc").withPathSensitivity(PathSensitivity.RELATIVE)
}
