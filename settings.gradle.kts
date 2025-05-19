plugins {
    id("com.gradle.develocity") version "4.0.1"
    id("io.github.gradle.gradle-enterprise-conventions-plugin") version "0.10.3"
}

rootProject.name = "exemplar"

include("samples-discovery")
include("samples-check")
include("docs")
