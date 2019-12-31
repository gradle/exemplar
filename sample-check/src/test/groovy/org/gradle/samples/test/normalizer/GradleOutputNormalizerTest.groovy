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

    def "can support normalized output without timing"() {
        given:
        OutputNormalizer normalizer = new GradleOutputNormalizer()
        String input = """
            |BUILD SUCCESSFUL
            |Dummy output after result for easy assertion""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        result.contains('BUILD SUCCESSFUL in 0s')
        !result.contains('BUILD SUCCESSFUL\n')
    }
}
