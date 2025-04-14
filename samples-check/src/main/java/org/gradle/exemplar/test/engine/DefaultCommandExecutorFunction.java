package org.gradle.exemplar.test.engine;

import org.gradle.exemplar.executor.CliCommandExecutor;
import org.gradle.exemplar.executor.CommandExecutor;

public class DefaultCommandExecutorFunction implements CommandExecutorFunction {
    @Override
    public CommandExecutor apply(CommandExecutorParams params) {
        return new CliCommandExecutor(params.workingDir);
    }
}
