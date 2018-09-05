package org.gradle.samples.executor;


import org.gradle.samples.model.Command;
import org.gradle.samples.test.runner.CommandModifier;

import java.io.OutputStream;
import java.util.List;

public class ModifyingCommandExecutor extends CommandExecutor {
    private CommandExecutor delegate;
    private List<CommandModifier> commandModifiers;

    public ModifyingCommandExecutor(CommandExecutor delegate, List<CommandModifier> commandModifiers) {
        this.delegate = delegate;
        this.commandModifiers = commandModifiers;
    }

    @Override
    protected int run(String executable, List<String> args, List<String> flags, OutputStream output) {
        return delegate.run(executable, args, flags, output);
    }

    public CommandExecutionResult execute(final Command commandIn, final ExecutionMetadata executionMetadata) {
        Command command = commandIn;
        for (CommandModifier modifier : commandModifiers) {
            command = modifier.update(command);
        }
        return delegate.execute(command, executionMetadata);
    }
}
