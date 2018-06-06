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
package org.gradle.samples.test.verifier;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;

import java.util.Arrays;
import java.util.LinkedList;

public class AnyOrderLineSegmentedOutputVerifier implements OutputVerifier {
    private static final String NEWLINE = System.getProperty("line.separator");

    public void verify(final String expected, final String actual, final boolean allowAdditionalOutput) {
        // ArrayList does not support removal, and deletions for linked lists are O(1)
        LinkedList<String> expectedLines = new LinkedList<>(Arrays.asList(expected.replaceAll("(\\r?\\n)+", "\n").split("\\r?\\n")));
        LinkedList<String> unmatchedLines = new LinkedList<>(Arrays.asList(actual.replaceAll("(\\r?\\n)+", "\n").split("\\r?\\n")));

        for (String expectedLine : expectedLines) {
            String matchedLine = null;
            for (String unmatchedLine : unmatchedLines) {
                if (unmatchedLine.equals(expectedLine)) {
                    matchedLine = unmatchedLine;
                }
            }
            if (matchedLine != null) {
                unmatchedLines.remove(matchedLine);
            } else {
                Assert.fail(String.format("Line missing from output.%n%s%n---%nActual output:%n%s%n---", expectedLine, actual));
            }
        }

        if (!(allowAdditionalOutput || unmatchedLines.isEmpty())) {
            String unmatched = StringUtils.join(unmatchedLines, NEWLINE);
            Assert.fail(String.format("Extra lines in output.%n%s%n---%nActual output:%n%s%n---", unmatched, actual));
        }
    }
}
