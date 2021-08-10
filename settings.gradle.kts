import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    id("com.gradle.enterprise").version("3.6.1")
    id("com.gradle.enterprise.gradle-enterprise-conventions-plugin").version("0.7.2")
}

rootProject.name = "exemplar"

include("discovery")
include("check")
include("docs")
