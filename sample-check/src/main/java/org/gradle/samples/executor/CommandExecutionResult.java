/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.samples.executor;

import org.gradle.samples.model.Command;

public class CommandExecutionResult {
    private final String output;
    private final int exitCode;
    private final Command command;
    private final ExecutionMetadata executionMetadata;

    public CommandExecutionResult(final Command command, final int exitCode, final String output, final ExecutionMetadata executionMetadata) {
        this.command = command;
        this.exitCode = exitCode;
        this.output = output;
        this.executionMetadata = executionMetadata;
    }

    public Command getCommand() {
        return command;
    }

    public String getOutput() {
        return output;
    }

    public int getExitCode() {
        return exitCode;
    }

    public ExecutionMetadata getExecutionMetadata() {
        return executionMetadata;
    }
}
