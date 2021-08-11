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
package org.gradle.exemplar.test.normalizer;

import org.gradle.exemplar.executor.ExecutionMetadata;

public class JavaObjectSerializationOutputNormalizer implements OutputNormalizer {
    @Override
    public String normalize(String commandOutput, ExecutionMetadata executionMetadata) {
        return commandOutput.replaceAll("(\\w+(\\.\\w+)*)@\\p{XDigit}+", "$1@12345");
    }
}
