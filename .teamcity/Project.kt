import jetbrains.buildServer.configs.kotlin.v2019_2.AbsoluteId
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.CheckoutMode
import jetbrains.buildServer.configs.kotlin.v2019_2.Project
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.commitStatusPublisher
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs


object Project : Project({
    buildType(Verify)
    buildType(Release)
    params {
        param("env.GRADLE_ENTERPRISE_ACCESS_KEY", "%ge.gradle.org.access.key%")
    }
})

object Verify : BuildType({
    id = AbsoluteId("VerifyGradleExemplar")
    uuid = "VerifyGradleExemplar"
    name = "Verify Gradle Exemplar"
    description = "Verify Gradle Exemplar"

    vcs {
        root(AbsoluteId("GradleExemplar_Master"))

        checkoutMode = CheckoutMode.ON_AGENT
        cleanCheckout = true
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
    }

    triggers {
        vcs {
            branchFilter = """
    +:refs/heads/*
""".trimIndent()
        }
    }

    steps {
        gradle {
            useGradleWrapper = true
            tasks = "check"
            buildFile = "build.gradle.kts"
        }
    }

    features {
        commitStatusPublisher {
            vcsRootExtId = "GradleExemplar_Master"
            publisher = github {
                githubUrl = "https://api.github.com"
                authType = personalToken {
                    token = "%github.bot-teamcity.token%"
                }
            }
        }
    }
})

object Release : BuildType({
    id = AbsoluteId("ReleaseGradleExemplar")
    uuid = "ReleaseGradleExemplar"
    name = "Release Gradle Exemplar"
    description = "Release Gradle Exemplar"

    vcs {
        root(AbsoluteId("GradleExemplar_Master"))

        checkoutMode = CheckoutMode.ON_AGENT
        cleanCheckout = true
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
    }

    steps {
        gradle {
            useGradleWrapper = true
            gradleParams = "-Dgradle.publish.skip.namespace.check=true"
            tasks = "publishMavenJavaPublicationToSonatypeRepository"
            buildFile = "build.gradle.kts"
        }
    }
    params {
        param("env.JAVA_HOME", "%linux.java8.oracle.64bit%")
        param("env.GRADLE_CACHE_USERNAME", "%gradle.cache.remote.username%")
        password("env.GRADLE_CACHE_PASSWORD", "%gradle.cache.remote.password%")
        param("env.MAVEN_CENTRAL_STAGING_REPO_USER", "%mavenCentralStagingRepoUser%")
        password("env.MAVEN_CENTRAL_STAGING_REPO_PASSWORD", "%mavenCentralStagingRepoPassword%")
    }
})
