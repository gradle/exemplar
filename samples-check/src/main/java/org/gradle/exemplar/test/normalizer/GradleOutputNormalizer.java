/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.exemplar.test.normalizer;

import org.apache.commons.lang3.StringUtils;
import org.gradle.exemplar.executor.ExecutionMetadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class GradleOutputNormalizer implements OutputNormalizer {
    private static final Pattern STACK_TRACE_ELEMENT = Pattern.compile("\\s+(at\\s+)?([\\w.$_]+/)?[\\w.$_]+\\.[\\w$_ =+\'-<>]+\\(.+?\\)(\\x1B\\[0K)?");
    private static final Pattern BUILD_RESULT_PATTERN = Pattern.compile("BUILD (SUCCESSFUL|FAILED)( in \\d+(ms|s|m|h)( \\d+(ms|s|m|h))*)?");
    private static final Pattern DOCUMENTATION_URL_PATTERN = Pattern.compile("https://docs.gradle.org/((\\d+.\\d+(.\\d+)?(-\\d+\\+0000)?)|current|nightly)/");
    private static final Pattern BUILD_SCAN_URL_PATTERN = Pattern.compile("https://gradle.com/s/([a-z0-9]+)");

    public static final String DOWNLOAD_MESSAGE_PREFIX = "Download ";
    public static final String GENERATING_JAR_PREFIX = "Generating JAR file 'gradle-api-";

    public static final String DOWNLOADING_WRAPPER_MESSAGE_PREFIX = "Downloading https://services.gradle.org/distributions";
    public static final String DOWNLOADING_WRAPPER_PROGRESS_PREFIX = ".........10%";

    public static final String INCUBATING_FEATURE_SUFFIX = "incubating feature.";

    // Duplicating here to avoid use of Gradle's internal API
    public static final String STARTING_A_GRADLE_DAEMON_MESSAGE = "Starting a Gradle Daemon";
    public static final String DAEMON_WILL_BE_STOPPED_MESSAGE = "Daemon will be stopped at the end of the build";
    public static final String EXPIRING_DAEMON_MESSAGE = "Expiring Daemon because JVM Tenured space is exhausted";
    public static final String DEPRECATED_GRADLE_FEATURES_MESSAGE = "Deprecated Gradle features were used in this build, making it incompatible with Gradle";
    public static final String JAVA_7_DEPRECATION_MESSAGE = "Support for running Gradle using Java 7 has been deprecated and is scheduled to be removed in Gradle 5.0.";

    @Override
    public String normalize(String commandOutput, ExecutionMetadata executionMetadata) {
        //commandOutput = commandOutput.replaceAll(executionMetadata.getTempSampleProjectDir().getAbsolutePath(), NORMALIZED_SAMPLES_PATH);
        List<String> result = new ArrayList<>();
        final List<String> lines = Arrays.asList(commandOutput.split("\\r?\\n", -1));
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i);
            if (line.startsWith(GENERATING_JAR_PREFIX)) {
                i++;
            } else if (line.startsWith(DOWNLOAD_MESSAGE_PREFIX)) {
                i++;
            } else if (line.startsWith(DOWNLOADING_WRAPPER_MESSAGE_PREFIX)) {
                // Remove the "Dowloading https://..." message for wrapper
                i++;
            } else if (line.startsWith(DOWNLOADING_WRAPPER_PROGRESS_PREFIX)) {
                // Remove the ".........10%.........20%" message
                i++;
            } else if (line.endsWith(INCUBATING_FEATURE_SUFFIX)) {
                // Remove the "...incubating feature." message
                i++;
            } else if (line.contains(STARTING_A_GRADLE_DAEMON_MESSAGE)) {
                // Remove the "daemon starting" message
                i++;
            } else if (line.contains(DAEMON_WILL_BE_STOPPED_MESSAGE)) {
                // Remove the "Daemon will be shut down" message
                i++;
            } else if (line.contains(EXPIRING_DAEMON_MESSAGE)) {
                // Remove the "Expiring Daemon" message
                i++;
            } else if (line.contains(DEPRECATED_GRADLE_FEATURES_MESSAGE)) {
                // Remove the "Deprecated Gradle features..." message and "See https://docs.gradle.org..."
                i+=2;
            } else if (line.contains(JAVA_7_DEPRECATION_MESSAGE)) {
                // Remove the Java 7 deprecation warning. This should be removed after 5.0
                i++;
                while (i < lines.size() && STACK_TRACE_ELEMENT.matcher(lines.get(i)).matches()) {
                    i++;
                }
            } else if (BUILD_RESULT_PATTERN.matcher(line).matches()) {
                result.add(BUILD_RESULT_PATTERN.matcher(line).replaceFirst("BUILD $1 in 0s"));
                i++;
            } else {
                if (DOCUMENTATION_URL_PATTERN.matcher(line).find()) {
                    line = DOCUMENTATION_URL_PATTERN.matcher(line).replaceFirst("https://docs.gradle.org/0.0.0/");
                } else if (BUILD_SCAN_URL_PATTERN.matcher(line).find()) {
                    line = BUILD_SCAN_URL_PATTERN.matcher(line).replaceFirst("https://gradle.com/s/feeedfooc00de");
                }
                result.add(line);
                i++;
            }
        }

        return StringUtils.join(result, "\n");
    }
}
