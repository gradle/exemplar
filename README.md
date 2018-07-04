# Exemplar - check and present samples for CLI tools

Given a collection of sample projects, this library allows you to verify the samples' output.

It does this by discovering, executing, then verifying output of configured sample projects.


## Use cases for sample-check

The intent of sample-check is to ensure that users of your command-line tool see what you expect them to see.
It handles sample discovery, normalization (semantically equivalent output in different environments), and flexible output verification.
It allows any command-line executable on the `PATH` to be invoked. You are not limited to Gradle or Java.

While this has an element of integration testing, it is not meant to replace your integration tests unless the logging output is the only result of executing your tool.
One cannot verify other side effects of invoking the samples, such as files created or their contents, unless that is explicitly configured.

This library is used to verify the functionality of samples in [Gradle documentation](https://docs.gradle.org).


## Usage

 - [Get sample-check](#get-sample-check)
 - [Configure samples](#configure-samples)
 - [Verify samples](#verify-samples)
 - [.sample.conf reference](#sample-conf-fields)
 - [Output normalization](#output-normalization)


### Get sample-check

First things first, you can pull this library down from Gradle's artifactory repository. Here's some [Gradle Kotlin DSL](https://github.com/gradle/kotlin-dsl) configuration:

```gradle
plugins {
    id("java-library")
}

repositories {
    maven {
        url = uri("https://repo.gradle.org/gradle/libs")
        // `url "https://repo.gradle.org/gradle/libs"` will do for Groovy scripts
    }
}

dependencies {
    implementation("org.gradle:sample-check:0.1.0")
}
```


### Configure samples

**NOTE: There are a bunch of (tested) samples under `test/samples` of this repository you can use to understand ways to configure samples.**  

You can configure a sample to be tested by creating a file ending with `.sample.conf` (e.g. `hello-world.sample.conf`) in a sample project dir. 
This is a file in [HOCON format](https://github.com/lightbend/config/blob/master/HOCON.md) that might look something like this:

```hocon
# test/samples/cli/quickstart/quickstart.sample.conf

executable: bash
args: sample.sh
expected-output-file: quickstart.sample.out
```

... or maybe a more complex, multi-step sample:

```hocon
# test/samples/gradle/multi-step-sample/incrementalTaskRemovedOutput.sample.conf

commands: [{
  executable: gradle
  args: originalInputs incrementalReverse
  expected-output-file: originalInputs.out
  allow-additional-output: true
}, {
  executable: gradle
  args: removeOutput incrementalReverse
  flags: --quiet
  expected-output-file: incrementalTaskRemovedOutput.out
  allow-disordered-output: true
}]
```

_See [Sample Conf fields](#sample-conf-fields) for a detailed description of all the possible options._


### Verify samples

You can verify samples either through one of the [JUnit Test Runners](#verifying-using-a-junit-runner) or use the API.

#### Verifying using a JUnit Runner

This library provides 2 JUnit runners [`SamplesRunner`](src/main/java/org/gradle/samples/test/runner/SamplesRunner.java) (executes via CLI) and [`GradleSamplesRunner`](src/main/java/org/gradle/samples/test/runner/GradleSamplesRunner.java) (executes samples using [Gradle's Tooling API](https://docs.gradle.org/current/userguide/embedding.html))

**NOTE:** `GradleSamplesRunner` supports Java 8 and above and ignores tests when running on Java 7 or lower.

**NOTE:** If you are using `GradleSamplesRunner`, you will need to add `gradleTestKit()` as a dependency as well:

```kotlin
dependencies {
    testImplementation(gradleTestKit())
}
```

To use them, just create a JUnit test class in your test sources (maybe something like `src/integTest/com/example/SamplesIntegrationTest.java`, [keeping these slow tests separate](https://docs.gradle.org/current/userguide/java_testing.html#sec:configuring_java_integration_tests) from your fast unit tests.) and annotate it with which JUnit runner implementation you'd like and where to find samples. 
Like this:

```java
package com.example;

import org.gradle.samples.test.runner.GradleSamplesRunner;

@RunWith(GradleSamplesRunner.class)
@SamplesRoot("src/docs/samples")
public class SamplesIntegrationTest {
}
```

When you run this test, it will search recursively under the samples root directory (`src/docs/samples` in this example) for any file with a `*.sample.conf` suffix. 
Any directory found to have one of these will be treated as a sample project dir (nesting sample projects is allowed).
The test runner will copy each sample project to a temporary location, invoke the configured commands, and capture and verify logging output.

#### Verifying using the API

Use of the JUnit runners is preferred, as discovery, output normalization, and reporting are handled for you. If you want to write custom samples verification or you're using a different test framework, by all means go ahead :) — please contribute back runners or normalizers you find useful!

You can get some inspiration for API use from [SamplesRunner](src/main/java/org/gradle/samples/test/runner/SamplesRunner.java) and [GradleSamplesRunner](src/main/java/org/gradle/samples/test/runner/GradleSamplesRunner.java).

Command execution is handled in the `org.gradle.samples.executor.*` classes, some output normalizers are provided in the `org.gradle.samples.test.normalizer` package, and output verification is handled by classes in the `org.gradle.samples.test.verifier` package.


### Sample conf fields

One of `executable` or `commands` are required at the root. 
If `executable` is found, the sample will be considered a single-command sample.
Otherwise, `commands` is expected to be an Array of [Commands](../sample-discovery/src/main/java/org/gradle/samples/model/Command.java):

 - repeated Command `commands` — An array of commands to run, in order.

A [Command](../sample-discovery/src/main/java/org/gradle/samples/model/Command.java) is specified with these fields.

 - required string `executable` — Executable to invoke.
 - optional string `execution-subdirectory` — Working directory in which to invoke the executable. _If not specified, the API assumes `./` (the directory the sample config file is in)._
 - optional string `args` — Arguments for executable. Default is `""`.
 - optional string `flags` — CLI flags (separated for tools that require these be provided in a certain order). Default is `""`.
 - optional string `expected-output-file` — Relative path from sample config file to a readable file to compare actual output to. Default is `null`. _If not specified, output verification is not performed._
 - optional boolean `expect-failure` — Invoking this command is expected to produce a non-zero exit code. Default: `false`.
 - optional boolean `allow-additional-output` — Allow extra lines in actual output. Default: `false`.
 - optional boolean `allow-disordered-output` — Allow output lines to be in any sequence. Default: `false`.


### Output normalization

sample-check allows actual output to be normalized in cases where output is semantically equivalent. 
You can use normalizers by annotating your JUnit test class with `@SamplesOutputNormalizers` and specifying which normalizers (in order) you'd like to use.
 
```java
@SamplesOutputNormalizers([JavaObjectSerializationOutputNormalizer.class, FileSeparatorOutputNormalizer.class])
```

Custom normalizers must implement the `org.gradle.samples.test.OutputNormalizer` interface. The two above are included in sample-check.


### Custom Gradle installation

To allow Gradle itself to run using test versions of Gradle, the `GradleSamplesRunner` allows a custom installation to be injected using the system property "integTest.gradleHomeDir".

