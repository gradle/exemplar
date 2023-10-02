plugins {
    id("com.gradle.enterprise") version "3.15"
    id("io.github.gradle.gradle-enterprise-conventions-plugin") version "0.7.6"
}

rootProject.name = "exemplar"

include("samples-discovery")
include("samples-check")
include("docs")
