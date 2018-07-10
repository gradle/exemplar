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

import org.apache.commons.io.FileUtils;
import org.gradle.internal.impldep.com.google.common.collect.Lists;
import org.gradle.samples.executor.CliCommandExecutor;
import org.gradle.samples.executor.CommandExecutionResult;
import org.gradle.samples.executor.ExecutionMetadata;
import org.gradle.samples.loader.SamplesDiscovery;
import org.gradle.samples.model.Command;
import org.gradle.samples.model.Sample;
import org.gradle.samples.test.normalizer.OutputNormalizer;
import org.gradle.samples.test.verifier.AnyOrderLineSegmentedOutputVerifier;
import org.gradle.samples.test.verifier.StrictOrderLineSegmentedOutputVerifier;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SamplesRunner extends ParentRunner<Sample> {
    // See https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
    public static final List<String> SAFE_SYSTEM_PROPERTIES = Arrays.asList("file.separator", "java.home", "java.vendor", "java.version", "line.separator", "os.arch", "os.name", "os.version", "path.separator", "user.dir", "user.home", "user.name");

    private List<OutputNormalizer> normalizers = new ArrayList<>();

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    /**
     * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
     *
     * @param testClass reference to test class being run
     */
    public SamplesRunner(Class<?> testClass) throws InitializationError {
        super(testClass);

        SamplesOutputNormalizers samplesOutputNormalizers = testClass.getAnnotation(SamplesOutputNormalizers.class);
        if (samplesOutputNormalizers != null) {
            try {
                for (Class<? extends OutputNormalizer> clazz : samplesOutputNormalizers.value()) {
                    normalizers.add(clazz.newInstance());
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Could not instantiate samples output normalizers", e);
            }
        }

        try {
            tmpDir.create();
        } catch (IOException e) {
            throw new RuntimeException("Could not create temporary folder " + tmpDir.getRoot().getAbsolutePath(), e);
        }
    }

    @Override
    protected List<Sample> getChildren() {
        List<Sample> samplesFromDirectory = getSamplesFromDirectory();
        List<Sample> samplesFromDocument = getSamplesFromDocument();
        List<Sample> result = Lists.newArrayList();
        result.addAll(samplesFromDirectory);
        result.addAll(samplesFromDocument);
        return result;
    }

    private List<Sample> getSamplesFromDocument() {
        DocumentWithSamples documentWithSamples = getTestClass().getAnnotation(DocumentWithSamples.class);
        File documentFile;
        try {
            if (documentWithSamples != null) {
                documentFile = new File(documentWithSamples.value());
            } else {
                throw new InitializationError("Document with samples is not declared.  Please annotate your test class with @DocumentWithSamples(\"path/to/document.adoc\")");
            }

            if (!documentFile.exists()) {
                throw new InitializationError("Document " + documentWithSamples.value() + " does not exist");
            }
            return SamplesDiscovery.allSamplesFromDocument(documentFile);
        } catch (InitializationError e) {
            throw new RuntimeException("Could not initialize SamplesRunner", e);
        }
    }

    private List<Sample> getSamplesFromDirectory() {
        SamplesRoot samplesRoot = getTestClass().getAnnotation(SamplesRoot.class);
        File samplesRootDir;
        try {
            if (samplesRoot != null) {
                samplesRootDir = new File(samplesRoot.value());
            } else {
                throw new InitializationError("Samples root directory is not declared. Please annotate your test class with @SamplesRoot(\"path/to/samples\")");
            }

            if (!samplesRootDir.exists()) {
                throw new InitializationError("Samples root directory " + samplesRootDir.getAbsolutePath() + " does not exist");
            }
            return SamplesDiscovery.allSamples(samplesRootDir);
        } catch (InitializationError e) {
            throw new RuntimeException("Could not initialize SamplesRunner", e);
        }
    }

    @Override
    protected Description describeChild(final Sample child) {
        return Description.createTestDescription(getTestClass().getJavaClass(), child.getId());
    }

    @Override
    protected void runChild(final Sample sample, final RunNotifier notifier) {
        Description childDescription = describeChild(sample);
        if (isIgnored(sample)) {
            notifier.fireTestIgnored(childDescription);
        } else {
            notifier.fireTestStarted(childDescription);
            try {
                final Sample testSpecificSample = initSample(sample);

                // Execute and verify each command
                for (Command command : testSpecificSample.getCommands()) {
                    CommandExecutionResult result = execute(testSpecificSample.getProjectDir(), command);

                    if (result.getExitCode() != 0 && !command.isExpectFailure()) {
                        Assert.fail("Expected sample invocation to succeed but it failed. Output was:\n" + result.getOutput());
                    } else if (result.getExitCode() == 0 && command.isExpectFailure()) {
                        Assert.fail("Expected sample invocation to fail but it succeeded. Output was:\n" + result.getOutput());
                    }

                    verifyOutput(command, result);
                }
            } catch (Throwable t) {
                notifier.fireTestFailure(new Failure(childDescription, t));
            } finally {
                notifier.fireTestFinished(childDescription);
            }
        }
    }

    private Sample initSample(final Sample sample) throws IOException {
        File tmpProjectDir = new File(tmpDir.getRoot(), sample.getId());
        tmpProjectDir.mkdirs();
        FileUtils.copyDirectory(sample.getProjectDir(), tmpProjectDir);
        return new Sample(sample.getId(), tmpProjectDir, sample.getCommands());
    }

    private void verifyOutput(final Command command, final CommandExecutionResult executionResult) {
        if (command.getExpectedOutput() == null) {
            return;
        }

        String expectedOutput = command.getExpectedOutput();
        String actualOutput = executionResult.getOutput();

        for (OutputNormalizer normalizer : normalizers) {
            actualOutput = normalizer.normalize(actualOutput, executionResult.getExecutionMetadata());
        }

        if (command.isAllowDisorderedOutput()) {
            new AnyOrderLineSegmentedOutputVerifier().verify(expectedOutput, actualOutput, command.isAllowAdditionalOutput());
        } else {
            new StrictOrderLineSegmentedOutputVerifier().verify(expectedOutput, actualOutput, command.isAllowAdditionalOutput());
        }
    }

    public CommandExecutionResult execute(final File tempSampleOutputDir, final Command command) throws IOException {
        return new CliCommandExecutor(tempSampleOutputDir).execute(command, getExecutionMetadata(tempSampleOutputDir));
    }

    protected ExecutionMetadata getExecutionMetadata(final File tempSampleOutputDir) {
        Map<String, String> systemProperties = new HashMap<>();
        for (String systemPropertyKey : SAFE_SYSTEM_PROPERTIES) {
            systemProperties.put(systemPropertyKey, System.getProperty(systemPropertyKey));
        }

        return new ExecutionMetadata(tempSampleOutputDir, systemProperties);
    }
}
