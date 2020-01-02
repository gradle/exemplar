package org.gradle.samples.test.normalizer;

import org.gradle.samples.executor.ExecutionMetadata;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TrailingNewLineOutputNormalizer implements OutputNormalizer {
    @Override
    public String normalize(String commandOutput, ExecutionMetadata executionMetadata) {
        List<String> lines = Arrays.asList(commandOutput.split("\\r?\\n"));

        while (lines.get(lines.size() - 1).isEmpty()) {
            lines.remove(lines.size() - 1);
        }

        return lines.stream().collect(Collectors.joining("\n"));
    }
}
