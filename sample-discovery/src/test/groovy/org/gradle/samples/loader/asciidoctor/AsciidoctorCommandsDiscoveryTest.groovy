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
package org.gradle.samples.loader.asciidoctor

import org.asciidoctor.AttributesBuilder
import org.asciidoctor.SafeMode
import org.gradle.samples.model.Command
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

@Subject(AsciidoctorCommandsDiscovery)
class AsciidoctorCommandsDiscoveryTest extends Specification {
    @Rule
    TemporaryFolder tmpDir = new TemporaryFolder()

    def "discovers samples inside an asciidoctor file with sources inline"() {
        given:
        def file = tmpDir.newFile('sample.adoc') << '''
            |= Document Title
            |
            |[.sample-command,allow-disordered-output=true]
            |----
            |$ ruby hello.rb
            |
            |hello, world
            |
            |some more output
            |----
            |'''.stripMargin()

        when:
        Collection<Command> commands = AsciidoctorCommandsDiscovery.extractFromAsciidoctorFile(file)

        then:
        commands.size() == 1
        def command = commands.get(0)
        command.executable == 'ruby'
        command.args == ['hello.rb']
        command.allowDisorderedOutput
        command.expectedOutput == '''
            |hello, world
            |
            |some more output'''.stripMargin()
    }

    def "sample may include multiple sample-command blocks"() {
        given:
        def file = tmpDir.newFile('sample.adoc') << '''
            |= Document Title
            |
            |Run this first:
            |
            |[.sample-command]
            |----
            |$ ruby hello.rb
            |hello, world
            |----
            |
            |Then do this:
            |
            |[.sample-command]
            |----
            |$ mkdir some-dir
            |----
            |'''.stripMargin()

        when:
        Collection<Command> commands = AsciidoctorCommandsDiscovery.extractFromAsciidoctorFile(file)

        then:
        commands.size() == 2
        def command = commands.get(0)
        command.executable == 'ruby'
        command.args == ['hello.rb']
        command.expectedOutput == 'hello, world'

        def command2 = commands.get(1)
        command2.executable == 'mkdir'
        command2.args == ['some-dir']
        command2.expectedOutput.empty
    }

    def "sample-command block may include multiple commands"() {
        given:
        def file = tmpDir.newFile('sample.adoc') << '''
            |= Document Title
            |
            |Run this first:
            |
            |[.sample-command]
            |----
            |$ ruby hello.rb
            |
            |hello, world
            |
            |$ mkdir some-dir
            |$ cd some-dir
            |----
            |'''.stripMargin()

        when:
        Collection<Command> commands = AsciidoctorCommandsDiscovery.extractFromAsciidoctorFile(file)

        then:
        commands.size() == 3
        def command = commands.get(0)
        command.executable == 'ruby'
        command.args == ['hello.rb']
        command.expectedOutput == '''
            |hello, world
            |'''.stripMargin()

        def command2 = commands.get(1)
        command2.executable == 'mkdir'
        command2.args == ['some-dir']
        command2.expectedOutput.empty

        def command3 = commands.get(2)
        command3.executable == 'cd'
        command3.args == ['some-dir']
        command3.expectedOutput.empty
    }

    def "can include data from attributes"() {
        given:
        def file = tmpDir.newFile('sample.adoc') << '''
            |= Document Title
            |
            |Run this first:
            |
            |[listing.terminal.sample-command]
            |----
            |$ pwd
            |include::{sampleoutputdir}/pwd-output.txt[]
            |----
            |'''.stripMargin()
        def outputDir = tmpDir.newFolder('output')
        tmpDir.newFile('output/pwd-output.txt').text = "${tmpDir.root.getAbsolutePath()}\n"

        when:
        Collection<Command> commands = AsciidoctorCommandsDiscovery.extractFromAsciidoctorFile(file) {
            it.attributes(AttributesBuilder.attributes().attribute('sampleoutputdir', outputDir.getAbsolutePath())).safe(SafeMode.UNSAFE)
        }

        then:
        commands.size() == 1
        def command = commands.get(0)
        command.executable == 'pwd'
        command.args == []
        command.expectedOutput == tmpDir.root.getAbsolutePath()
    }

    def "can extract commands when using Asciidoctor callout"() {
        given:
        def file = tmpDir.newFile('sample.adoc') << '''
            |= Document Title
            |
            |[listing.terminal.sample-command]
            |----
            |$ ./command
            |Some output // <1>
            |----
            |<1> Some callout
            |'''.stripMargin()

        when:
        Collection<Command> commands = AsciidoctorCommandsDiscovery.extractFromAsciidoctorFile(file)

        then:
        commands.size() == 1
        def command = commands.get(0)
        command.executable == './command'
        command.args == []
        command.expectedOutput == 'Some output // <1>'
    }

    def "can extract empty user inputs when none is specified"() {
        given:
        def file = tmpDir.newFile('sample.adoc') << '''
            |= Document Title
            |
            |[listing.terminal.sample-command]
            |----
            |$ ./command
            |Some output
            |----
            |'''.stripMargin()

        when:
        Collection<Command> commands = AsciidoctorCommandsDiscovery.extractFromAsciidoctorFile(file)

        then:
        commands.size() == 1
        def command = commands.get(0)
        command.userInputs.size() == 0
    }

    def "can extract user inputs to the command"() {
        given:
        def file = tmpDir.newFile('sample.adoc') << '''
            |= Document Title
            |
            |[listing.terminal.sample-command,user-inputs="1||yes"]
            |----
            |$ ./command
            |Some output
            |----
            |'''.stripMargin()

        when:
        Collection<Command> commands = AsciidoctorCommandsDiscovery.extractFromAsciidoctorFile(file)

        then:
        commands.size() == 1
        def command = commands.get(0)
        command.userInputs.size() == 3
        command.userInputs.get(0) == '1'
        command.userInputs.get(1) == ''
        command.userInputs.get(2) == 'yes'
    }
}
