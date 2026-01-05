package test;

import org.gradle.exemplar.executor.ExecutionMetadata;
import org.gradle.exemplar.test.normalizer.OutputNormalizer;

@SuppressWarnings("unused") // used by system property
public class TestOutputNormalizer implements OutputNormalizer {
    @Override
    public String normalize(String commandOutput, ExecutionMetadata executionMetadata) {
        return commandOutput;
    }
}
