package org.gradle.exemplar.test.runner.modifiers;

import org.gradle.exemplar.executor.ExecutionMetadata;
import org.gradle.exemplar.test.normalizer.OutputNormalizer;

import java.io.File;
import java.util.regex.Pattern;

public class ProjectDirPathOutputNormalizer implements OutputNormalizer {
    @Override
    public String normalize(String commandOutput, ExecutionMetadata executionMetadata) {
        try {
            String tempSampleProjectDir = executionMetadata.getTempSampleProjectDir().getCanonicalPath();
            return commandOutput.replaceAll(Pattern.quote(tempSampleProjectDir), "/path/to");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
