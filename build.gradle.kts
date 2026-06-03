plugins {
    alias(libs.plugins.nexus.publish)
}

group = "org.gradle.exemplar"
version = "2.0.0"

nexusPublishing {
    repositories.apply {
        sonatype {
            nexusUrl.set(uri("https://ossrh-staging-api.central.sonatype.com/service/local/"))
            snapshotRepositoryUrl.set(uri("https://central.sonatype.com/repository/maven-snapshots/"))
            username.set(System.getenv("MAVEN_CENTRAL_STAGING_REPO_USER"))
            password.set(System.getenv("MAVEN_CENTRAL_STAGING_REPO_PASSWORD"))
        }
    }
}
