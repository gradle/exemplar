plugins {
    `build-scan`
}

subprojects {
    group = "org.gradle"
    version = "0.1.0"

    repositories {
        maven {
            url = uri("https://repo.gradle.org/gradle/libs-releases")
        }
        jcenter()
    }
}

buildScan {
    setTermsOfServiceUrl("https://gradle.com/terms-of-service")
    setTermsOfServiceAgree("yes")
    if (!System.getenv("CI").isNullOrEmpty()) {
        publishAlways()
        tag("CI")
    }
}
