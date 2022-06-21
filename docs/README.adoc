= Exemplar - check and present samples for CLI tools
:toc:
:toc-placement!:

Given a collection of sample projects, this library allows you to verify the samples' output.

It does this by discovering, executing, then verifying output of sample projects in a separate directory or embedded within asciidoctor. In fact, the samples embedded here are verified by `ReadmeTest`.

toc::[]

== Use cases

The intent of the `samples-check` module is to ensure that users of your command-line tool see what you expect them to see.
It handles sample discovery, normalization (semantically equivalent output in different environments), and flexible output verification.
It allows any command-line executable on the `PATH` to be invoked. You are not limited to Gradle or Java.

While this has an element of integration testing, it is not meant to replace your integration tests unless the logging output is the only result of executing your tool.
One cannot verify other side effects of invoking the samples, such as files created or their contents, unless that is explicitly configured.

This library is used to verify the functionality of samples in https://docs.gradle.org[Gradle documentation].

== Installation

First things first, you can pull this library down from Gradle's artifactory repository. This https://github.com/gradle/kotlin-dsl[Gradle Kotlin DSL] script shows one way to do just that.

.Installing with Gradle
[.testable-sample]
====

.build.gradle.kts
[source,kotlin]
----
plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.gradle.exemplar:samples-check:1.0.0")
}
----

[.sample-command,allow-additional-output=true]
----
$ gradle check
----

====

== Usage

=== Configuring external samples

An external sample consists of a directory containing all the files required to run that sample.
It may include link:https://asciidoctor.org/docs/user-manual/#include-partial[tagged content regions] that can be extracted into documentation.

You can configure a sample to be tested by creating a file ending with `.sample.conf` (e.g. `hello-world.sample.conf`) in a sample project dir.
This is a file in https://github.com/lightbend/config/blob/master/HOCON.md[HOCON format] that might look something like this:

.quickstart.sample.conf
[source,hocon]
----
executable: bash
args: sample.sh
expected-output-file: quickstart.sample.out
----

or maybe a more complex, multi-step sample:

.incrementalTaskRemovedOutput.sample.conf
[source,hocon]
----
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
----

When there are multiple steps specified for a sample, they are run one after the other, in the order specified. The 'executable' can be either a command on the `$PATH`, or `gradle` (to run Gradle), or `cd` (to change the working directory for subsequent steps).

_See <<sample-conf-fields,Sample Conf fields>> for a detailed description of all the possible options._

*NOTE:* There are a bunch of (tested) samples under `samples-check/src/test/samples` of this repository you can use to understand ways to configure samples.

=== Configuring embedded samples

An embedded sample is one in which the source for the sample is written directly within an link:https://asciidoctor.org/[Asciidoctor] source file.

Use this syntax to allow samples-discovery to extract your sources from the doc, execute the `sample-command`, and verify the output matches what is declared in the doc.

[source,adoc]
----
.Sample title
[.testable-sample]       // <1>
====

.hello.rb                // <2>
[source,ruby]            // <3>
-----
puts "hello, #{ARGV[0]}" // <4>
-----

[.sample-command]        // <5>
-----
$ ruby hello.rb world    // <6>
hello, world             // <7>
-----

====
----
<1> Mark blocks containing your source files with the role `testable-sample`
<2> The title of each source block should be the name of the source file
<3> All source blocks with a title are extracted to a temporary directory
<4> Source code. This can be `include::`d
<5> Exemplar will execute the commands in a block with role `sample-command`. There can be multiple blocks.
<6> Terminal commands should start with "$ ". Everything after the "$ " is treated as a command to run. There can be multiple commands in a block.
<7> One or more lines of expected output

[NOTE] All sources have to be under the same block, and you must set the title of source blocks to a valid file name.

=== Verify samples

You can verify samples either through one of the <<verifying-using-a-junit-runner,JUnit Test Runners>> or use the API.

==== Verifying using a JUnit Runner

This library provides 2 JUnit runners link:../samples-check/src/main/java/org/gradle/exemplar/test/runner/SamplesRunner.java[`SamplesRunner`] (executes via CLI) and link:../samples-check/src/main/java/org/gradle/exemplar/test/runner/GradleSamplesRunner.java[`GradleSamplesRunner`] (executes samples using https://docs.gradle.org/current/userguide/test_kit.html[Gradle TestKit]). If you are using `GradleSamplesRunner`, you will need to add `gradleTestKit()` and SLF4J binding dependencies as well:

[source,kotlin]
----
dependencies {
    testImplementation(gradleTestKit())
    testRuntimeOnly("org.slf4j:slf4j-simple:1.7.16")
}
----

*NOTE:* `GradleSamplesRunner` supports Java 8 and above and ignores tests when running on Java 7 or lower.

To use them, just create a JUnit test class in your test sources (maybe something like `src/integTest/com/example/SamplesIntegrationTest.java`, https://docs.gradle.org/current/userguide/java_testing.html#sec:configuring_java_integration_tests[keeping these slow tests separate] from your fast unit tests.) and annotate it with which JUnit runner implementation you'd like and where to find samples.
Like this:

// NOTE: inception bites us if we try to turn this into a testable sample.
.SamplesRunnerIntegrationTest.java
[source,java]
----
package com.example;

import org.junit.runner.RunWith;
import org.gradle.exemplar.test.runner.GradleSamplesRunner;
import org.gradle.exemplar.test.runner.SamplesRoot;

@RunWith(GradleSamplesRunner.class)
@SamplesRoot("src/docs/samples")
public class SamplesIntegrationTest {
}
----

When you run this test, it will search recursively under the samples root directory (`src/docs/samples` in this example) for any file with a `*.sample.conf` suffix.
Any directory found to have one of these will be treated as a sample project dir (nesting sample projects is allowed).
The test runner will copy each sample project to a temporary location, invoke the configured commands, and capture and verify logging output.

==== Verifying using the API

Use of the JUnit runners is preferred, as discovery, output normalization, and reporting are handled for you. If you want to write custom samples verification or you're using a different test framework, by all means go ahead :) -- please contribute back runners or normalizers you find useful!

You can get some inspiration for API use from link:https://github.com/gradle/exemplar/blob/main/samples-check/src/main/java/org/gradle/exemplar/test/runner/SamplesRunner.java[SamplesRunner] and link:https://github.com/gradle/exemplar/blob/main/samples-check/src/main/java/org/gradle/exemplar/test/runner/GradleSamplesRunner.java[GradleSamplesRunner].

Command execution is handled in the `org.gradle.exemplar.executor.*` classes, some output normalizers are provided in the `org.gradle.exemplar.test.normalizer` package, and output verification is handled by classes in the `org.gradle.exemplar.test.verifier` package.

=== Using Samples for other integration tests

You might want to verify more than just log output, so this library includes link:https://github.com/junit-team/junit4/wiki/rules[JUnit rules] that allow you to easily copy sample projects to a temporarily location for other verification. Here is an example of a test that demonstrates use of the `@Sample` and `@UsesSample` rules.

.BasicSampleTest.java
[source,java]
----
package com.example;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.gradle.exemplar.test.rule.Sample;
import org.gradle.exemplar.test.rule.UsesSample;

public class BasicSampleTest {
    public TemporaryFolder temporaryFolder = new TemporaryFolder()
    public Sample sample = Sample.from("src/test/samples/gradle")
            .into(temporaryFolder)
            .withDefaultSample("basic-sample")

    @Rule
    public TestRule ruleChain = RuleChain.outerRule(temporaryFolder).around(sample)

    @Test
    void verifyDefaultSample() {
        assert sample.getDir() == new File(temporaryFolder.getRoot(), "samples/basic-sample");
        assert sample.getDir().isDirectory();
        assert new File(sample.getDir(), "build.gradle").isFile();

        // TODO(You): Execute what you wish in the sample project
        // TODO(You): Verify file contents or whatever you want
    }

    @Test
    @UsesSample("composite-sample/basic")
    void verifyOtherSample() {
        // TODO(You): Utilize sample project under samples/composite-sample/basic
    }
}
----

=== External sample.conf reference

One of `executable` or `commands` are required at the root.
If `executable` is found, the sample will be considered a single-command sample.
Otherwise, `commands` is expected to be an Array of link:https://github.com/gradle/exemplar/blob/main/samples-discovery/src/main/java/org/gradle/exemplar/model/Command.java[Commands]:

* repeated Command `commands` -- An array of commands to run, in order.

A link:https://github.com/gradle/exemplar/blob/main/samples-discovery/src/main/java/org/gradle/exemplar/model/Command.java[Command] is specified with these fields.

* required string `executable` -- Executable to invoke.
* optional string `execution-subdirectory` -- Working directory in which to invoke the executable. _If not specified, the API assumes `./` (the directory the sample config file is in)._
* optional string `args` -- Arguments for executable. Default is `""`.
* optional string `flags` -- CLI flags (separated for tools that require these be provided in a certain order). Default is `""`.
* optional string `expected-output-file` -- Relative path from sample config file to a readable file to compare actual output to. Default is `null`. _If not specified, output verification is not performed._
* optional boolean `expect-failure` -- Invoking this command is expected to produce a non-zero exit code. Default: `false`.
* optional boolean `allow-additional-output` -- Allow extra lines in actual output. Default: `false`.
* optional boolean `allow-disordered-output` -- Allow output lines to be in any sequence. Default: `false`.

=== Output normalization

samples-check allows actual output to be normalized in cases where output is semantically equivalent.
You can use normalizers by annotating your JUnit test class with `@SamplesOutputNormalizers` and specifying which normalizers (in order) you'd like to use.

[source,java]
----
@SamplesOutputNormalizers({JavaObjectSerializationOutputNormalizer.class, FileSeparatorOutputNormalizer.class, GradleOutputNormalizer.class})
----

Custom normalizers must implement the link:https://github.com/gradle/exemplar/blob/main/samples-check/src/main/java/org/gradle/exemplar/test/normalizer/OutputNormalizer.java[`OutputNormalizer`] interface. The two above are included in check.

=== Common sample modification

samples-check supports modifying all samples before they are executed by implementing the link:https://github.com/gradle/exemplar/blob/main/samples-check/src/main/java/org/gradle/exemplar/test/runner/SampleModifier.java[`SampleModifier`] interface and declaring link:https://github.com/gradle/exemplar/blob/main/samples-check/src/main/java/org/gradle/exemplar/test/runner/SampleModifiers.java[`SampleModifiers`].
This allows you to do things like set environment properties, change the executable or arguments, and even conditionally change verification based on some logic.
For example, you might prepend a `Command` that sets up some environment before other commands are run or change `expect-failure` to `true` if you know verification conditionally won't work on Windows.

[source,java]
----
@SampleModifiers({SetupEnvironmentSampleModifier.class, ExtraCommandArgumentsSampleModifier.class})
----

=== Custom Gradle installation

To allow Gradle itself to run using test versions of Gradle, the `GradleSamplesRunner` allows a custom installation to be injected using the system property "integTest.gradleHomeDir".

== Contributing

[link=https://builds.gradle.org/viewType.html?buildTypeId=Build_Tool_Services_Exemplar]
image::https://builds.gradle.org/guestAuth/app/rest/builds/buildType:(id:Build_Tool_Services_Exemplar)/statusIcon.svg[Build status]

[link=https://gradle.org/conduct/]
image::https://img.shields.io/badge/code%20of-conduct-lightgrey.svg?style=flat&colorB=ff69b4[code of conduct]

== Releasing

1. Change the version number in the root build script to the version you want to release.
1. Add missing changes to the <<Changes>> section below.
1. Push the changes to `main` branch.
1. Run the https://builds.gradle.org/buildConfiguration/Build_Tool_Services_Exemplar_Verify[Verify Exemplar] job on the commit you
want to release.
1. Run the https://builds.gradle.org/buildConfiguration/Build_Tool_Services_Exemplar_Publish[Publish Exemplar] job on the commit you
want to release. This job publishes everything to the Maven Central staging repository.
1. https://s01.oss.sonatype.org/#stagingRepositories[Login to Sonatype], close the staging repository after reviewing
its contents.
1. Release the staging repository.
1. Tag the commit you just release with the version number `git tag -s VERSION -m "Tag VERSION release" && git push --tags`
1. Go to the https://github.com/gradle/exemplar/releases[Releases section on GitHub] and create a new release using the tag you just pushed. Copy the release notes from the <<Changes>> section into the release description.
1. Bump the version in the root build script to the next snapshot version. Push the change to `main` branch.

== Changes

=== 1.0.0

- Publish all artifacts to the Maven Central repository under `org.gradle.exemplar` group
- Renamed modules from `sample-check` and `sample-discovery` to `samples-check` and `samples-discovery`
- Changed root package name from `org.gradle.samples` to `org.gradle.exemplar`

=== 0.12.6

- `AsciidoctorCommandsDiscovery` now support `user-inputs` for providing inputs to a command
- Introduce a output normalizer removing leading new lines

=== 0.12.5

- `GradleOutputNormalizer` can normalize incubating feature message

=== 0.12.4

- Ensure output normalizer aren't removing trailing new lines (except for the `TrailingNewLineOutputNormalizer`

=== 0.12.3

- `GradleOutputNormalizer` can normalize snapshot documentation urls

=== 0.12.2

- `GradleOutputNormalizer` can normalize wrapper download message
- `TrailingNewLineOutputNormalizer` can normalize empty output

=== 0.12.1

- `GradleOutputNormalizer` can normalize build scan urls

=== 0.12

- Rename `AsciidoctorAnnotationNormalizer` to `AsciidoctorAnnotationOutputNormalizer`
- Introduce `WorkingDirectoryOutputNormalizer` to normalize paths in the output

=== 0.11.1

- Introduce `AsciidoctorCommandsDiscovery` to only discover commands

=== 0.11

- Downgraded AsciidoctorJ to 1.5.8.1 to play nice with Asciidoctor extension used by Gradle documentation.

Note: The upgrade of AsciidoctorJ will need to be cross-cutting.

=== 0.10

- Fixes to the `AsciidoctorSamplesDiscovery` classes
- Allow configuring the underlying `Asciidoctor` instance used by `AsciidoctorSamplesDiscovery`
- A bunch of out-of-the-box `OutputNormalizer`

=== 0.8

- Handle `cd <dir>` commands, to keep track of the user's working directory and apply it to later commands in the same sample.
