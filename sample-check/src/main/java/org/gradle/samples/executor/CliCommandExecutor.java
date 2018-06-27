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

import java.io.File;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CliCommandExecutor extends CommandExecutor {
    public CliCommandExecutor(File directory) {
        super(directory);
    }

    @Override
    protected int run(final String executable, final List<String> commands, final List<String> flags, Map<String, String> environmentVariables, final OutputStream output) {
        List<String> commandLine = new ArrayList<>();
        commandLine.add(executable);
        commandLine.addAll(flags);
        commandLine.addAll(commands);

        try {
            run(new ProcessBuilder(commandLine), output);
        } catch (Exception e) {
            // TODO: get exit code
            return 1;
        }
        return 0;
    }
}
