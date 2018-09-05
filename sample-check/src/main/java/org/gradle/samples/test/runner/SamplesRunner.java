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
import org.apache.commons.lang3.StringUtils;
import org.gradle.samples.executor.*;
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
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class SamplesRunner extends ParentRunner<Sample> {
    // See https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
    public static final List<String> SAFE_SYSTEM_PROPERTIES = Arrays.asList("file.separator", "java.home", "java.vendor", "java.version", "line.separator", "os.arch", "os.name", "os.version", "path.separator", "user.dir", "user.home", "user.name");

    private final List<? extends OutputNormalizer> normalizers;

    private final List<CommandModifier> commandModifiers;

    @Rule
    public TemporaryFolder tmpDir = new TemporaryFolder();

    /**
     * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
     *
     * @param testClass reference to test class being run
     */
    public SamplesRunner(Class<?> testClass) throws InitializationError {
        super(testClass);

        normalizers = this.instantiateAnnotationClasses(testClass, SamplesOutputNormalizers.class, new Transformer<Class<OutputNormalizer>[], SamplesOutputNormalizers>() {
            @Override
            public Class<OutputNormalizer>[] transform(SamplesOutputNormalizers samplesOutputNormalizers) {
                return (Class<OutputNormalizer>[]) samplesOutputNormalizers.value();
            }
        });
        commandModifiers = instantiateAnnotationClasses(testClass, CommandModifiers.class, new Transformer<Class<CommandModifier>[], CommandModifiers>() {
            @Override
            public Class<CommandModifier>[] transform(CommandModifiers commandModifiers) {
                return (Class<CommandModifier>[]) commandModifiers.value();
            }
        });

        try {
            tmpDir.create();
        } catch (IOException e) {
            throw new RuntimeException("Could not create temporary folder " + tmpDir.getRoot().getAbsolutePath(), e);
        }
    }

    private <T, A extends Annotation> List<T> instantiateAnnotationClasses(Class testClass, Class<A> annotationClass, Transformer<Class<T>[], A> transformer) {
        A annotation = (A) testClass.getAnnotation(annotationClass);
        List<T> ret = new ArrayList<>();
        if (annotation != null) {
            for (Class<T> clazz : transformer.transform(annotation)) {
                try {
                    ret.add(clazz.getConstructor().newInstance());
                } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                    throw new RuntimeException("Could not instantiate " + clazz.getName(), e);
                }
            }
        }
        return ret;
    }

    @Override
    protected List<Sample> getChildren() {
        return SamplesDiscovery.externalSamples(getSamplesRootDir());
    }

    protected File getSamplesRootDir() {
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
        } catch (InitializationError e) {
            throw new RuntimeException("Could not initialize SamplesRunner", e);
        }
        return samplesRootDir;
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
                        Assert.fail(String.format("Expected sample invocation to succeed but it failed.%nCommand was: '%s %s'%n[BEGIN OUTPUT]%n%s%n[END OUTPUT]%n", command.getExecutable(), StringUtils.join(command.getArgs(), " "), result.getOutput()));
                    } else if (result.getExitCode() == 0 && command.isExpectFailure()) {
                        Assert.fail(String.format("Expected sample invocation to fail but it succeeded.%nCommand was: '%s %s'%n[BEGIN OUTPUT]%n%s%n[END OUTPUT]%n", command.getExecutable(), StringUtils.join(command.getArgs(), " "), result.getOutput()));
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
        // TODO: get executor
        return decorateExecutor(new CliCommandExecutor(tempSampleOutputDir)).execute(command, getExecutionMetadata(tempSampleOutputDir));
    }

    protected CommandExecutor decorateExecutor(CommandExecutor executor) {
        return new ModifyingCommandExecutor(executor, commandModifiers);
    }

    protected ExecutionMetadata getExecutionMetadata(final File tempSampleOutputDir) {
        Map<String, String> systemProperties = new HashMap<>();
        for (String systemPropertyKey : SAFE_SYSTEM_PROPERTIES) {
            systemProperties.put(systemPropertyKey, System.getProperty(systemPropertyKey));
        }

        return new ExecutionMetadata(tempSampleOutputDir, systemProperties);
    }
}
