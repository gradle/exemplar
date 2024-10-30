plugins {
    id("com.gradle.develocity") version "3.18.1"
    id("io.github.gradle.gradle-enterprise-conventions-plugin") version "0.10.2"
}

rootProject.name = "exemplar"

include("samples-discovery")
include("samples-check")
include("docs")
