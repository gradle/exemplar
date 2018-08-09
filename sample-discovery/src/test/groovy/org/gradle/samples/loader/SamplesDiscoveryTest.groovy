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
package org.gradle.samples.loader

import org.gradle.samples.model.Sample
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class SamplesDiscoveryTest extends Specification {
    @Rule
    TemporaryFolder tmpDir = new TemporaryFolder()

    def "discovers nested samples"() {
        given:
        tmpDir.newFolder("basic-sample")
        tmpDir.newFile("basic-sample/default.sample.conf") << "executable: help"
        tmpDir.newFolder("advanced-sample", "nested")
        tmpDir.newFile("advanced-sample/shallow.sample.conf") << "commands: [{executable: foo}]"
//        tmpDir.newFile("advanced-sample/nested/crazy.sample.conf") << "commands: [{executable: build}, {executable: cleanup}]"

        when:
        Collection<Sample> samples = SamplesDiscovery.externalSamples(tmpDir.root)

        then:
        samples.size() == 2
    }

    def "allows custom file filter"() {
        given:
        tmpDir.newFolder("first-sample")
        tmpDir.newFile("first-sample/default.sample") << "executable: help"
        tmpDir.newFolder("src", "play")
        tmpDir.newFile("src/play/bogus.conf") << "I'm not a sample file"

        when:
        Collection<Sample> samples = SamplesDiscovery.filteredExternalSamples(tmpDir.root, ["sample"].toArray() as String[], true)

        then:
        samples.size() == 1
        samples[0].id == "first-sample_default"
    }
}
