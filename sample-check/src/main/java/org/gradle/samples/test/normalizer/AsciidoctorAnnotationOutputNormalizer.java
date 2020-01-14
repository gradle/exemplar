package org.gradle.samples.test.normalizer;

import org.gradle.samples.executor.ExecutionMetadata;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AsciidoctorAnnotationOutputNormalizer implements OutputNormalizer {
    private static final Pattern ASCIIDOCTOR_ANNOTATION_PATTERN = Pattern.compile("\\s+// <\\d+>$");

    @Override
    public String normalize(String commandOutput, ExecutionMetadata executionMetadata) {
        return Arrays.stream(commandOutput.split("\\r?\\n", -1))
                .map(AsciidoctorAnnotationOutputNormalizer::stripAsciidoctorAnnotation)
                .collect(Collectors.joining("\n"));
    }

    private static String stripAsciidoctorAnnotation(String line) {
        if (ASCIIDOCTOR_ANNOTATION_PATTERN.matcher(line).find()) {
            return ASCIIDOCTOR_ANNOTATION_PATTERN.matcher(line).replaceFirst("");
        }
        return line;
    }
}
