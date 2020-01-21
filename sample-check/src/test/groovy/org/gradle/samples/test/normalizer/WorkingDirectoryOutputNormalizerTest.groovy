package org.gradle.samples.test.normalizer

import org.gradle.samples.executor.ExecutionMetadata
import spock.lang.Specification
import spock.lang.Subject

@Subject(WorkingDirectoryOutputNormalizer)
class WorkingDirectoryOutputNormalizerTest extends Specification {
    def "can remove working path"() {
        given:
        OutputNormalizer normalizer = new WorkingDirectoryOutputNormalizer()
        String input = """
            |Some output with a temporary path: /private/var/folders/rg/y7myh0qj1sd58f02v_tgypvh0000gn/T/exemplar1868731005284620663/demo/build/classes/java/main
            |BUILD SUCCESSFUL
            |2 actionable tasks: 2 executed
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(new File('/private/var/folders/rg/y7myh0qj1sd58f02v_tgypvh0000gn/T/exemplar1868731005284620663'), [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        !result.contains('/private/var/folders/rg/y7myh0qj1sd58f02v_tgypvh0000gn/T/exemplar1868731005284620663')
        result.contains('/working-directory/demo/build/classes/java/main')
    }

    def "does not remove leading new lines"() {
        given:
        OutputNormalizer normalizer = new WorkingDirectoryOutputNormalizer()
        String input = """
            |BUILD SUCCESSFUL
            |2 actionable tasks: 2 executed
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(new File('/private/var/folders/rg/y7myh0qj1sd58f02v_tgypvh0000gn/T/exemplar1868731005284620663'), [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        result.startsWith('\n')
    }

    def "does not remove trailing new lines"() {
        given:
        OutputNormalizer normalizer = new WorkingDirectoryOutputNormalizer()
        String input = """
            |Some output with a temporary path: /private/var/folders/rg/y7myh0qj1sd58f02v_tgypvh0000gn/T/exemplar1868731005284620663/demo/build/classes/java/main
            |BUILD SUCCESSFUL
            |2 actionable tasks: 2 executed
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(new File('/private/var/folders/rg/y7myh0qj1sd58f02v_tgypvh0000gn/T/exemplar1868731005284620663'), [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        result.endsWith('\n')
    }
}
