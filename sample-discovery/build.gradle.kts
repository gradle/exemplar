plugins {
    id("exemplar.java-conventions")
    id("exemplar.publishing-conventions")
    id("groovy")
}

dependencies {
    compileOnly(Libraries.JSR305)
    implementation(Libraries.ASCIIDOCTORJ)
    implementation(Libraries.COMMONS_IO)
    implementation(Libraries.COMMONS_LANG3)
    implementation(Libraries.TYPESAFE_CONFIG)
    testImplementation(Libraries.SPOCK_CORE)
    testImplementation(Libraries.GROOVY)
}
