# Exemplar Gradle Plugin
This is a Gradle Plugin for the [Exemplar project](https://github.com/gradle/exemplar).

## Description
When you apply this plugin it will automatically add all necessary 
dependencies to your project.

So there is no more setup to use Exemplar.

## How to use
Simply apply the plugin inside the `plugins {}` block and you are done:
```kotlin
plugins {
    id("org.gradle.exemplar") version "$VERSION"
}
```
