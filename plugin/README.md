# Exemplar Gradle Plugin
This is a Gradle Plugin for the [Exemplar project](https://github.com/gradle/exemplar).

## Description
When you apply this plugin it will automatically add the `org.gradle:sample-check:$VERSION`
dependency to the `testImplementation` configuration.

When you want to execute the tests with the [Gradle TestKit](https://docs.gradle.org/current/userguide/test_kit.html)
we provide a handy **Kotlin extension function** for you.
Just call it from the `dependencies {}` block and we will add all necessary dependencies for you:
```kotlin
import org.gradle.exemplar.gradleExemplar

dependencies {
    gradleExemplar()
}
```  

> **Note:** This is obviously only possible with the [Kotlin DSL](https://github.com/gradle/kotlin-dsl).
When you use the Groovy DSL you have to manually add the following dependencies:
```groovy
dependencies {
    testImplementation(gradleTestKit())
    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.16")
}
```
> Also please check out the [main README](../docs/README.adoc) for it...

## How to use
Simply apply the plugin inside the `plugins {}` block and you are done:
```kotlin
plugins {
    id("org.gradle.exemplar") version "$VERSION"
}
```

As mentioned above - when you want to execute the tests with the **Gradle TestKit** call
either the `gradleExemplar()` extension function inside the `dependencies {}`block (with the Kotlin DSL)
or add the dependencies manually (with the Groovy DSL).
