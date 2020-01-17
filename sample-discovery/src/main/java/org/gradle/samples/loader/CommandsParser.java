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
package org.gradle.samples.loader;

import com.typesafe.config.*;
import org.gradle.samples.InvalidSampleException;
import org.gradle.samples.model.Command;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class CommandsParser {
    private static final String EXECUTABLE = "executable";
    private static final String COMMANDS = "commands";
    private static final String EXECUTION_SUBDIRECTORY = "execution-subdirectory";
    private static final String ARGS = "args";
    private static final String FLAGS = "flags";
    private static final String EXPECT_FAILURE = "expect-failure";
    private static final String ALLOW_ADDITIONAL_OUTPUT = "allow-additional-output";
    private static final String ALLOW_DISORDERED_OUTPUT = "allow-disordered-output";
    private static final String EXPECTED_OUTPUT_FILE = "expected-output-file";
    private static final String USER_INPUTS = "user-inputs";

    public static List<Command> parse(final File sampleConfigFile) {
        try {
            final Config sampleConfig = ConfigFactory.parseFile(sampleConfigFile, ConfigParseOptions.defaults().setAllowMissing(false)).resolve();
            final File sampleProjectDir = sampleConfigFile.getParentFile();

            List<Command> commands = new ArrayList<>();
            // Allow a single command to be specified without an enclosing list
            if (sampleConfig.hasPath(EXECUTABLE)) {
                commands.add(parseCommand(sampleConfig, sampleProjectDir));
            } else if (sampleConfig.hasPath(COMMANDS)) {
                for (Config stepConfig : sampleConfig.getConfigList(COMMANDS)) {
                    commands.add(parseCommand(stepConfig, sampleProjectDir));
                }
            } else {
                throw new InvalidSampleException("A sample must be defined with an 'executable' or 'commands'");
            }

            return commands;
        } catch (Exception e) {
            throw new InvalidSampleException(String.format("Could not read sample definition from %s.", sampleConfigFile), e);
        }
    }

    private static Command parseCommand(final Config commandConfig, final File sampleProjectDir) {
        // NOTE: A user must specify an executable. This prevents unexpected behavior when an empty or unexpected file is accidentally loaded
        String executable;
        try {
            executable = commandConfig.getString(EXECUTABLE);
        } catch (ConfigException e) {
            throw new InvalidSampleException("'executable' field cannot be empty", e);
        }
        final String executionDirectory = ConfigUtil.string(commandConfig, EXECUTION_SUBDIRECTORY, null);
        final List<String> commands = ConfigUtil.strings(commandConfig, ARGS, new ArrayList<String>());
        final List<String> flags = ConfigUtil.strings(commandConfig, FLAGS, new ArrayList<String>());
        String expectedOutput = null;
        if (commandConfig.hasPath(EXPECTED_OUTPUT_FILE)) {
            final File expectedOutputFile = new File(sampleProjectDir, commandConfig.getString(EXPECTED_OUTPUT_FILE));
            try {
                final Path path = Paths.get(expectedOutputFile.getAbsolutePath());
                expectedOutput = new String(Files.readAllBytes(path), Charset.forName("UTF-8"));
            } catch (IOException e) {
                throw new InvalidSampleException("Could not read sample output file " + expectedOutputFile.getAbsolutePath(), e);
            }
        }

        final boolean expectFailures = ConfigUtil.booleanValue(commandConfig, EXPECT_FAILURE, false);
        final boolean allowAdditionalOutput = ConfigUtil.booleanValue(commandConfig, ALLOW_ADDITIONAL_OUTPUT, false);
        final boolean allowDisorderedOutput = ConfigUtil.booleanValue(commandConfig, ALLOW_DISORDERED_OUTPUT, false);
        final List<String> userInputs = ConfigUtil.strings(commandConfig, USER_INPUTS, Collections.emptyList());

        return new Command(executable, executionDirectory, commands, flags, expectedOutput, expectFailures, allowAdditionalOutput, allowDisorderedOutput, userInputs);
    }
}
