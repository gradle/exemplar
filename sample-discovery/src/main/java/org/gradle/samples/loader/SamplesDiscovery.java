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
package org.gradle.samples.loader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Options;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.extension.JavaExtensionRegistry;
import org.gradle.samples.model.Command;
import org.gradle.samples.model.Sample;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class SamplesDiscovery {
    public static List<Sample> allSamples(File rootSamplesDir) {
        // The .sample.conf suffix makes it clear that this is a HOCON file specifically for samples
        return filteredSamples(rootSamplesDir, new String[]{"sample.conf"}, true);
    }

    public static List<Sample> filteredSamples(File rootSamplesDir, String[] fileExtensions, boolean recursive) {
        Collection<File> sampleConfigFiles = FileUtils.listFiles(rootSamplesDir, fileExtensions, recursive);

        List<Sample> samples = new ArrayList<>();
        for (File sampleConfigFile : sampleConfigFiles) {
            final String id = generateSampleId(rootSamplesDir, sampleConfigFile);
            final List<Command> commands = CommandsParser.parse(sampleConfigFile);
            final File sampleProjectDir = sampleConfigFile.getParentFile();
            samples.add(new Sample(id, sampleProjectDir, commands));
        }

        return samples;
    }

    private static String generateSampleId(File rootSamplesDir, File scenarioFile) {
        String prefix = rootSamplesDir
                .toPath()
                .relativize(scenarioFile.getParentFile().toPath())
                .toString()
                .replaceAll("[/\\\\]", "_");
        return prefix + "_" + FilenameUtils.removeExtension(scenarioFile.getName());
    }

    public static List<Sample> allSamplesFromDocument(File documentFile) {
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        JavaExtensionRegistry extensionRegistry = asciidoctor.javaExtensionRegistry();
        SampleBlockProcessor processor = new SampleBlockProcessor();
//        extensionRegistry.block("sample", processor);
        extensionRegistry.block("sample", SampleBlockProcessor.class);
        asciidoctor.convertFile(documentFile, new Options());
        return processor.getSamples();
    }
}
