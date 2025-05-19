import jetbrains.buildServer.configs.kotlin.AbsoluteId
import jetbrains.buildServer.configs.kotlin.BuildType
import jetbrains.buildServer.configs.kotlin.CheckoutMode
import jetbrains.buildServer.configs.kotlin.Project
import jetbrains.buildServer.configs.kotlin.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.triggers.vcs


object Project : Project({
    buildType(Verify)
    buildType(Publish)
    params {
        param("env.JAVA_HOME", "%linux.java17.openjdk.64bit%")
        param("env.GRADLE_CACHE_REMOTE_URL", "%gradle.cache.remote.url%")
        param("env.GRADLE_CACHE_REMOTE_USERNAME", "%gradle.cache.remote.username%")
        password("env.GRADLE_CACHE_REMOTE_PASSWORD", "%gradle.cache.remote.password%")
        password("env.DEVELOCITY_ACCESS_KEY", "%ge.gradle.org.access.key%")
    }
})

object Verify : BuildType({
    id = AbsoluteId("Build_Tool_Services_Exemplar_Verify")
    uuid = "Build_Tool_Services_Exemplar_Verify"
    name = "Verify Exemplar"
    description = "Verify integrity of Exemplar libraries"

    vcs {
        root(AbsoluteId("Exemplar_Master"))
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
            gradleParams = "-Dgradle.cache.remote.push=%env.BUILD_CACHE_PUSH%"
            buildFile = "build.gradle.kts"
        }
    }
})

object Publish : BuildType({
    id = AbsoluteId("Build_Tool_Services_Exemplar_Publish")
    uuid = "Build_Tool_Services_Exemplar_Publish"
    name = "Publish Exemplar"
    description = "Publish Exemplar libraries to Maven Central staging repository"

    vcs {
        root(AbsoluteId("Exemplar_Master"))
        checkoutMode = CheckoutMode.ON_AGENT
        cleanCheckout = true
    }

    requirements {
        contains("teamcity.agent.jvm.os.name", "Linux")
    }

    steps {
        gradle {
            useGradleWrapper = true
            tasks = "clean publishMavenJavaPublicationToSonatypeRepository"
            gradleParams = "-Dgradle.publish.skip.namespace.check=true"
            buildFile = "build.gradle.kts"
        }
    }
    params {
        param("env.MAVEN_CENTRAL_STAGING_REPO_USER", "%mavenCentralStagingRepoUser%")
        password("env.MAVEN_CENTRAL_STAGING_REPO_PASSWORD", "%mavenCentralStagingRepoPassword%")
    }
})
