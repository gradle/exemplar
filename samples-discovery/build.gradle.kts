plugins {
    id("exemplar.java-conventions")
    id("exemplar.publishing-conventions")
}

dependencies {
    api(libs.asciidoctorj.api)
    compileOnly(libs.jsr305)
    implementation(libs.commons.io)
    implementation(libs.commons.lang3)
    implementation(libs.typesafe.config)

    runtimeOnly(libs.asciidoctorj)

    testImplementation(libs.groovy)
    testImplementation(libs.junit4)
    testImplementation(libs.spock.core)

    testRuntimeOnly(libs.spock.junit4)
    testRuntimeOnly(libs.junit.launcher)
}
