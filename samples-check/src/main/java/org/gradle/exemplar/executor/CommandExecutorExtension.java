package org.gradle.exemplar.executor;

import org.gradle.exemplar.model.Command;

import java.io.File;

public interface CommandExecutorExtension {

    CommandExecutionResult execute(final Command command, final ExecutionMetadata executionMetadata, File workingDir);

}
