package org.gradle.exemplar.test.engine;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.gradle.exemplar.executor.CliCommandExecutor;
import org.gradle.exemplar.executor.CommandExecutionResult;
import org.gradle.exemplar.executor.CommandExecutor;
import org.gradle.exemplar.executor.ExecutionMetadata;
import org.gradle.exemplar.loader.SamplesDiscovery;
import org.gradle.exemplar.model.Command;
import org.gradle.exemplar.model.Sample;
import org.gradle.exemplar.test.normalizer.OutputNormalizer;
import org.gradle.exemplar.test.SampleModifier;
import org.gradle.exemplar.test.Samples;
import org.gradle.exemplar.test.verifier.AnyOrderLineSegmentedOutputVerifier;
import org.gradle.exemplar.test.verifier.StrictOrderLineSegmentedOutputVerifier;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.opentest4j.AssertionFailedError;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;

public class SamplesRunnerJUnitEngine implements TestEngine {

    // See https://docs.oracle.com/javase/tutorial/essential/environment/sysprop.html
    public static final List<String> SAFE_SYSTEM_PROPERTIES = Arrays.asList("file.separator", "java.home", "java.vendor", "java.version", "line.separator", "os.arch", "os.name", "os.version", "path.separator", "user.dir", "user.home", "user.name");

    private final File tempDir;

    public SamplesRunnerJUnitEngine() throws IOException {
        tempDir = Files.createTempDirectory("samples-junit-engine").toFile();
    }

    @Override
    public String getId() {
        return "SamplesRunnerJUnitEngine";
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, "Samples Runner JUnit Engine");
        discoveryRequest.getSelectorsByType(ClassSelector.class)
                .stream()
                .map(ClassSelector::getJavaClass).forEach(javaClass -> {
                    filterClass(uniqueId, engineDescriptor, javaClass);
                });

        return engineDescriptor;
    }

    private void filterClass(UniqueId uniqueId, EngineDescriptor engineDescriptor, Class<?> javaClass) {
        Samples samplesDef = javaClass.getAnnotation(Samples.class);
        if (samplesDef != null) {
            UniqueId classUniqueId = uniqueId.append("className", javaClass.getSimpleName());
            SamplesTestDescriptor samplesTestDescriptor = new SamplesTestDescriptor(
                    classUniqueId,
                    javaClass
            );

            List<Sample> samples = samplesDef.samplesType() == Samples.SamplesType.DEFAULT ?
                    SamplesDiscovery.externalSamples(getSamplesRootDir(
                            samplesDef.root(),
                            samplesDef.implicitRootDirSupplier()
                    )) :
                    SamplesDiscovery.embeddedSamples(getSamplesRootDir(
                            samplesDef.root(),
                            samplesDef.implicitRootDirSupplier()
                    ));

            List<? extends OutputNormalizer> normalizers = instantiateList(samplesDef.outputNormalizers());
            List<SampleModifier> sampleModifiers = instantiateList(samplesDef.modifiers());
            Function<CommandExecutorParams, CommandExecutor> commandExecutorFunction = instantiateObject(samplesDef.commandExecutorFunction());

            samples.forEach(sample ->
                    samplesTestDescriptor.addChild(new SampleTestDescriptor(
                            classUniqueId.append("sampleId", sample.getId()),
                            sample,
                            commandExecutorFunction,
                            normalizers,
                            sampleModifiers
                    ))
            );

            engineDescriptor.addChild(samplesTestDescriptor);
        }
    }

    @Override
    public void execute(ExecutionRequest request) {
        TestDescriptor engineDescriptor = request.getRootTestDescriptor();
        EngineExecutionListener listener = request.getEngineExecutionListener();

        listener.executionStarted(engineDescriptor);
        for (TestDescriptor classDescriptor : engineDescriptor.getChildren()) {
            listener.executionStarted(classDescriptor);
            for (TestDescriptor testDescriptor : classDescriptor.getChildren()) {
                SampleTestDescriptor descriptor = (SampleTestDescriptor) testDescriptor;
                listener.executionStarted(testDescriptor);
                try {
                    runSample(descriptor);
                    listener.executionFinished(testDescriptor, TestExecutionResult.successful());
                } catch (Throwable t) {
                    listener.executionFinished(testDescriptor, TestExecutionResult.failed(t));
                }
            }

            listener.executionFinished(classDescriptor, TestExecutionResult.successful());
        }
        listener.executionFinished(engineDescriptor, TestExecutionResult.successful());
    }

    protected File getSamplesRootDir(String samplesRoot, Class<? extends Supplier<File>> implicitRootDirSupplier) {
        File samplesRootDir;
        try {
            if (samplesRoot != null) {
                samplesRootDir = new File(samplesRoot);
            } else {
                samplesRootDir = supply(implicitRootDirSupplier);
            }

            if (samplesRootDir == null) {
                throw new IllegalArgumentException("Samples root directory is not declared. Please annotate your test class with @SamplesRoot(\"path/to/samples\")");
            }
            if (!samplesRootDir.exists()) {
                throw new IllegalArgumentException("Samples root directory " + samplesRootDir.getAbsolutePath() + " does not exist");
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not initialize SamplesRunnerJUnitEngine", e);
        }
        return samplesRootDir;
    }

    private static <T> List<T> instantiateList(Class<? extends T>[] classes) {
        if (classes == null) {
            return emptyList();
        }

        List<T> list = new ArrayList<>();
        for (Class<? extends T> clazz : classes) {
            try {
                list.add(clazz.getConstructor().newInstance());
            } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
                throw new RuntimeException("Could not instantiate " + clazz.getName(), e);
            }
        }

        return list;
    }

    private static <T> T instantiateObject(Class<? extends T> supplierClass) {
        try {
            return supplierClass.getDeclaredConstructor().newInstance();
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to supply a value from " + supplierClass + " class", e);
        }
    }

    private static <T> T supply(Class<? extends Supplier<T>> supplierClass) {
        return instantiateObject(supplierClass).get();
    }

    private void runSample(SampleTestDescriptor descriptor) throws Exception {
        final Sample testSpecificSample = initSample(descriptor);
        File baseWorkingDir = testSpecificSample.getProjectDir();

        // Execute and verify each command
        for (Command command : testSpecificSample.getCommands()) {
            File workingDir = baseWorkingDir;

            if (command.getExecutionSubdirectory() != null) {
                workingDir = new File(workingDir, command.getExecutionSubdirectory());
            }

            // This should be some kind of plugable executor rather than hard-coded here
            if (command.getExecutable().equals("cd")) {
                baseWorkingDir = new File(baseWorkingDir, command.getArgs().get(0)).getCanonicalFile();
                continue;
            }

            CommandExecutionResult result = executeSample(descriptor, workingDir, command);

            if (result.getExitCode() != 0 && !command.isExpectFailure()) {
                throw new AssertionFailedError(String.format(
                        "Expected sample invocation to succeed but it failed.%nCommand was: '%s %s'%nWorking directory: '%s'%n[BEGIN OUTPUT]%n%s%n[END OUTPUT]%n",
                        command.getExecutable(),
                        StringUtils.join(command.getArgs(), " "),
                        workingDir.getAbsolutePath(),
                        result.getOutput()
                ));
            } else if (result.getExitCode() == 0 && command.isExpectFailure()) {
                throw new AssertionFailedError(String.format(
                        "Expected sample invocation to fail but it succeeded.%nCommand was: '%s %s'%nWorking directory: '%s'%n[BEGIN OUTPUT]%n%s%n[END OUTPUT]%n",
                        command.getExecutable(),
                        StringUtils.join(command.getArgs(), " "),
                        workingDir.getAbsolutePath(),
                        result.getOutput()
                ));
            }

            verifyOutput(command, result, descriptor.normalizers);
        }
    }

    private void verifyOutput(
            final Command command,
            final CommandExecutionResult executionResult,
            final List<? extends OutputNormalizer> normalizers
    ) {
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

    private CommandExecutionResult executeSample(SampleTestDescriptor descriptor, File workingDir, Command command) {
        ExecutionMetadata executionMetadata = getExecutionMetadata(descriptor.sample.getProjectDir());
        CommandExecutorParams commandExecutorParams = new CommandExecutorParams(executionMetadata, workingDir, command);
        return descriptor.commandExecutorFunction
                .apply(commandExecutorParams)
                .execute(command, executionMetadata);
    }

    private ExecutionMetadata getExecutionMetadata(final File tempSampleOutputDir) {
        Map<String, String> systemProperties = new HashMap<>();
        for (String systemPropertyKey : SAFE_SYSTEM_PROPERTIES) {
            systemProperties.put(systemPropertyKey, System.getProperty(systemPropertyKey));
        }

        return new ExecutionMetadata(tempSampleOutputDir, systemProperties);
    }

    private Sample initSample(final SampleTestDescriptor descriptor) throws IOException {
        Sample sampleIn = descriptor.sample;
        File tmpProjectDir = new File(tempDir, sampleIn.getId());
        FileUtils.copyDirectory(sampleIn.getProjectDir(), tmpProjectDir);
        Sample sample = new Sample(sampleIn.getId(), tmpProjectDir, sampleIn.getCommands());
        for (SampleModifier sampleModifier : descriptor.sampleModifiers) {
            sample = sampleModifier.modify(sample);
        }
        return sample;
    }

    private static class SampleTestDescriptor extends AbstractTestDescriptor {

        private final Sample sample;
        private final Function<CommandExecutorParams, CommandExecutor> commandExecutorFunction;
        private final List<? extends OutputNormalizer> normalizers;
        private final List<SampleModifier> sampleModifiers;

        private SampleTestDescriptor(
                UniqueId uniqueId,
                Sample sample,
                Function<CommandExecutorParams, CommandExecutor> commandExecutorFunction, List<? extends OutputNormalizer> normalizers,
                List<SampleModifier> sampleModifiers
        ) {
            super(uniqueId, sample.getId());
            this.sample = sample;
            this.commandExecutorFunction = commandExecutorFunction;
            this.normalizers = normalizers;
            this.sampleModifiers = sampleModifiers;
        }

        @Override
        public Type getType() {
            return Type.TEST;
        }
    }

    private static class SamplesTestDescriptor extends AbstractTestDescriptor {

        private SamplesTestDescriptor(UniqueId uniqueId, Class<?> testClass) {
            super(uniqueId, testClass.getSimpleName(), ClassSource.from(testClass));
        }

        @Override
        public Type getType() {
            return Type.CONTAINER;
        }
    }
}
