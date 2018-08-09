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

import org.gradle.internal.impldep.com.google.common.collect.Lists;
import org.gradle.samples.loader.SamplesDiscovery;
import org.gradle.samples.model.Sample;
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
        List<Sample> samplesFromDirectory = getEmbeddedSamplesFromAsciidocSources();
        List<Sample> result = Lists.newArrayList();
        result.addAll(samplesFromDirectory);
        return result;
    }

    private List<Sample> getEmbeddedSamplesFromAsciidocSources() {
        AsciidocSourcesRoot asciidocSourcesRoot = getTestClass().getAnnotation(AsciidocSourcesRoot.class);
        File asciidocSourcesRootDir;
        try {
            if (asciidocSourcesRoot != null) {
                asciidocSourcesRootDir = new File(asciidocSourcesRoot.value());
            } else {
                throw new InitializationError("Asciidoctor sources root is not declared. Please annotate your test class with @AsciidocSourcesRoot(\"path/to/docs/\")");
            }

            if (!asciidocSourcesRootDir.exists()) {
                throw new InitializationError("Directory " + asciidocSourcesRoot.value() + " does not exist. NOTE: it is relative to the Gradle (sub)project rootDir.");
            }
            return SamplesDiscovery.embeddedSamples(asciidocSourcesRootDir);
        } catch (InitializationError e) {
            throw new RuntimeException("Could not initialize SamplesRunner", e);
        } catch (IOException e) {
            throw new RuntimeException("Could not extract samples from " + asciidocSourcesRoot.value(), e);
        }
    }
}
