object Versions {
    val ASCIIDOCTOR = "1.5.6"
    val COMMONS_IO = "2.4"
    val COMMONS_LANG3 = "3.7"
    //    TODO: Junit 5
    val JUNIT = "4.12"
    val JSR305 = "3.0.2"
    val SLF4J = "1.7.16"
    val SPOCK_CORE = "1.1-groovy-2.4"
    val TYPESAFE_CONFIG = "1.3.1"
}

object Libraries {
    val ASCIIDOCTOR = "org.asciidoctor:asciidoctorj:${Versions.ASCIIDOCTOR}"
    val COMMONS_IO = "commons-io:commons-io:${Versions.COMMONS_IO}"
    val COMMONS_LANG3 = "org.apache.commons:commons-lang3:${Versions.COMMONS_LANG3}"
    val JUNIT = "junit:junit:${Versions.JUNIT}"
    val JSR305 = "com.google.code.findbugs:jsr305:${Versions.JSR305}"
    val SLF4J = "org.slf4j:slf4j-simple:${Versions.SLF4J}"
    val SPOCK_CORE = "org.spockframework:spock-core:${Versions.SPOCK_CORE}"
    val TYPESAFE_CONFIG = "com.typesafe:config:${Versions.TYPESAFE_CONFIG}"
}
