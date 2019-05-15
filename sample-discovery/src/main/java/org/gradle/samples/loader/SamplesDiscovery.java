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
import org.gradle.samples.loader.asciidoctor.AsciidoctorSamplesDiscovery;
import org.gradle.samples.model.Command;
import org.gradle.samples.model.Sample;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SamplesDiscovery {
    public static List<Sample> externalSamples(File rootSamplesDir) {
        // The .sample.conf suffix makes it clear that this is a HOCON file specifically for samples
        return filteredExternalSamples(rootSamplesDir, new String[]{"sample.conf"}, true);
    }

    public static List<Sample> filteredExternalSamples(File rootSamplesDir, String[] fileExtensions, boolean recursive) {
        Collection<File> sampleConfigFiles = FileUtils.listFiles(rootSamplesDir, fileExtensions, recursive);

        List<Sample> samples = new ArrayList<>();
        for (File sampleConfigFile : sampleConfigFiles) {
            final String id = generateSampleId(rootSamplesDir, sampleConfigFile);
            final List<Command> commands = CommandsParser.parse(sampleConfigFile);
            // FIXME: Currently the temp directory used when running sample-check has a different name.
            // This causes Gradle project names to differ when one is not explicitly set in settings.gradle. This should be preserved.
            final File sampleProjectDir = sampleConfigFile.getParentFile();
            samples.add(new Sample(id, sampleProjectDir, commands));
        }
        // Always return (and test) samples in a fixed order
        sortSamples(samples);

        return samples;
    }

    public static List<Sample> embeddedSamples(File asciidocSrcDir) throws IOException {
        return filteredEmbeddedSamples(asciidocSrcDir, new String[]{"adoc", "asciidoc"}, true);
    }

    public static List<Sample> filteredEmbeddedSamples(File rootSamplesDir, String[] fileExtensions, boolean recursive) throws IOException {
        Collection<File> sampleConfigFiles = FileUtils.listFiles(rootSamplesDir, fileExtensions, recursive);

        List<Sample> samples = new ArrayList<>();
        for (File sampleConfigFile : sampleConfigFiles) {
            samples.addAll(AsciidoctorSamplesDiscovery.extractFromAsciidoctorFile(sampleConfigFile));
        }
        sortSamples(samples);

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

    private static void sortSamples(List<Sample> samples) {
        Collections.sort(samples, new SampleComparator());
    }

    private static class SampleComparator implements Comparator<Sample> {
        @Override
        public int compare(Sample s1, Sample s2) {
            return s1.getId().compareTo(s2.getId());
        }
    }
}
