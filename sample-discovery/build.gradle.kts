plugins {
    groovy
}

dependencies {
    compileOnly(Libraries.JSR305)
    implementation(Libraries.ASCIIDOCTORJ)
    implementation(Libraries.ASCIIDOCTORJ_API)
    implementation(Libraries.COMMONS_IO)
    implementation(Libraries.COMMONS_LANG3)
    implementation(Libraries.TYPESAFE_CONFIG)
    testCompile(Libraries.SPOCK_CORE)
}
