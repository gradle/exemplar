package org.gradle.samples.test.normalizer;

import org.gradle.samples.executor.ExecutionMetadata;

import java.util.Arrays;
import java.util.stream.Collectors;

public class StripTrailingOutputNormalizer implements OutputNormalizer {
    @Override
    public String normalize(String commandOutput, ExecutionMetadata executionMetadata) {
        return Arrays.stream(commandOutput.split("\\r?\\n", -1)).map(StripTrailingOutputNormalizer::stripTrailing).collect(Collectors.joining("\n"));
    }

    private static String stripTrailing(String self) {
        int len = self.length();
        int st = 0;
        char[] val = self.toCharArray();    /* avoid getfield opcode */

        while ((st < len) && (Character.isSpaceChar(val[len - 1]))) {
            len--;
        }
        return ((len < self.length())) ? self.substring(0, len) : self;
    }
}
