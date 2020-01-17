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
import java.util.Collections;
import java.util.List;

public class Command {
    private final String executable;
    private final String executionSubdirectory;
    private final List<String> args;
    private final List<String> flags;
    private final String expectedOutput;
    private final boolean expectFailure;
    private final boolean allowAdditionalOutput;
    private final boolean allowDisorderedOutput;
    private final List<String> userInputs;

    public Command(@Nonnull String executable, @Nullable String executionDirectory, List<String> args, List<String> flags, @Nullable String expectedOutput, boolean expectFailure, boolean allowAdditionalOutput, boolean allowDisorderedOutput, List<String> userInputs) {
        this.executable = executable;
        this.executionSubdirectory = executionDirectory;
        this.args = args;
        this.flags = flags;
        this.expectedOutput = expectedOutput;
        this.expectFailure = expectFailure;
        this.allowAdditionalOutput = allowAdditionalOutput;
        this.allowDisorderedOutput = allowDisorderedOutput;
        this.userInputs = userInputs;
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

    /**
     * @return a list of user inputs to provide to the command
     */
    public List<String> getUserInputs() {
        return userInputs;
    }

    public Builder toBuilder() {
        return new Builder(getExecutable(),
                getExecutionSubdirectory(),
                getArgs(),
                getFlags(),
                getExpectedOutput(),
                isExpectFailure(),
                isAllowAdditionalOutput(),
                isAllowDisorderedOutput(),
                getUserInputs());
    }

    public static class Builder {
        private String executable;
        private String executionSubdirectory;
        private List<String> args;
        private List<String> flags;
        private String expectedOutput;
        private boolean expectFailure;
        private boolean allowAdditionalOutput;
        private boolean allowDisorderedOutput;
        private List<String> userInputs;

        private Builder(String executable, String executionDirectory, List<String> args, List<String> flags, String expectedOutput, boolean expectFailure, boolean allowAdditionalOutput, boolean allowDisorderedOutput, List<String> userInputs) {
            this.executable = executable;
            this.executionSubdirectory = executionDirectory;
            this.args = args;
            this.flags = flags;
            this.expectedOutput = expectedOutput;
            this.expectFailure = expectFailure;
            this.allowAdditionalOutput = allowAdditionalOutput;
            this.allowDisorderedOutput = allowDisorderedOutput;
            this.userInputs = userInputs;
        }

        public Builder setExecutable(String executable) {
            this.executable = executable;
            return this;
        }

        public Builder setExecutionSubdirectory(String executionSubdirectory) {
            this.executionSubdirectory = executionSubdirectory;
            return this;
        }

        public Builder setArgs(List<String> args) {
            this.args = args;
            return this;
        }

        public Builder setFlags(List<String> flags) {
            this.flags = flags;
            return this;
        }

        public Builder setExpectedOutput(String expectedOutput) {
            this.expectedOutput = expectedOutput;
            return this;
        }

        public Builder setExpectFailure(boolean expectFailure) {
            this.expectFailure = expectFailure;
            return this;
        }

        public Builder setAllowAdditionalOutput(boolean allowAdditionalOutput) {
            this.allowAdditionalOutput = allowAdditionalOutput;
            return this;
        }

        public Builder setAllowDisorderedOutput(boolean allowDisorderedOutput) {
            this.allowDisorderedOutput = allowDisorderedOutput;
            return this;
        }

        public Command build() {
            return new Command(executable, executionSubdirectory, args, flags, expectedOutput, expectFailure, allowAdditionalOutput, allowDisorderedOutput, userInputs);
        }
    }
}
