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
package org.gradle.samples.test.runner;

import org.gradle.samples.loader.SamplesDiscovery;
import org.gradle.samples.model.Sample;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.util.List;

/**
 * A custom implementation of {@link SamplesRunner} that uses the Gradle Tooling API to execute sample builds.
 */
public class GradleEmbeddedSamplesRunner extends GradleSamplesRunner {
    /**
     * {@inheritDoc}
     */
    public GradleEmbeddedSamplesRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected List<Sample> getChildren() {
        try {
            return SamplesDiscovery.embeddedSamples(getSamplesRootDir());
        } catch (IOException e) {
            throw new RuntimeException("Could not extract embedded samples", e);
        }
    }
}
