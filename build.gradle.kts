plugins {
    alias(libs.plugins.nexus.publish)
}

group = "org.gradle.exemplar"
version = "2.0.0"

tasks.named<UpdateDaemonJvm>("updateDaemonJvm").configure {
    toolchainDownloadUrls.empty()
}

nexusPublishing {
    repositories.apply {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("MAVEN_CENTRAL_STAGING_REPO_USER"))
            password.set(System.getenv("MAVEN_CENTRAL_STAGING_REPO_PASSWORD"))
        }
    }
}
