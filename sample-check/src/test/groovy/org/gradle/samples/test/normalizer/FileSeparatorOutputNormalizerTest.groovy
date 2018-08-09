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
package org.gradle.samples.test.normalizer

import org.gradle.samples.executor.ExecutionMetadata
import spock.lang.Specification

class FileSeparatorOutputNormalizerTest extends Specification {
    ExecutionMetadata executionMetadata = new ExecutionMetadata(null, Collections.emptyMap())
    FileSeparatorOutputNormalizer normalizer = new FileSeparatorOutputNormalizer()

    def "avoids normalizing strings that aren't file paths"() {
        expect:
        normalizer.normalize("anything", executionMetadata) == "anything"
        normalizer.normalize("", executionMetadata) == ""
        normalizer.normalize("foo /--- bar", executionMetadata, '\\' as char) == "foo /--- bar"
        normalizer.normalize("foo /--- bar", executionMetadata, '/' as char) == "foo /--- bar"
        normalizer.normalize("foo /--- bar", executionMetadata, File.separatorChar) == "foo /--- bar"
    }

    def "replaces all file separators in paths to unix-style"() {
        expect:
        normalizer.normalize("Path C:\\Users\\username\\dir", executionMetadata, '\\' as char) == "Path C:/Users/username/dir"
    }
}
