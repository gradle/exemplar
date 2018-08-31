package org.gradle.samples.test.customizer;

import org.gradle.samples.model.Command;

import java.util.List;

public class CommandBuilder {
    private String executable;
    private String executionSubdirectory;
    private List<String> args;
    private List<String> flags;
    private String expectedOutput;
    private boolean expectFailure;
    private boolean allowAdditionalOutput;
    private boolean allowDisorderedOutput;

    private CommandBuilder(String executable, String executionDirectory, List<String> args, List<String> flags, String expectedOutput, boolean expectFailure, boolean allowAdditionalOutput, boolean allowDisorderedOutput) {
        this.executable = executable;
        this.executionSubdirectory = executionDirectory;
        this.args = args;
        this.flags = flags;
        this.expectedOutput = expectedOutput;
        this.expectFailure = expectFailure;
        this.allowAdditionalOutput = allowAdditionalOutput;
        this.allowDisorderedOutput = allowDisorderedOutput;
    }

    public static CommandBuilder fromCommand(Command command) {
        return new CommandBuilder(command.getExecutable(),
                command.getExecutionSubdirectory(),
                command.getArgs(),
                command.getFlags(),
                command.getExpectedOutput(),
                command.isExpectFailure(),
                command.isAllowAdditionalOutput(),
                command.isAllowDisorderedOutput());
    }

    public CommandBuilder setExecutable(String executable) {
        this.executable = executable;
        return this;
    }

    public CommandBuilder setExecutionSubdirectory(String executionSubdirectory) {
        this.executionSubdirectory = executionSubdirectory;
        return this;
    }

    public CommandBuilder setArgs(List<String> args) {
        this.args = args;
        return this;
    }

    public CommandBuilder setFlags(List<String> flags) {
        this.flags = flags;
        return this;
    }

    public CommandBuilder setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
        return this;
    }

    public CommandBuilder setExpectFailure(boolean expectFailure) {
        this.expectFailure = expectFailure;
        return this;
    }

    public CommandBuilder setAllowAdditionalOutput(boolean allowAdditionalOutput) {
        this.allowAdditionalOutput = allowAdditionalOutput;
        return this;
    }

    public CommandBuilder setAllowDisorderedOutput(boolean allowDisorderedOutput) {
        this.allowDisorderedOutput = allowDisorderedOutput;
        return this;
    }

    public Command build() {
        return new Command(executable, executionSubdirectory, args, flags, expectedOutput, expectFailure, allowAdditionalOutput, allowDisorderedOutput);
    }
}
