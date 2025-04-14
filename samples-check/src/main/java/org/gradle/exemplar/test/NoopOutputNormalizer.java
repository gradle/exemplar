package org.gradle.exemplar.test;

import org.gradle.exemplar.executor.ExecutionMetadata;
import org.gradle.exemplar.test.normalizer.OutputNormalizer;

public class NoopOutputNormalizer implements OutputNormalizer {
    @Override
    public String normalize(String commandOutput, ExecutionMetadata executionMetadata) {
        return commandOutput;
    }
}
