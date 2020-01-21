package org.gradle.samples.test.normalizer

import org.gradle.samples.executor.ExecutionMetadata
import spock.lang.Specification
import spock.lang.Subject

@Subject(TrailingNewLineOutputNormalizer)
class TrailingNewLineOutputNormalizerTest extends Specification {
    def "can remove empty line at the end of the output"() {
        given:
        OutputNormalizer normalizer = new TrailingNewLineOutputNormalizer()
        String input = '''
            |BUILD SUCCESSFUL
            |2 actionable tasks: 2 executed
            |'''.stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        !result.endsWith('\n')
    }

    def "can remove multiple empty line at the end of the output"() {
        given:
        OutputNormalizer normalizer = new TrailingNewLineOutputNormalizer()
        String input = '''
            |BUILD SUCCESSFUL
            |2 actionable tasks: 2 executed
            |
            |
            |'''.stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        !result.endsWith('\n')
    }

    def "can normalize empty output"() {
        given:
        OutputNormalizer normalizer = new TrailingNewLineOutputNormalizer()
        String input = ''
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        when:
        def result = normalizer.normalize(input, executionMetadata)

        then:
        noExceptionThrown()

        and:
        result == ''
    }

    def "can normalize one line of output"() {
        given:
        OutputNormalizer normalizer = new TrailingNewLineOutputNormalizer()
        String input = 'Some output'
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        when:
        def result = normalizer.normalize(input, executionMetadata)

        then:
        noExceptionThrown()

        and:
        result == 'Some output'
    }

    def "does not remove leading new lines"() {
        given:
        OutputNormalizer normalizer = new TrailingNewLineOutputNormalizer()
        String input = """
            |BUILD SUCCESSFUL
            |2 actionable tasks: 2 executed
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        result.startsWith('\n')
    }
}
