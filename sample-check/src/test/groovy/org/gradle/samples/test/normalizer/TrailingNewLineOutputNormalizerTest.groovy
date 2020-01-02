package org.gradle.samples.test.normalizer

import org.gradle.samples.executor.ExecutionMetadata
import spock.lang.Specification
import spock.lang.Subject

@Subject(TrailingNewLineOutputNormalizer)
class TrailingNewLineOutputNormalizerTest extends Specification {
    def "can remove empty line at the end of the output"() {
        given:
        OutputNormalizer normalizer = new TrailingNewLineOutputNormalizer()
        String input = """
            |BUILD SUCCESSFUL
            |2 actionable tasks: 2 executed
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        !result.endsWith('\n')
    }

    def "can remove multiple empty line at the end of the output"() {
        given:
        OutputNormalizer normalizer = new TrailingNewLineOutputNormalizer()
        String input = """
            |BUILD SUCCESSFUL
            |2 actionable tasks: 2 executed
            |
            |
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        !result.endsWith('\n')
    }
}
