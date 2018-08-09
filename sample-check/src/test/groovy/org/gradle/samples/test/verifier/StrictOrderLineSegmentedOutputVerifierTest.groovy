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

class StrictOrderLineSegmentedOutputVerifierTest extends Specification {
    private static final String NL = System.getProperty("line.separator")
    OutputVerifier verifier = new StrictOrderLineSegmentedOutputVerifier()

    def "checks all expected lines exist in sequential order disallowing extra output"() {
        given:
        String expected = """
message 1
message 2
"""
        String actual = """
message 1
message 2
"""

        when:
        verifier.verify(expected, actual, false)

        then:
        notThrown(AssertionError)
    }

    def "checks all expected lines exist in sequential order with extra output"() {
        given:
        String expected = """
message 1
message 2
"""
        String actual = """
message 1
message 2

extra logs
"""

        when:
        verifier.verify(expected, actual, true)

        then:
        notThrown(AssertionError)
    }

    def "checks all expected lines exist in sequential order with extra output at the beginning"() {
        given:
        String expected = """message 1
message 2
"""
        String actual = """
extra logs

message 1
message 2
"""

        when:
        verifier.verify(expected, actual, true)

        then:
        notThrown(AssertionError)
    }

    def "fails when expected lines not found while disallowing extra output"() {
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
        AssertionError assertionError = thrown(AssertionError)
        assertionError.message.contains("""Unexpected value at line 2.${NL}Expected: message 1${NL}Actual: message 2""")
    }

    def "fails with extra output while disallowing extra output"() {
        given:
        String expected = """
message 1
message 2
"""
        String actual = """
message 1
message 2

extra logs
"""

        when:
        verifier.verify(expected, actual, false)

        then:
        AssertionError assertionError = thrown(AssertionError)
        assertionError.message.contains('Extra lines in actual result, starting at line 4.')
    }

    def "fails when expected lines not found while allowing extra output"() {
        given:
        String expected = """message 1
message 2
"""
        String actual = """
extra logs

message 3
message 4

extra logs 2
"""

        when:
        verifier.verify(expected, actual, true)

        then:
        def error = thrown(AssertionError)
        error.message.contains('Lines missing from actual result, starting at expected line 0.')
    }
}
