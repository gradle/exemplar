plugins {
    id("com.gradle.develocity") version "4.3.2"
    id("io.github.gradle.develocity-conventions-plugin") version "0.13.0"
}

rootProject.name = "exemplar"

include("samples-discovery")
include("samples-check")
include("docs")
