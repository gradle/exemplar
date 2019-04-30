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
package org.gradle.samples.model

import org.gradle.samples.InvalidSampleException
import org.gradle.samples.loader.CommandsParser
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class CommandsParserTest extends Specification {
    @Rule
    TemporaryFolder tmpDir = new TemporaryFolder()
    File sampleConfigFile

    def setup() {
        sampleConfigFile = tmpDir.newFile("default.sample.conf")
    }

    def "fails fast when config file is badly formed"() {
        given:
        sampleConfigFile << "broken!"

        when:
        CommandsParser.parse(sampleConfigFile)

        then:
        def e = thrown(InvalidSampleException)
        e.message == "Could not read sample definition from ${sampleConfigFile}."
        e.cause != null
    }

    def "fails fast given no executable or commands array specified"() {
        given:
        sampleConfigFile << "bogus { unexpected: true }"
        tmpDir.newFile("bogus.sample.out").createNewFile()

        when:
        CommandsParser.parse(sampleConfigFile)

        then:
        def e = thrown(InvalidSampleException)
        e.message == "Could not read sample definition from ${sampleConfigFile}."
        e.cause.message == "A sample must be defined with an 'executable' or 'commands'"
    }

    def "fails fast when no executable for command specified"() {
        given:
        sampleConfigFile << """
            commands: [{ }, { }]
        """

        when:
        CommandsParser.parse(sampleConfigFile)

        then:
        def e = thrown(InvalidSampleException)
        e.message == "Could not read sample definition from ${sampleConfigFile}."
        e.cause.message == "'executable' field cannot be empty"
    }

    def "provides reasonable defaults for command config"() {
        given:
        sampleConfigFile << "executable: hello"

        when:
        List<Command> commands = CommandsParser.parse(sampleConfigFile)
        Command command = commands[0]

        then:
        commands.size() == 1
        command.executable == "hello"
        command.executionSubdirectory == null
        command.args == []
        command.flags == []
        !command.expectFailure
        command.expectedOutput == null
        !command.allowAdditionalOutput
        !command.allowDisorderedOutput
    }

    def "loads custom values for all command config options"() {
        given:
        sampleConfigFile << """
            executable: gradle
            execution-subdirectory: subproj
            args: build
            flags: -I init.gradle.kts
            expect-failure: true
            allow-additional-output: true
            allow-disordered-output: true
            expected-output-file: customLoggerKts.out
        """
        File expectedOutputFile = tmpDir.newFile("customLoggerKts.out")
        expectedOutputFile << "> Task :build"

        when:
        List<Command> commands = CommandsParser.parse(sampleConfigFile)
        Command command = commands[0]

        then:
        commands.size() == 1
        command.executable == "gradle"
        command.executionSubdirectory == "subproj"
        command.args == ["build"]
        command.flags == ["-I", "init.gradle.kts"]
        command.expectFailure
        command.expectedOutput == "> Task :build"
        command.allowAdditionalOutput
        command.allowDisorderedOutput
    }

    def "parses multiple steps"() {
        given:
        sampleConfigFile << """
            commands: [{
                executable: gradle
                execution-subdirectory: subproj
                args: produce
            },{
                executable: gradle
                execution-subdirectory: otherproject
                args: consume
                flags: --quiet
                expect-failure: true
                allow-additional-output: true
                allow-disordered-output: true
                expected-output-file: customLoggerKts.out
            }]
        """
        File expectedOutputFile = tmpDir.newFile("customLoggerKts.out")
        expectedOutputFile << "> Task :build"

        when:
        List<Command> commands = CommandsParser.parse(sampleConfigFile)

        then:
        commands.size() == 2

        Command firstCommand = commands[0]
        firstCommand.executable == "gradle"
        firstCommand.executionSubdirectory == "subproj"
        firstCommand.args == ["produce"]
        !firstCommand.expectFailure

        Command secondCommand = commands[1]
        secondCommand.executable == "gradle"
        secondCommand.executionSubdirectory == "otherproject"
        secondCommand.args == ["consume"]
        secondCommand.flags == ["--quiet"]
        secondCommand.expectFailure
        secondCommand.expectedOutput == "> Task :build"
        secondCommand.allowAdditionalOutput
        secondCommand.allowDisorderedOutput
    }

    def "loads included config"() {
        given:
        File normalizersConfigFile = tmpDir.newFile("normalizers.conf")
        normalizersConfigFile << """
            flags: --init-script foo.bar.kts
"""

        sampleConfigFile << """
            executable: gradle
            args: build
            include file("${normalizersConfigFile.absolutePath.replace((char) '\\', (char) '/')}")
        """
        tmpDir.newFile("default.sample.out").createNewFile()

        when:
        List<Command> commands = CommandsParser.parse(sampleConfigFile)
        Command command = commands[0]

        then:
        commands.size() == 1
        command.executable == "gradle"
        command.args == ["build"]
        command.flags == ["--init-script", "foo.bar.kts"]
    }
}
