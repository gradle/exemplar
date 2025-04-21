/*
 * Copyright 2025 the original author or authors.
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
package org.gradle.exemplar.test.runner

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.junit.platform.engine.discovery.DiscoverySelectors
import org.junit.platform.launcher.Launcher
import org.junit.platform.launcher.LauncherDiscoveryRequest
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import spock.lang.Specification

class BrokenSampleDiscoveryIntegrationTest extends Specification {
    @Rule
    TemporaryFolder tmpDir = new TemporaryFolder()

    def "start JUnit vintage engine via launcher"() {
        given:
        def brokenSample = tmpDir.newFile("broken.sample.conf")
        brokenSample << """
            executable: sleep
            args: 1
            expected-output-file: not-exist.sample.out
        """.stripMargin()

        def testClass = """
            package org.gradle.exemplar.test;
            
            import org.gradle.exemplar.test.runner.SamplesRunner;
            import org.gradle.exemplar.test.runner.SamplesRoot;
            import org.junit.runner.RunWith;

            @RunWith(SamplesRunner.class)
            @SamplesRoot("${tmpDir.root.absolutePath}")
            public class SimpleJUnit4Test {
            }
        """

        def testClassFile = tmpDir.newFile("SimpleJUnit4Test.java")
        testClassFile.text = testClass

        def compiler = new GroovyClassLoader()
        def compiledClass = compiler.parseClass(testClassFile)

        when:
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(DiscoverySelectors.selectClass(compiledClass))
                .build()

        Launcher launcher = LauncherFactory.create()
        def listener = new SummaryGeneratingListener()
        launcher.registerTestExecutionListeners(listener)

        launcher.execute(request)

        then:
        listener.summary.testsFailedCount == 1
        listener.summary.failures[0].exception.message.contains("Could not read sample definition")
    }
}
