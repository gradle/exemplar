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
package org.gradle.samples.test.normalizer;

import org.gradle.samples.executor.ExecutionMetadata;

import java.io.File;
import java.util.regex.Pattern;

public class FileSeparatorOutputNormalizer implements OutputNormalizer {
    @Override
    public String normalize(String commandOutput, ExecutionMetadata executionMetadata) {
        return normalize(commandOutput, executionMetadata, File.separatorChar);
    }

    protected String normalize(String commandOutput, ExecutionMetadata executionMetadata, char separatorChar) {
        return commandOutput.replaceAll(Pattern.quote(String.valueOf(separatorChar)) + "(?=\\w)", "/");
    }
}
