plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

version = "1.0.0"

nexusPublishing {
    packageGroup.set("com.gradle")
    repositories.apply {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(System.getenv("MAVEN_CENTRAL_STAGING_REPO_USER"))
            password.set(System.getenv("MAVEN_CENTRAL_STAGING_REPO_PASSWORD"))
        }
    }
}
