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

import org.gradle.api.JavaVersion;
import org.gradle.samples.executor.CliCommandExecutor;
import org.gradle.samples.executor.CommandExecutor;
import org.gradle.samples.executor.ExecutionMetadata;
import org.gradle.samples.executor.GradleRunnerCommandExecutor;
import org.gradle.samples.model.Command;
import org.gradle.samples.model.Sample;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runners.model.InitializationError;

import javax.annotation.Nullable;
import java.io.File;

/**
 * A custom implementation of {@link SamplesRunner} that uses the Gradle Tooling API to execute sample builds.
 */
public class GradleSamplesRunner extends SamplesRunner {
    private static final String GRADLE_EXECUTABLE = "gradle";
    @Rule
    public TemporaryFolder tempGradleUserHomeDir = new TemporaryFolder();
    private File customGradleInstallation = null;

    /**
     * {@inheritDoc}
     */
    public GradleSamplesRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    /**
     * Gradle samples tests are ignored on Java 7 and below.
     */
    @Override
    protected boolean isIgnored(Sample child) {
        return !JavaVersion.current().isJava8Compatible();
    }

    @Override
    protected CommandExecutor selectExecutor(ExecutionMetadata executionMetadata, File workingDir, Command command) {
        boolean expectFailure = command.isExpectFailure();
        if (command.getExecutable().equals(GRADLE_EXECUTABLE)) {
            return new GradleRunnerCommandExecutor(workingDir, customGradleInstallation, expectFailure);
        }
        return new CliCommandExecutor(workingDir);
    }

    @Nullable
    @Override
    protected File getImplicitSamplesRootDir() {
        String gradleHomeDir = getCustomGradleInstallationFromSystemProperty();
        if (System.getProperty("integTest.samplesdir") != null) {
            String samplesRootProperty = System.getProperty("integTest.samplesdir", gradleHomeDir + "/samples");
            return new File(samplesRootProperty);
        } else if (customGradleInstallation != null) {
            return new File(customGradleInstallation, "samples");
        } else {
            return null;
        }
    }

    @Nullable
    private String getCustomGradleInstallationFromSystemProperty() {
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
        return gradleHomeDirProperty;
    }

}
