package org.gradle.exemplar.test.engine;

import org.gradle.exemplar.executor.CommandExecutor;

import java.util.function.Function;

public interface CommandExecutorFunction extends Function<CommandExecutorParams, CommandExecutor> {
}
