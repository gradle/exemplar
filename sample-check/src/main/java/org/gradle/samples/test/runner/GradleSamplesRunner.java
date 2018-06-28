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

import org.gradle.samples.executor.CommandExecutionResult;
import org.gradle.samples.executor.GradleToolingApiCommandExecutor;
import org.gradle.samples.loader.SamplesDiscovery;
import org.gradle.samples.model.Command;
import org.gradle.samples.model.Sample;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

/**
 * A custom implementation of {@link SamplesRunner} that uses the Gradle Tooling API to execute sample builds.
 */
public class GradleSamplesRunner extends SamplesRunner {
    private File customGradleInstallation = null;
    private GradleConnector gradleConnector;

    @Rule
    public TemporaryFolder tempGradleUserHomeDir = new TemporaryFolder();

    /**
     * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
     *
     * @param testClass reference to test class being run
     */
    public GradleSamplesRunner(Class<?> testClass) throws InitializationError {
        super(testClass);

        gradleConnector = GradleConnector.newConnector();
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
    public CommandExecutionResult execute(final File tempSampleOutputDir, final Command command) throws IOException {
        // Further isolate samples with unique Gradle user home dir
        File tempGradleUserHomeDir = new File(tempSampleOutputDir, ".gradle");
        tempGradleUserHomeDir.mkdirs();

        // Gradle Daemons should shut down after being idle
        File tempGradleProperties = new File(tempGradleUserHomeDir, "gradle.properties");
        Files.write(tempGradleProperties.toPath(), "org.gradle.daemon.idletimeout=30000".getBytes());

        File tempWorkingDirectory = tempSampleOutputDir;

        if (command.getExecutionSubdirectory() != null) {
            tempWorkingDirectory = new File(tempSampleOutputDir, command.getExecutionSubdirectory());
        }

        ProjectConnection projectConnection = gradleConnector
                .forProjectDirectory(tempWorkingDirectory)
                .useGradleUserHomeDir(tempGradleUserHomeDir)
                .connect();

        try {
            return new GradleToolingApiCommandExecutor(projectConnection).execute(command, getExecutionMetadata(tempSampleOutputDir));
        } finally {
            projectConnection.close();
        }
    }
}
