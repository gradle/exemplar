plugins {
    id("com.gradle.enterprise") version "3.6.3"
    id("com.gradle.enterprise.gradle-enterprise-conventions-plugin") version "0.7.2"
}

enableFeaturePreview("VERSION_CATALOGS")

rootProject.name = "exemplar"

include("samples-discovery")
include("samples-check")
include("docs")
