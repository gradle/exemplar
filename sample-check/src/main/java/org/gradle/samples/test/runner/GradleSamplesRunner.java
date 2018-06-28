/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
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
package org.gradle.samples.test.runner;

import org.apache.commons.io.IOUtils;
import org.gradle.samples.executor.CommandExecutionResult;
import org.gradle.samples.executor.CommandExecutor;
import org.gradle.samples.executor.ExecutionMetadata;
import org.gradle.samples.loader.SamplesDiscovery;
import org.gradle.samples.model.Command;
import org.gradle.samples.model.Sample;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.internal.DefaultGradleRunner;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A custom implementation of {@link SamplesRunner} that uses the Gradle Tooling API to execute sample builds.
 */
public class GradleSamplesRunner extends SamplesRunner {
    @Rule
    public TemporaryFolder tempGradleUserHomeDir = new TemporaryFolder();
    private File customGradleInstallation = null;

    /**
     * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
     *
     * @param testClass reference to test class being run
     */
    public GradleSamplesRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected List<Sample> getChildren() {
        // Allow Gradle installation and samples root dir to be set from a system property
        // This is to allow Gradle to test Gradle installations during integration testing
        final String gradleHomeDirProperty = System.getProperty("integTest.gradleHomeDir");
        if (gradleHomeDirProperty != null) {
            File customGradleInstallationDir = new File(gradleHomeDirProperty);
            if (customGradleInstallationDir.exists()) {
                this.customGradleInstallation = customGradleInstallationDir;
            } else {
                throw new RuntimeException(String.format("Custom Gradle installation dir at %s was not found", gradleHomeDirProperty));
            }
        }

        File samplesRootDir;
        SamplesRoot samplesRoot = getTestClass().getAnnotation(SamplesRoot.class);
        try {

            if (samplesRoot != null) {
                samplesRootDir = new File(samplesRoot.value());
            } else if (System.getProperty("integTest.samplesdir") != null) {
                String samplesRootProperty = System.getProperty("integTest.samplesdir", gradleHomeDirProperty + "/samples");
                samplesRootDir = new File(samplesRootProperty);
            } else if (customGradleInstallation != null) {
                samplesRootDir = new File(customGradleInstallation, "samples");
            } else {
                throw new InitializationError("Samples root directory is not declared. Please annotate your test class with @SamplesRoot(\"path/to/samples\")");
            }

            if (customGradleInstallation != null) {
                gradleConnector.useInstallation(customGradleInstallation);
            }

            if (!samplesRootDir.exists()) {
                throw new InitializationError("Samples root directory " + samplesRootDir.getAbsolutePath() + " does not exist");
            }
            return SamplesDiscovery.allSamples(samplesRootDir);
        } catch (InitializationError e) {
            throw new RuntimeException("Could not initialize GradleSamplesRunner", e);
        }
    }

    @Override
    public CommandExecutionResult execute(final File tempSampleOutputDir, final Command command) {
        File workingDir = tempSampleOutputDir;
        if (command.getExecutionSubdirectory() != null) {
            workingDir = new File(tempSampleOutputDir, command.getExecutionSubdirectory());
        }

        boolean expectFailure = command.isExpectFailure();
        ExecutionMetadata executionMetadata = getExecutionMetadata(tempSampleOutputDir);
        return new GradleRunnerCommandExecutor(workingDir, customGradleInstallation, expectFailure).execute(command, executionMetadata);
    }

    private static class GradleRunnerCommandExecutor extends CommandExecutor {
        private final File workingDir;
        private final File customGradleInstallation;
        private final boolean expectFailure;

        private GradleRunnerCommandExecutor(File workingDir, File customGradleInstallation, boolean expectFailure) {
            this.workingDir = workingDir;
            this.customGradleInstallation = customGradleInstallation;
            this.expectFailure = expectFailure;
        }

        @Override
        protected int run(String executable, List<String> commands, List<String> flags, Map<String, String> environmentVariables, OutputStream output) {
            assert environmentVariables.isEmpty() : "Gradle Runner does not support changing environment variables";

            List<String> allArguments = new ArrayList<>(commands);
            allArguments.addAll(flags);

            GradleRunner gradleRunner = GradleRunner.create()
                    .withProjectDir(workingDir)
                    .withArguments(allArguments)
                    .forwardOutput();
            if (customGradleInstallation!=null) {
                gradleRunner.withGradleInstallation(customGradleInstallation);
            }
            ((DefaultGradleRunner)gradleRunner).withJvmArguments(
                    // TODO: Does TestKit already do this?
                    "-Dorg.gradle.daemon.idletimeout=30000",
                    // TODO: Configurable?
                    "-Xmx512m",
                    // TODO: OK for newer JDKs?
                    "-XX:MaxPermSize=128m"
            );

            Writer mergedOutput = new OutputStreamWriter(output);
            try {
                BuildResult buildResult;
                if (expectFailure) {
                    buildResult = gradleRunner.buildAndFail();
                } else {
                    buildResult = gradleRunner.build();
                }
                mergedOutput.write(buildResult.getOutput());
                mergedOutput.close();
                return expectFailure ? 1 : 0;
            } catch (Exception e) {
                throw new RuntimeException("Could not execute " + executable, e);
            } finally {
                IOUtils.closeQuietly(mergedOutput);
            }
        }
    }
}
