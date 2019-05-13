package org.gradle.samples.executor;

import org.apache.commons.io.IOUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;

import javax.annotation.Nullable;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

public class GradleRunnerCommandExecutor extends CommandExecutor {
    private final File workingDir;
    private final File customGradleInstallation;
    private final boolean expectFailure;

    public GradleRunnerCommandExecutor(File workingDir, @Nullable File customGradleInstallation, boolean expectFailure) {
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
            .forwardOutput();

        if (customGradleInstallation != null) {
            gradleRunner.withGradleInstallation(customGradleInstallation);
        }

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
