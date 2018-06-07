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
import org.gradle.tooling.BuildLauncher;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.ProjectConnection;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class GradleToolingApiCommandExecutor extends CommandExecutor {
    private final ProjectConnection projectConnection;

    public GradleToolingApiCommandExecutor(final ProjectConnection projectConnection) {
        super();
        this.projectConnection = projectConnection;
    }

    @Override
    protected int run(String executable, List<String> commands, List<String> flags, Map<String, String> environmentVariables, OutputStream outputStream) {
        try {
            BuildLauncher build = projectConnection.newBuild();
            build.forTasks(commands.toArray(new String[0]));
            build.withArguments(flags);

            if (!environmentVariables.isEmpty()) {
                build.setEnvironmentVariables(environmentVariables);
            }

            // NOTE: Both stdout and stderr go to the same output stream, just like what a typical user would see in their console
            build.setStandardOutput(outputStream);
            build.setStandardError(outputStream);
            build.setJvmArguments("-Xmx512m", "-XX:MaxPermSize=128m");

            build.run();
        } catch (BuildException e) {
            return 1;
        } catch (GradleConnectionException e) {
            System.out.println("ERROR: failed to run build. See log file for details.");
            throw e;
        } finally {
            projectConnection.close();
        }
        return 0;
    }
}
