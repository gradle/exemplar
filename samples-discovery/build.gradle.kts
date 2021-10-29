plugins {
    id("exemplar.java-conventions")
    id("exemplar.publishing-conventions")
}

dependencies {
    compileOnly(libs.jsr305)
    implementation(libs.asciidoctorj)
    implementation(libs.commons.io)
    implementation(libs.commons.lang3)
    implementation(libs.typesafe.config)
    testImplementation(libs.groovy)
    testImplementation(libs.bundles.spock)
}
