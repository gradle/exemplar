/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.samples.test.normalizer

import org.gradle.samples.executor.ExecutionMetadata
import spock.lang.Specification
import spock.lang.Unroll

class GradleOutputNormalizerTest extends Specification {
    def "removes Gradle logs unrelated to the sample itself"() {
        given:
        OutputNormalizer normalizer = new GradleOutputNormalizer()
        String input = """
Starting a Gradle Daemon. Subsequent builds will be faster.
message
message 2

Support for running Gradle using Java 7 has been deprecated and is scheduled to be removed in Gradle 5.0.
        at org.codehaus.groovy.vmplugin.v5.Java5.configureClassNode(Java5.java:397)
        at org.codehaus.groovy.ast.ClassNode.lazyClassInit(ClassNode.java:280)
        at org.codehaus.groovy.ast.ClassNode.getUnresolvedSuperClass(ClassNode.java:1009)

BUILD FAILED in 12s

Deprecated Gradle features were used in this build, making it incompatible with Gradle 5.0. Use '--warning-mode all' to show the individual deprecation warnings.
"""
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        normalizer.normalize(input, executionMetadata) == """
message
message 2


BUILD FAILED in 0s
"""
    }

    @Unroll
    def "can normalize time units [#timeUnit]"(String timeUnit) {
        given:
        OutputNormalizer normalizer = new GradleOutputNormalizer()
        String input = """
            |BUILD FAILED in 512${timeUnit}
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        result.contains('BUILD FAILED in 0s')
        !result.contains("BUILD FAILED in 512${timeUnit}")

        where:
        timeUnit << ['ms', 's', 'm', 'h']
    }

    def "can normalize a long timing"() {
        given:
        OutputNormalizer normalizer = new GradleOutputNormalizer()
        String input = """
            |BUILD FAILED in 5m 3s
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        result.contains('BUILD FAILED in 0s')
        !result.contains("BUILD FAILED in 5m 3s")
    }

    def "can support normalized output without timing"() {
        given:
        OutputNormalizer normalizer = new GradleOutputNormalizer()
        String input = """
            |BUILD SUCCESSFUL
            |2 actionable tasks: 2 executed""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        result.contains('BUILD SUCCESSFUL in 0s')
        !result.contains('BUILD SUCCESSFUL\n')
    }

    @Unroll
    def "can normalize docs.gradle.org URLs [#displayName]"(String displayName, String version, String documentationPath) {
        given:
        OutputNormalizer normalizer = new GradleOutputNormalizer()
        String input = """
            |> Task :documentationUrl
            |Get more help with your project: https://docs.gradle.org/${version}/${documentationPath}
            |
            |BUILD SUCCESSFUL
            |2 actionable tasks: 2 executed""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        result.contains("https://docs.gradle.org/0.0.0/${documentationPath}")
        !result.contains("https://docs.gradle.org/${version}/${documentationPath}")

        where:
        displayName                         | version                   | documentationPath
        'versioned User Manual'             | '5.5.1'                   | 'userguide/userguide.html'
        'versioned API Reference'           | '5.5.1'                   | 'dsl/index.html'
        'versioned Javadoc'                 | '5.5.1'                   | 'javadoc/index.html?overview-summary.html'
        'versioned Release Notes'           | '5.5.1'                   | 'release-notes.html'
        'versioned Samples'                 | '5.5.1'                   | 'samples/index.html'
        'current User Manual'               | 'current'                 | 'userguide/userguide.html'
        'current API Reference'             | 'current'                 | 'dsl/index.html'
        'current Javadoc'                   | 'current'                 | 'javadoc/index.html?overview-summary.html'
        'current Release Notes'             | 'current'                 | 'release-notes.html'
        'current Samples'                   | 'current'                 | 'samples/index.html'
        'nightly User Manual'               | 'nightly'                 | 'userguide/userguide.html'
        'nightly API Reference'             | 'nightly'                 | 'dsl/index.html'
        'nightly Javadoc'                   | 'nightly'                 | 'javadoc/index.html?overview-summary.html'
        'nightly Release Notes'             | 'nightly'                 | 'release-notes.html'
        'nightly Samples'                   | 'nightly'                 | 'samples/index.html'
        'snapshot versioned User Manual'    | '6.2-20191223165223+0000' | 'userguide/userguide.html'
        'snapshot versioned API Reference'  | '6.2-20191223165223+0000' | 'dsl/index.html'
        'snapshot versioned Javadoc'        | '6.2-20191223165223+0000' | 'javadoc/index.html?overview-summary.html'
        'snapshot versioned Release Notes'  | '6.2-20191223165223+0000' | 'release-notes.html'
        'snapshot versioned Samples'        | '6.2-20191223165223+0000' | 'samples/index.html'
    }

    def "can normalize public build scan URL"() {
        given:
        OutputNormalizer normalizer = new GradleOutputNormalizer()
        String input = """
            |BUILD SUCCESSFUL in 6s
            |
            |Publishing build scan...
            |https://gradle.com/s/czajmbyg73t62
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        !result.contains('https://gradle.com/s/czajmbyg73t62')
        result.contains('https://gradle.com/s/feeedfooc00de')
    }

    def "can normalize wrapper download message and progress for official distribution"() {
        given:
        OutputNormalizer normalizer = new GradleOutputNormalizer()
        String input = """
            |Downloading https://services.gradle.org/distributions/gradle-6.0.1-bin.zip
            |.........10%.........20%.........30%..........40%.........50%.........60%..........70%.........80%.........90%..........100%
            |
            |> Task :app:check
            |> Task :app:build
            |
            |BUILD SUCCESSFUL in 0s
            |55 actionable tasks: 1 executed, 54 up-to-date
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        !result.contains('Downloading https://services.gradle.org/distributions/gradle-6.0.1-bin.zip')
        !result.contains('.........10%')
    }

    def "can normalize wrapper download message and progress for snapshot distribution"() {
        given:
        OutputNormalizer normalizer = new GradleOutputNormalizer()
        String input = """
            |Downloading https://services.gradle.org/distributions-snapshots/gradle-6.2-20191223165223+0000-bin.zip
            |.........10%.........20%.........30%..........40%.........50%.........60%..........70%.........80%.........90%..........100%
            |
            |> Task :app:check
            |> Task :app:build
            |
            |BUILD SUCCESSFUL in 0s
            |55 actionable tasks: 1 executed, 54 up-to-date
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        !result.contains('Downloading https://services.gradle.org/distributions-snapshots/gradle-6.2-20191223165223+0000-bin.zip')
        !result.contains('.........10%')
    }

    def "does not remove trailing new lines"() {
        given:
        OutputNormalizer normalizer = new GradleOutputNormalizer()
        String input = """
            |BUILD SUCCESSFUL in 0s
            |55 actionable tasks: 1 executed, 54 up-to-date
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        result.endsWith('\n')
    }

    def "removes incubating feature messsage"() {
        given:
        OutputNormalizer normalizer = new GradleOutputNormalizer()
        String input = """
            |Partial virtual file system invalidation is an incubating feature.
            |> Task :compileJava
            |> Task :processResources NO-SOURCE
            |> Task :classes
            |
            |> Task :run
            |Hello, World!
            |
            |BUILD SUCCESSFUL in 0s
            |2 actionable tasks: 2 executed
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        !result.contains('Partial virtual file system invalidation is an incubating feature.')
    }
}
