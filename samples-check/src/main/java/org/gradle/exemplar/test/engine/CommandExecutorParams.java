package org.gradle.exemplar.test.engine;

import org.gradle.exemplar.executor.ExecutionMetadata;
import org.gradle.exemplar.model.Command;

import java.io.File;

public class CommandExecutorParams {
    final ExecutionMetadata executionMetadata;
    final File workingDir;
    final Command command;

    public CommandExecutorParams(ExecutionMetadata executionMetadata, File workingDir, Command command) {
        this.executionMetadata = executionMetadata;
        this.workingDir = workingDir;
        this.command = command;
    }

}
