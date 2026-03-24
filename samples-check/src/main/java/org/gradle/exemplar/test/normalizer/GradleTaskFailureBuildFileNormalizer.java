package org.gradle.exemplar.test.normalizer;

import org.gradle.exemplar.executor.ExecutionMetadata;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Normalizes Gradle task failure messages by replacing {@code .gradle.kts} build file
 * references with {@code .gradle}, so that Kotlin DSL output matches Groovy DSL expectations.
 * <p>
 * Only lines beginning with {@code "Execution failed for task"} are modified;
 * all other lines pass through unchanged.</p>
 */
public final class GradleTaskFailureBuildFileNormalizer implements OutputNormalizer {
    private static final String TASK_FAILURE_PREFIX = "Execution failed for task";

    @Override
    public String normalize(String commandOutput, ExecutionMetadata executionMetadata) {
        return Arrays.stream(commandOutput.split("\\r?\\n", -1))
            .map(line -> line.startsWith(TASK_FAILURE_PREFIX)
                ? line.replace(".gradle.kts'", ".gradle'")
                : line)
            .collect(Collectors.joining("\n"));
    }
}
