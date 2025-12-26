package org.gradle.exemplar;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.gradle.exemplar.executor.CliCommandExecutor;
import org.gradle.exemplar.executor.CommandExecutionResult;
import org.gradle.exemplar.executor.CommandExecutor;
import org.gradle.exemplar.executor.ExecutionMetadata;
import org.gradle.exemplar.model.Command;
import org.gradle.exemplar.model.InvalidSample;
import org.gradle.exemplar.model.Sample;
import org.gradle.exemplar.test.normalizer.OutputNormalizer;
import org.gradle.exemplar.test.runner.SampleModifier;
import org.gradle.exemplar.test.verifier.AnyOrderLineSegmentedOutputVerifier;
import org.gradle.exemplar.test.verifier.StrictOrderLineSegmentedOutputVerifier;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ExemplarTestEngine implements TestEngine {
    public static final Logger LOGGER = LoggerFactory.getLogger(ExemplarTestEngine.class);

    public static final List<String> SAFE_SYSTEM_PROPERTIES = Arrays.asList("file.separator", "java.home", "java.vendor", "java.version", "line.separator", "os.arch", "os.name", "os.version", "path.separator", "user.dir", "user.home", "user.name");
    public static final String ENGINE_ID = "exemplar";
    public static final String ENGINE_NAME = "Exemplar Test Engine";

    private final Set<OutputNormalizer> sampleNormalizers = new LinkedHashSet<>();
    private final Set<SampleModifier> sampleModifiers = new LinkedHashSet<>();

    @Override
    public String getId() {
        return ENGINE_ID;
    }

    @Override
    public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
        LOGGER.info(() -> {
            String selectorsMsg = discoveryRequest.getSelectorsByType(DiscoverySelector.class).stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n\t", "\t", ""));
            return "Discovering tests with engine: " + uniqueId + " using selectors:\n" + selectorsMsg;
        });

        EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, ENGINE_NAME);

        EngineDiscoveryRequestResolver.builder()
            .addSelectorResolver(new ExemplarTestResolver())
            .build()
            .resolve(discoveryRequest, engineDescriptor);

        return engineDescriptor;
    }

    @Override
    public void execute(ExecutionRequest executionRequest) {
        LOGGER.info(() -> "Executing tests with engine: " + executionRequest.getRootTestDescriptor().getUniqueId());

        File tmpDir = createTmpDir();
        try {
            EngineExecutionListener listener = executionRequest.getEngineExecutionListener();
            executionRequest.getRootTestDescriptor().getChildren().forEach(test -> {
                if (test instanceof ExemplarTestDescriptor) {
                    execute(((ExemplarTestDescriptor) test), tmpDir, executionRequest, listener);
                } else {
                    throw new IllegalStateException("Cannot execute test: " + test + " of type: " + test.getClass().getName());
                }
            });
        } finally {
            deleteRecursively(tmpDir);
        }
    }

    private static File createTmpDir() {
        try {
            Path path = Files.createTempDirectory("exemplar-");
            LOGGER.info(() -> "Testing base directory: " + path.toAbsolutePath());
            return path.toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteRecursively(File tmpDir) {
        if (tmpDir != null && tmpDir.exists()) {
            try {
                FileUtils.deleteDirectory(tmpDir);
            } catch (IOException e) {
                throw new RuntimeException("Could not delete temporary directory: ", e);
            }
        }
    }

    private void execute(ExemplarTestDescriptor test, File tmpDir, ExecutionRequest request, EngineExecutionListener listener) {
        populateSampleModifiers(request);
        populateOutputNormalizers(request);

        Sample sample = test.getSample();
        if (sample instanceof InvalidSample) {
            listener.executionFinished(test, TestExecutionResult.failed(((InvalidSample) sample).getException()));
        } else {
            listener.executionStarted(test);
            try {
                final Sample testSpecificSample = initSample(sample, tmpDir);
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

                    CommandExecutionResult result = execute(getExecutionMetadata(testSpecificSample.getProjectDir()), workingDir, command);

                    if (result.getExitCode() != 0 && !command.isExpectFailure()) {
                        String message = String.format("Expected sample invocation to succeed but it failed.%nCommand was: '%s %s'%nWorking directory: '%s'%n[BEGIN OUTPUT]%n%s%n[END OUTPUT]%n", command.getExecutable(), StringUtils.join(command.getArgs(), " "), workingDir.getAbsolutePath(), result.getOutput());

                        listener.executionFinished(test, TestExecutionResult.failed(new RuntimeException(message)));
                    } else if (result.getExitCode() == 0 && command.isExpectFailure()) {
                        String message = String.format("Expected sample invocation to fail but it succeeded.%nCommand was: '%s %s'%nWorking directory: '%s'%n[BEGIN OUTPUT]%n%s%n[END OUTPUT]%n", command.getExecutable(), StringUtils.join(command.getArgs(), " "), workingDir.getAbsolutePath(), result.getOutput());
                        listener.executionFinished(test, TestExecutionResult.failed(new RuntimeException(message)));
                    }
                    verifyOutput(command, result);
                }
                listener.executionFinished(test, TestExecutionResult.successful());
            } catch (Throwable t) {
                listener.executionFinished(test, TestExecutionResult.failed(t));
            }
        }
    }

    private void populateSampleModifiers(ExecutionRequest request) {
        populateFromSystemProperty(request, "exemplar.output.modifiers", sampleModifiers);
    }

    private void populateOutputNormalizers(ExecutionRequest request) {
        populateFromSystemProperty(request, "exemplar.output.normalizers", sampleNormalizers);
    }

    private static <T> void populateFromSystemProperty(ExecutionRequest request, String propertyName, Set<T> targetSet) {
        Optional<String> values = request.getConfigurationParameters().get(propertyName);
        boolean hasValues = values.isPresent() && !values.get().trim().isEmpty();
        if (hasValues) {
            String[] classNames = StringUtils.split(values.get(), ',');
            for (String className : classNames) {
                targetSet.add(newInstance(className));
            }
        }
    }

    private static <T> T newInstance(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return (T) clazz.getConstructor().newInstance();
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
                 InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Could not instantiate class: " + className, e);
        }
    }

    private Sample initSample(final Sample sampleIn, File tmpDir) throws IOException {
        File tmpProjectDir = new File(tmpDir, sampleIn.getId());
        tmpProjectDir.mkdirs();
        FileUtils.copyDirectory(sampleIn.getProjectDir(), tmpProjectDir);
        Sample sample = new Sample(sampleIn.getId(), tmpProjectDir, sampleIn.getCommands());

        for (SampleModifier sampleModifier : sampleModifiers) {
            LOGGER.debug(()-> "Modifier: " + sampleModifier.getClass().getName());
            sample = sampleModifier.modify(sample);
        }
        return sample;
    }

    private ExecutionMetadata getExecutionMetadata(final File tempSampleOutputDir) {
        Map<String, String> systemProperties = new HashMap<>();
        for (String systemPropertyKey : SAFE_SYSTEM_PROPERTIES) {
            systemProperties.put(systemPropertyKey, System.getProperty(systemPropertyKey));
        }
        return new ExecutionMetadata(tempSampleOutputDir, systemProperties);
    }

    private CommandExecutionResult execute(ExecutionMetadata executionMetadata, File workingDir, Command command) {
        return selectExecutor(executionMetadata, workingDir, command).execute(command, executionMetadata);
    }

    protected CommandExecutor selectExecutor(ExecutionMetadata executionMetadata, File workingDir, Command command) {
        return new CliCommandExecutor(workingDir);
    }

    private void verifyOutput(final Command command, final CommandExecutionResult executionResult) {
        if (command.getExpectedOutput() == null) {
            return;
        }

        String expectedOutput = command.getExpectedOutput();
        String actualOutput = executionResult.getOutput();

        for (OutputNormalizer normalizer : sampleNormalizers) {
            actualOutput = normalizer.normalize(actualOutput, executionResult.getExecutionMetadata());
        }

        if (command.isAllowDisorderedOutput()) {
            new AnyOrderLineSegmentedOutputVerifier().verify(expectedOutput, actualOutput, command.isAllowAdditionalOutput());
        } else {
            new StrictOrderLineSegmentedOutputVerifier().verify(expectedOutput, actualOutput, command.isAllowAdditionalOutput());
        }
    }
}
