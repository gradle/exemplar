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


import org.gradle.samples.model.Sample
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class AsciidoctorSamplesDiscoveryTest extends Specification {
    @Rule
    TemporaryFolder tmpDir = new TemporaryFolder()

    def "discovers samples inside an asciidoctor file with sources inline"() {
        given:
        def file = tmpDir.newFile("sample.adoc") << """
= Document Title

.Sample title
====
[.testable-sample]
=====
.hello.rb
[source,ruby]
----
target = "world"
puts "hello, #{target}"
----

[.sample-command,allow-disordered-output=true]
----
\$ ruby hello.rb
hello, world
----
=====
====
"""

        when:
        Collection<Sample> samples = AsciidoctorSamplesDiscovery.extractFromAsciidoctorFile(file)

        then:
        samples.size() == 1
        def commands = samples.get(0).commands

        and:
        commands.size() == 1
        def command = commands.get(0)
        command.executable == "ruby"
        command.args == ["hello.rb"]
        command.allowDisorderedOutput
    }

    def "discovers samples inside an asciidoctor file with sources included"() {
        given:
        tmpDir.newFolder("src", "samples", "bash")
        tmpDir.newFile("src/samples/bash/script.sh") << """
#!/usr/bin/env bash

echo "Hello world"
"""
        def file = tmpDir.newFile("sample.adoc") << """
= Document title

.Sample title
====
[.testable-sample,dir="src/samples/bash"]
=====
.script.sh
[source,bash]
----
include::src/samples/bash/script.sh[]
----

[.sample-command]
----
\$ bash script.sh
Hello world
----
=====
====
"""

        when:
        Collection<Sample> samples = AsciidoctorSamplesDiscovery.extractFromAsciidoctorFile(file)

        then:
        samples.size() == 1
        samples.get(0).projectDir.toString() == "src/samples/bash"
        def commands = samples.get(0).commands

        and:
        commands.size() == 1
        def command = commands.get(0)
        command.executable == "bash"
        command.args == ["script.sh"]
    }
}
