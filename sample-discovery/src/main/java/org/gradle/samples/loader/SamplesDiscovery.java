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
            samples.addAll(extractFromAsciidoctorFile(sampleConfigFile));
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
        return processAsciidocSampleBlocks(document);
    }

    /**
     * Perform an pre-order traversal of the tree under the given search root, and process testable-sample nodes
     * and their descendant sample-command blocks.
     *
     * Asciidoctor renders content as it parses it, which prevents us from collecting nodes to process separately.
     * If we do not process as we discover samples, they will already be rendered as HTML (default backend)
     * and the contents are no longer parsable.
     *
     * @param rootNode Asciidoctor AST root node
     * @return extracted list of {@link Sample}s
     * @throws IOException if any temporary directory or file could not be created
     */
    private static List<Sample> processAsciidocSampleBlocks(StructuralNode rootNode) throws IOException {
        List<Sample> samples = new ArrayList<>();
        Path tempDir = Files.createTempDirectory("exemplar-testable-samples");

        Queue<StructuralNode> queue = new ArrayDeque<>();
        queue.add(rootNode);
        while (!queue.isEmpty()) {
            StructuralNode node = queue.poll();
            for (StructuralNode child : node.getBlocks()) {
                if (child.isBlock() && child.hasRole("testable-sample")) {
                    List<Command> commands = extractAsciidocCommands((Block) node);
                    // Nothing to verify, skip this sample
                    if (commands.isEmpty()) {
                        // TODO: print a warning here as this is probably a user mistake
                        continue;
                    }
                    samples.add(processSampleNode(child, tempDir, commands));
                } else {
                    queue.offer(child);
                }
            }
        }
        return samples;
    }

    /**
     * "testable-sample"s that declare a "dir" attribute have the sample sources living there.
     *
     * @param node Asciidoctor StructuralNode
     * @param tempDir Path to create any temporary dirs/files
     * @param commands Pre-extracted commands
     * @return new Sample
     */
    private static Sample processSampleNode(StructuralNode node, Path tempDir, List<Command> commands) {
        String sampleId;
        File sampleDir;
        if (node.hasAttribute("dir")) {
            String dir = node.getAttribute("dir").toString();
            sampleId = dir.replaceAll("[/\\\\]", "_");
            sampleDir = new File(dir);
        } else {
            sampleId = RandomStringUtils.randomAlphabetic(7);
            try {
                sampleDir = Files.createDirectory(tempDir.resolve(sampleId)).toFile();
            } catch (IOException e) {
                throw new IllegalStateException("Could not create temp sample directory under " + tempDir.toString());
            }
            extractEmbeddedSampleSources((Block) node, sampleDir);
        }
        return new Sample(sampleId, sampleDir, commands);
    }

    private static List<Command> extractAsciidocCommands(Block testableSampleBlock) {
        List<Command> commands = new ArrayList<>();
        Queue<StructuralNode> queue = new ArrayDeque<>();
        queue.add(testableSampleBlock);
        while (!queue.isEmpty()) {
            StructuralNode node = queue.poll();
            for (StructuralNode child : node.getBlocks()) {
                if (child.isBlock() && child.hasRole("sample-command")) {
                    commands.add(parseEmbeddedCommand((Block) child, "$ "));
                } else {
                    queue.offer(child);
                }
            }
        }

        return commands;
    }

    private static Command parseEmbeddedCommand(Block block, String commandPrefix) {
        String[] commandLineAndOutput = block.getSource().split("\r?\n", 2);

        String commandLine = commandLineAndOutput[0];
        if (!commandLine.startsWith(commandPrefix)) {
            throw new InvalidSampleException("Inline sample command " + commandLine);
        }

        String[] commandLineWords = commandLine.substring(commandPrefix.length()).split("\\s+");
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

    private static void extractEmbeddedSampleSources(Block sampleBlock, File tempSampleDir) {
        for (StructuralNode block : sampleBlock.getBlocks()) {
            if (block.getStyle().equals("source") && block.getTitle() != null) {
                File sampleFile = new File(tempSampleDir, block.getTitle());
                File sampleSubfolder = sampleFile.getParentFile();
                if (!sampleSubfolder.exists() && !sampleSubfolder.mkdirs()) {
                    throw new IllegalStateException("Couldn't create dir: " + sampleSubfolder);
                }
                try {
                    Files.write(sampleFile.toPath(), block.getContent().toString().getBytes());
                } catch (IOException e) {
                    throw new IllegalStateException("Could not write sample source file " + sampleFile.toPath().toString());
                }
            }
        }
    }
}
