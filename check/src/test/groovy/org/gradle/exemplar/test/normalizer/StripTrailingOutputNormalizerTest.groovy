package org.gradle.exemplar.test.normalizer

import org.gradle.exemplar.executor.ExecutionMetadata
import spock.lang.Specification
import spock.lang.Subject

@Subject(StripTrailingOutputNormalizer)
class StripTrailingOutputNormalizerTest extends Specification {
    def "can remove trailing spaces at the end of each output line"() {
        given:
        OutputNormalizer normalizer = new StripTrailingOutputNormalizer()
        String input = """
            |BUILD SUCCESSFUL   
            |2 actionable tasks: 2 executed   
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        (input =~ /[ ]+$/).find()
        !(result =~ /[ ]+$/).find()
    }

    def "does not remove leading new lines"() {
        given:
        OutputNormalizer normalizer = new StripTrailingOutputNormalizer()
        String input = """
            |BUILD SUCCESSFUL
            |2 actionable tasks: 2 executed
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        result.startsWith('\n')
    }

    def "does not remove trailing new lines"() {
        given:
        OutputNormalizer normalizer = new StripTrailingOutputNormalizer()
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
