package org.gradle.exemplar.test.normalizer

import org.gradle.exemplar.executor.ExecutionMetadata
import spock.lang.Specification
import spock.lang.Subject

@Subject(GradleTaskFailureBuildFileNormalizer)
class GradleTaskFailureBuildFileNormalizerTest extends Specification {
    def "normalizes .gradle.kts on task failure line"() {
        given:
        OutputNormalizer normalizer = new GradleTaskFailureBuildFileNormalizer()
        String input = "Execution failed for task ':foo' (registered in build file 'build.gradle.kts')."
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        normalizer.normalize(input, executionMetadata) == "Execution failed for task ':foo' (registered in build file 'build.gradle')."
    }

    def "handles arbitrary file names"() {
        given:
        OutputNormalizer normalizer = new GradleTaskFailureBuildFileNormalizer()
        String input = "Execution failed for task ':bar' (registered in build file 'custom-plugin.gradle.kts')."
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        normalizer.normalize(input, executionMetadata) == "Execution failed for task ':bar' (registered in build file 'custom-plugin.gradle')."
    }

    def "handles deep paths"() {
        given:
        OutputNormalizer normalizer = new GradleTaskFailureBuildFileNormalizer()
        String input = "Execution failed for task ':sub:task' (registered in build file '/a/b/c/build.gradle.kts')."
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        normalizer.normalize(input, executionMetadata) == "Execution failed for task ':sub:task' (registered in build file '/a/b/c/build.gradle')."
    }

    def "handles included build paths"() {
        given:
        OutputNormalizer normalizer = new GradleTaskFailureBuildFileNormalizer()
        String input = "Execution failed for task ':included-build:compileJava' (registered in build file '/project/included-build/build.gradle.kts')."
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        normalizer.normalize(input, executionMetadata) == "Execution failed for task ':included-build:compileJava' (registered in build file '/project/included-build/build.gradle')."
    }

    def "only replaces .gradle.kts when delimited by single quotes"() {
        given:
        OutputNormalizer normalizer = new GradleTaskFailureBuildFileNormalizer()
        String input = "Execution failed for task ':foo' because something.gradle.kts was bad."
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        normalizer.normalize(input, executionMetadata) == "Execution failed for task ':foo' because something.gradle.kts was bad."
    }

    def "leaves non-failure lines untouched"() {
        given:
        OutputNormalizer normalizer = new GradleTaskFailureBuildFileNormalizer()
        String input = "Build file '/path/to/build.gradle.kts' line: 5"
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        normalizer.normalize(input, executionMetadata) == "Build file '/path/to/build.gradle.kts' line: 5"
    }

    def "leaves .gradle without .kts untouched"() {
        given:
        OutputNormalizer normalizer = new GradleTaskFailureBuildFileNormalizer()
        String input = "Execution failed for task ':foo' (registered in build file 'build.gradle')."
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        normalizer.normalize(input, executionMetadata) == "Execution failed for task ':foo' (registered in build file 'build.gradle')."
    }

    def "handles mixed output"() {
        given:
        OutputNormalizer normalizer = new GradleTaskFailureBuildFileNormalizer()
        String input = """\
            |> Task :foo FAILED
            |Execution failed for task ':foo' (registered in build file '/path/build.gradle.kts').
            |Build file '/path/build.gradle.kts' line: 5""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        result.contains("> Task :foo FAILED")
        result.contains("Execution failed for task ':foo' (registered in build file '/path/build.gradle').")
        result.contains("Build file '/path/build.gradle.kts' line: 5")
    }

    def "does not remove leading new lines"() {
        given:
        OutputNormalizer normalizer = new GradleTaskFailureBuildFileNormalizer()
        String input = """
            |Execution failed for task ':foo' (registered in build file 'build.gradle.kts').
            |BUILD FAILED""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        result.startsWith('\n')
    }

    def "does not remove trailing new lines"() {
        given:
        OutputNormalizer normalizer = new GradleTaskFailureBuildFileNormalizer()
        String input = """
            |Execution failed for task ':foo' (registered in build file 'build.gradle.kts').
            |BUILD FAILED
            |""".stripMargin()
        ExecutionMetadata executionMetadata = new ExecutionMetadata(null, [:])

        expect:
        def result = normalizer.normalize(input, executionMetadata)
        result.endsWith('\n')
    }
}
