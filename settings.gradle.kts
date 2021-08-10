import org.gradle.kotlin.dsl.support.serviceOf

plugins {
    id("com.gradle.enterprise").version("3.6.1")
    id("com.gradle.enterprise.gradle-enterprise-conventions-plugin").version("0.7.2")
}

rootProject.name = "exemplar"

include("discovery")
include("check")
include("docs")

val providers = gradle.serviceOf<ProviderFactory>()

buildCache {
    local {
        isEnabled = true
    }

    val username = providers.environmentVariable("BUILD_CACHE_USERNAME").forUseAtConfigurationTime()
        .orElse(providers.systemProperty("buildCacheUsername").forUseAtConfigurationTime())
        .orNull
    val password = providers.environmentVariable("BUILD_CACHE_PASSWORD").forUseAtConfigurationTime()
        .orElse(providers.systemProperty("buildCachePassword").forUseAtConfigurationTime())
        .orNull
    if (username != null || password != null) {
        remote<HttpBuildCache> {
            url = uri(
                providers.environmentVariable("BUILD_CACHE_URL").forUseAtConfigurationTime()
                    .orElse(providers.systemProperty("buildCacheUrl").forUseAtConfigurationTime())
                    .getOrElse("https://e.grdev.net/cache/")
            )
            isPush = true
            credentials {
                this.username = username
                this.password = password
            }
        }
    }
}