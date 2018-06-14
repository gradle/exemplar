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
import org.apache.commons.lang3.RandomStringUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.gradle.samples.InvalidSampleException;
import org.gradle.samples.model.Command;
import org.gradle.samples.model.Sample;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SamplesDiscovery {
    public static List<Sample> independentSamples(File rootSamplesDir) {
        // The .sample.conf suffix makes it clear that this is a HOCON file specifically for samples
        return filteredIndependentSamples(rootSamplesDir, new String[]{"sample.conf"}, true);
    }

    public static List<Sample> filteredIndependentSamples(File rootSamplesDir, String[] fileExtensions, boolean recursive) {
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

    public static List<Sample> embeddedSamples(File asciidocSrcDir) throws IOException {
        return filteredEmbeddedSamples(asciidocSrcDir, new String[]{"adoc", "asciidoc"}, true);
    }

    public static List<Sample> filteredEmbeddedSamples(File rootSamplesDir, String[] fileExtensions, boolean recursive) throws IOException {
        Collection<File> sampleConfigFiles = FileUtils.listFiles(rootSamplesDir, fileExtensions, recursive);

        List<Sample> samples = new ArrayList<>();
        for (File sampleConfigFile : sampleConfigFiles) {
            extractFromAsciidoctorFile(sampleConfigFile);
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

    public static List<Sample> extractFromAsciidoctorFile(File documentFile) throws IOException {
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        Document document = asciidoctor.loadFile(documentFile, new HashMap<String, Object>());
        Path tempDirectory = Files.createTempDirectory("exemplar-testable-samples");
        return recurseAsciidocTree(document.getBlocks(), new ArrayList<Sample>(), tempDirectory);
    }

    private static List<Sample> recurseAsciidocTree(List<StructuralNode> nodes, List<Sample> samples, Path tempDir) throws IOException {
        for (StructuralNode node : nodes) {
            if (node.hasRole("testable-sample") && node.isBlock()) {
                String sampleId = RandomStringUtils.randomAlphabetic(7);
                File tempSampleDir = Files.createDirectory(tempDir.resolve(sampleId)).toFile();
                List<Command> commands = processEmbeddedSample((Block) node, tempSampleDir);
                // Nothing to verify, skip this sample
                if (commands.isEmpty()) {
                    continue;
                }

                samples.add(new Sample(sampleId, tempSampleDir, commands));
            }

            recurseAsciidocTree(node.getBlocks(), samples, tempDir);
        }

        return samples;
    }

    private static List<Command> processEmbeddedSample(Block inlineSampleBlock, File tempSampleDir) throws IOException {
        List<Command> commands = new ArrayList<>();

        List<StructuralNode> children = inlineSampleBlock.getBlocks();
        for (StructuralNode child : children) {
            if (child.hasRole("sample-command") && child.isBlock()) {
                commands.add(parseEmbeddedCommand((Block) child));
            } else if (child.getStyle().equals("source") && child.getTitle() != null) {
                File sampleFile = new File(tempSampleDir, child.getTitle());
                File sampleSubfolder = sampleFile.getParentFile();
                if (!sampleSubfolder.exists() && !sampleSubfolder.mkdirs()) {
                    throw new IllegalStateException("Couldn't create dir: " + sampleSubfolder);
                }
                Files.write(sampleFile.toPath(), child.getContent().toString().getBytes());
            }
        }

        return commands;
    }

    protected static Command parseEmbeddedCommand(Block block) {
        String[] commandLineAndOutput = block.getSource().split("\r?\n", 2);

        String commandLine = commandLineAndOutput[0];
        if (!commandLine.startsWith("$ ")) {
            throw new InvalidSampleException("Could not parse inline sample command " + commandLine);
        }

        String[] commandLineWords = commandLine.substring(2).split("\\s+");
        String executable = commandLineWords[0];

        List<String> args = new ArrayList<>();
        if (commandLineWords.length > 1) {
            args = Arrays.asList(Arrays.copyOfRange(commandLineWords, 1, commandLineWords.length));
        }

        String expectedOutput = null;
        if (commandLineAndOutput.length > 1) {
            expectedOutput = commandLineAndOutput[1];
        }

        Map<String, Object> attributes = block.getAttributes();
        return new Command(executable,
                null,
                args,
                new ArrayList<String>(),
                expectedOutput,
                attributes.containsKey("expect-failure"),
                attributes.containsKey("allow-additional-output"),
                attributes.containsKey("allow-disordered-output"));
    }
}
