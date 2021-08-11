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
package org.gradle.exemplar.test.runner;

import org.gradle.exemplar.loader.SamplesDiscovery;
import org.gradle.exemplar.model.Sample;
import org.junit.runners.model.InitializationError;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class EmbeddedSamplesRunner extends SamplesRunner {

    /**
     * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
     *
     * @param testClass reference to test class being run
     */
    public EmbeddedSamplesRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected List<Sample> getChildren() {
        File samplesRootDir = getSamplesRootDir();
        try {
            return SamplesDiscovery.embeddedSamples(samplesRootDir);
        } catch (IOException e) {
            throw new RuntimeException("Could not extract samples from " + samplesRootDir, e);
        }
    }
}
