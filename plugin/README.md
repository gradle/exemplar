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
or resp. for the Groovy DSL:
```groovy
import org.gradle.exemplar.DependencyHandlerExtensionKt

dependencies {
    DependencyHandlerExtensionKt.gradleExemplar(this)
}
```

## How to use
Simply apply the plugin inside the `plugins {}` block and you are done:
```kotlin
plugins {
    id("org.gradle.exemplar") version "$VERSION"
}
```

As mentioned above - when you want to execute the tests with the **Gradle TestKit** simply call
the `gradleExemplar()` extension function inside the `dependencies {}` block.
