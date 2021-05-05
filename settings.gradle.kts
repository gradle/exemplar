plugins {
    id("com.gradle.enterprise").version("3.6.1")
    id("com.gradle.enterprise.gradle-enterprise-conventions-plugin").version("0.7.2")
}

rootProject.name = "exemplar"

include("sample-discovery")
include("sample-check")
include("docs")
