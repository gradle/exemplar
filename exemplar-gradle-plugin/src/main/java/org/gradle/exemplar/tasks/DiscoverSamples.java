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
package org.gradle.exemplar.tasks;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.InputDirectory;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.SourceTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.samples.loader.SamplesDiscovery;
import org.gradle.samples.model.Sample;

import java.util.List;

public class DiscoverSamples extends SourceTask {
    private final RegularFileProperty outputFile;
    private final DirectoryProperty inputDir;

    public DiscoverSamples() {
        this.outputFile = newOutputFile();
        this.inputDir = getProject().getLayout().directoryProperty();
    }

    @InputDirectory
    public DirectoryProperty getInputDir() {
        return inputDir;
    }

    @OutputDirectory
    public RegularFileProperty getOutputFile() {
        return outputFile;
    }

    /**
     * Given an input directory, use {@link SamplesDiscovery} to discover all *.sample.conf
     * sample config files and write a file in JSON that can be used for indexing or running samples.
     */
    @TaskAction
    public void generate() {
        List<Sample> samples = SamplesDiscovery.externalSamples(getInputDir().getAsFile().get());

    }
}
