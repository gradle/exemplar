package org.gradle.samples.test.normalizer

import org.gradle.samples.executor.ExecutionMetadata
import spock.lang.Specification
import spock.lang.Subject

@Subject(LeadingNewLineOutputNormalizer)
class LeadingNewLineOutputNormalizerTest extends Specification {
    def "can normalize empty output"() {
        given:
        OutputNormalizer normalizer = new LeadingNewLineOutputNormalizer()
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
        OutputNormalizer normalizer = new LeadingNewLineOutputNormalizer()
        String input = 'Some output'
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        when:
        def result = normalizer.normalize(input, executionMetadata)

        then:
        noExceptionThrown()

        and:
        result == 'Some output'
    }

    def "does not remove trailing new lines"() {
        given:
        OutputNormalizer normalizer = new LeadingNewLineOutputNormalizer()
        String input = """
            |BUILD SUCCESSFUL
            |2 actionable tasks: 2 executed
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        result.endsWith('\n')
    }
}
