package org.gradle.exemplar.test.normalizer;

import org.gradle.exemplar.executor.ExecutionMetadata;

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
