package org.gradle.exemplar.test.normalizer;

import org.gradle.exemplar.executor.ExecutionMetadata;

import java.util.Arrays;
import java.util.stream.Collectors;

public class TrailingNewLineOutputNormalizer implements OutputNormalizer {
    @Override
    public String normalize(String commandOutput, ExecutionMetadata executionMetadata) {
        if (commandOutput.isEmpty()) {
            return commandOutput;
        }
        return Arrays.stream(commandOutput.split("\\r?\\n")).collect(Collectors.joining("\n"));
    }
}
