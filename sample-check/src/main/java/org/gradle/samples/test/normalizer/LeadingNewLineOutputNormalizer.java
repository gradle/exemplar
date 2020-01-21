package org.gradle.samples.test.normalizer;

import org.gradle.samples.executor.ExecutionMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class LeadingNewLineOutputNormalizer implements OutputNormalizer {
    @Override
    public String normalize(String commandOutput, ExecutionMetadata executionMetadata) {
        if (commandOutput.isEmpty()) {
            return commandOutput;
        }
        List<String> lines = new ArrayList<>(Arrays.asList(commandOutput.split("\\r?\\n", -1)));

        while (lines.get(0).isEmpty()) {
            lines.remove(0);
        }

        return lines.stream().collect(Collectors.joining("\n"));
    }
}
