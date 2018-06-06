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

import org.gradle.tooling.BuildException;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.LongRunningOperation;
import org.gradle.tooling.ProjectConnection;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class GradleToolingApiCommandExecutor extends CommandExecutor {
    private final ProjectConnection projectConnection;

    public GradleToolingApiCommandExecutor(final ProjectConnection projectConnection) {
        super();
        this.projectConnection = projectConnection;
    }

    public static <T extends LongRunningOperation, R> R run(final T operation, final Function<T, R> function) {
        operation.setStandardOutput(Logging.detailed());
        operation.setStandardError(Logging.detailed());
        try {
            return function.apply(operation);
        } catch (GradleConnectionException e) {
            System.out.println();
            System.out.println("ERROR: failed to run build. See log file for details.");
            System.out.println();
            throw e;
        }
    }

    @Override
    protected int run(String executable, List<String> commands, List<String> flags, Map<String, String> environmentVariables, OutputStream outputStream) {
        return run(projectConnection.newBuild(), buildLauncher -> {
            buildLauncher.forTasks(commands.toArray(new String[0]));
            buildLauncher.withArguments(flags);
            // Do not override environment unless specifically requested
            if (!environmentVariables.isEmpty()) {
                buildLauncher.setEnvironmentVariables(environmentVariables);
            }
            // NOTE: Both stdout and stderr go to the same output stream, just like what a typical user would see in their console
            buildLauncher.setStandardOutput(outputStream);
            buildLauncher.setStandardError(outputStream);
            try {
                buildLauncher.run();
            } catch (BuildException e) {
                return 1;
            }
            return 0;
        });
    }
}
