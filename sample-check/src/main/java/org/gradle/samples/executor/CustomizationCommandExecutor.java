package org.gradle.samples.executor;


import org.gradle.samples.model.Command;
import org.gradle.samples.test.customizer.CommandCustomizer;

import java.io.OutputStream;
import java.util.List;

public class CustomizationCommandExecutor extends CommandExecutor {
    private CommandExecutor delegate;
    private List<CommandCustomizer> customizers;

    public CustomizationCommandExecutor(CommandExecutor delegate, List<CommandCustomizer> customizers) {
        this.delegate = delegate;
        this.customizers = customizers;
    }

    @Override
    protected int run(String executable, List<String> args, List<String> flags, OutputStream output) {
        return delegate.run(executable, args, flags, output);
    }

    public CommandExecutionResult execute(final Command command, final ExecutionMetadata executionMetadata) {
        Command c = command;
        for (CommandCustomizer customizer : customizers) {
            c = customizer.customize(c);
        }
        return delegate.execute(c, executionMetadata);
    }
}
