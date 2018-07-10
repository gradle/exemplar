plugins {
    groovy
}

dependencies {
    compileOnly(Libraries.JSR305)
    implementation(Libraries.ASCIIDOCTOR)
    implementation(Libraries.COMMONS_IO)
    implementation(Libraries.TYPESAFE_CONFIG)
    testCompile(Libraries.SPOCK_CORE)
}
