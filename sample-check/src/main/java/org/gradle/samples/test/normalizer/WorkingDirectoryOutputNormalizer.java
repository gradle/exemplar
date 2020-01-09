package org.gradle.samples.test.normalizer;

import org.gradle.samples.executor.ExecutionMetadata;

import java.io.IOException;
import java.io.UncheckedIOException;

public class WorkingDirectoryOutputNormalizer implements OutputNormalizer {
    @Override
    public String normalize(String commandOutput, ExecutionMetadata executionMetadata) {
        try {
            return commandOutput.replace(executionMetadata.getTempSampleProjectDir().getCanonicalPath(), "/working-directory");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
