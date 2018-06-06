/*
 * Copyright 2018 the original author or authors.
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
package org.gradle.samples.model;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class Command {
    private final String executable;
    private final String executionSubdirectory;
    private final List<String> args;
    private final List<String> flags;
    private final Map<String, String> environmentVariables;
    private final String expectedOutput;
    private final boolean expectFailure;
    private final boolean allowAdditionalOutput;
    private final boolean allowDisorderedOutput;

    public Command(@Nonnull String executable, @Nullable String executionDirectory, List<String> args, List<String> flags, Map<String, String> environmentVariables, @Nullable String expectedOutput, boolean expectFailure, boolean allowAdditionalOutput, boolean allowDisorderedOutput) {
        this.executable = executable;
        this.executionSubdirectory = executionDirectory;
        this.args = args;
        this.flags = flags;
        this.environmentVariables = environmentVariables;
        this.expectedOutput = expectedOutput;
        this.expectFailure = expectFailure;
        this.allowAdditionalOutput = allowAdditionalOutput;
        this.allowDisorderedOutput = allowDisorderedOutput;
    }

    @Nonnull
    public String getExecutable() {
        return executable;
    }

    @Nullable
    public String getExecutionSubdirectory() {
        return executionSubdirectory;
    }

    public List<String> getArgs() {
        return args;
    }

    public List<String> getFlags() {
        return flags;
    }

    @Nullable
    public String getExpectedOutput() {
        return expectedOutput;
    }

    /**
     * @return true if executing the scenario build is expected to fail.
     */
    public boolean isExpectFailure() {
        return expectFailure;
    }

    /**
     * @return true if output lines other than those provided are allowed.
     */
    public boolean isAllowAdditionalOutput() {
        return allowAdditionalOutput;
    }

    /**
     * @return true if actual output lines can differ in order from expected.
     */
    public boolean isAllowDisorderedOutput() {
        return allowDisorderedOutput;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }
}
