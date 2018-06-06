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
package org.gradle.samples.test.verifier;

import org.junit.Assert;

import java.util.Arrays;
import java.util.List;

public class StrictOrderLineSegmentedOutputVerifier implements OutputVerifier {
    public void verify(final String expected, final String actual, final boolean allowAdditionalOutput) {
        List<String> expectedLines = Arrays.asList(expected.split("\\r?\\n"));
        List<String> actualLines = Arrays.asList(actual.split("\\r?\\n"));

        int index = 0;
        for (; index < actualLines.size() && index < expectedLines.size(); index++) {
            final String expectedLine = expectedLines.get(index);
            final String actualLine = actualLines.get(index);
            if (!expectedLine.equals(actualLine)) {
                if (expectedLine.contains(actualLine)) {
                    Assert.fail(String.format("Missing text at line %d.%nExpected: %s%nActual: %s%nActual output:%n%s%n", index + 1, expectedLine, actualLine, actual));
                }
                if (actualLine.contains(expectedLine)) {
                    Assert.fail(String.format("Extra text at line %d.%nExpected: %s%nActual: %s%nActual output:%n%s%n", index + 1, expectedLine, actualLine, actual));
                }
                Assert.fail(String.format("Unexpected value at line %d.%nExpected: %s%nActual: %s%nActual output:%n%s%n", index + 1, expectedLine, actualLine, actual));
            }
        }

        if (index == actualLines.size() && index < expectedLines.size()) {
            Assert.fail(String.format("Lines missing from actual result, starting at line %d.%nExpected: %s%nActual output:%n%s%n", index + 1, expectedLines.get(index), actual));
        }
        if (!allowAdditionalOutput && index < actualLines.size() && index == expectedLines.size()) {
            Assert.fail(String.format("Extra lines in actual result, starting at line %d.%nActual: %s%nActual output:%n%s%n", index + 1, actualLines.get(index), actual));
        }
    }
}
