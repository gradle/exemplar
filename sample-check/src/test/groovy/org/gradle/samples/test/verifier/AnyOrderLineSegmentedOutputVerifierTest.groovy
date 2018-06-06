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
package org.gradle.samples.test.verifier

import spock.lang.Specification

class AnyOrderLineSegmentedOutputVerifierTest extends Specification {
    private static final String NL = System.getProperty("line.separator")
    OutputVerifier verifier = new AnyOrderLineSegmentedOutputVerifier()

    def "checks all expected lines exist in any order with no extra output"() {
        given:
        String expected = """
message 1
message 2
"""
        String actual = """
message 2
message 1
"""

        when:
        verifier.verify(expected, actual, false)

        then:
        notThrown(AssertionError)
    }

    def "checks all expected lines exist in any order with extra output"() {
        given:
        String expected = """
message 1
message 2
"""
        String actual = """
`
message 1


extra output
"""

        when:
        verifier.verify(expected, actual, true)

        then:
        AssertionError assertionError = thrown(AssertionError)
        assertionError.message.contains("""Line missing from output.${NL}message 2""")
    }
}
