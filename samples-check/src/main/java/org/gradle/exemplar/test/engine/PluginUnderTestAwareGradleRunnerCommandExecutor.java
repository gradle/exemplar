package org.gradle.exemplar.test.engine;

import org.gradle.exemplar.executor.CommandExecutor;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

public class PluginUnderTestAwareGradleRunnerCommandExecutor extends CommandExecutor {
    private final File workingDir;
    private final File customGradleInstallation;
    private final boolean expectFailure;

    PluginUnderTestAwareGradleRunnerCommandExecutor(File workingDir, @Nullable File customGradleInstallation, boolean expectFailure) {
        this.workingDir = workingDir;
        this.customGradleInstallation = customGradleInstallation;
        this.expectFailure = expectFailure;
    }

    @Override
    protected int run(String executable, List<String> args, List<String> flags, OutputStream output) {
        List<String> allArguments = new ArrayList<>(args);
        allArguments.addAll(flags);

        GradleRunner gradleRunner = GradleRunner.create()
                .withProjectDir(workingDir)
                .withArguments(allArguments)
                .withPluginClasspath()
                .forwardOutput();

        if (customGradleInstallation != null) {
            gradleRunner.withGradleInstallation(customGradleInstallation);
        }

        BuildResult buildResult;
        if (expectFailure) {
            buildResult = gradleRunner.buildAndFail();
        } else {
            buildResult = gradleRunner.build();
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(output)) {
            writer.write(buildResult.getOutput());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return expectFailure ? 1 : 0;
    }
}
