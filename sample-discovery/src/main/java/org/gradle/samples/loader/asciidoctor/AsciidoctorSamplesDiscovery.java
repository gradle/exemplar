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
package org.gradle.samples.loader.asciidoctor;

import org.apache.commons.lang3.RandomStringUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListImpl;
import org.gradle.samples.InvalidSampleException;
import org.gradle.samples.model.Command;
import org.gradle.samples.model.Sample;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

import static org.asciidoctor.OptionsBuilder.options;

public class AsciidoctorSamplesDiscovery {

    private static final String COMMAND_PREFIX = "$ ";

    public static List<Sample> extractFromAsciidoctorFile(File documentFile) throws IOException {
        return extractFromAsciidoctorFile(documentFile, it -> {});
    }

    public static List<Sample> extractFromAsciidoctorFile(File documentFile, Consumer<OptionsBuilder> action) throws IOException {
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();

        try {
            OptionsBuilder options = options();
            action.accept(options);

            Document document = asciidoctor.loadFile(documentFile, options.asMap());
            return processAsciidocSampleBlocks(document);
        } finally {
            asciidoctor.shutdown();
        }
    }

    /**
     * Perform an pre-order traversal of the tree under the given search root, and process testable-sample nodes
     * and their descendant sample-command blocks.
     * <p>
     * Asciidoctor renders content as it parses it, which prevents us from collecting nodes to process separately.
     * If we do not process as we discover samples, they will already be rendered as HTML (default backend)
     * and the contents are no longer parsable.
     *
     * @param rootNode Asciidoctor AST root node
     * @return extracted list of {@link Sample}s
     * @throws IOException if any temporary directory or file could not be created
     */
    private static List<Sample> processAsciidocSampleBlocks(AbstractBlock rootNode) throws IOException {
        List<Sample> samples = new ArrayList<>();
        Path tempDir = Files.createTempDirectory("exemplar-testable-samples");

        Queue<AbstractBlock> queue = new ArrayDeque<>();
        queue.add(rootNode);
        while (!queue.isEmpty()) {
            AbstractBlock node = queue.poll();

            List<AbstractBlock> blocks = node.getBlocks();
            // Some asciidoctor AST types return null instead of an empty list
            if (blocks == null) {
                continue;
            }

            for (AbstractBlock child : blocks) {
                if (child.isBlock() && child.hasRole("testable-sample")) {
                    List<Command> commands = extractAsciidocCommands(node);
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
     * @param node     Asciidoctor StructuralNode
     * @param tempDir  Path to create any temporary dirs/files
     * @param commands Pre-extracted commands
     * @return new Sample
     */
    private static Sample processSampleNode(AbstractBlock node, Path tempDir, List<Command> commands) {
        String sampleId;
        File sampleDir;
        if (node.getAttr("dir") != null) {
            String dir = node.getAttr("dir").toString();
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

    private static List<Command> extractAsciidocCommands(AbstractBlock testableSampleBlock) {
        List<Command> commands = new ArrayList<>();
        Queue<AbstractBlock> queue = new ArrayDeque<>();
        queue.add(testableSampleBlock);
        while (!queue.isEmpty()) {
            AbstractBlock node = queue.poll();
            if (node instanceof ListImpl) {
                queue.addAll(((ListImpl) node).getItems());
            } else {
                for (AbstractBlock child : node.getBlocks()) {
                    if (child.isBlock() && child.hasRole("sample-command")) {
                        parseEmbeddedCommand((Block) child, commands);
                    } else {
                        queue.offer(child);
                    }
                }
            }
        }

        return commands;
    }

    private static void parseEmbeddedCommand(Block block, List<Command> commands) {
        Map<String, Object> attributes = block.getAttributes();
        String[] lines = block.source().split("\r?\n");
        int pos = 0;

        do {
            pos = parseOneCommand(lines, pos, attributes, commands);
        } while (pos < lines.length);
    }

    private static int parseOneCommand(String[] lines, int pos, Map<String, Object> attributes, List<Command> commands) {
        String commandLine = lines[pos];
        if (!commandLine.startsWith(COMMAND_PREFIX)) {
            throw new InvalidSampleException("Inline sample command " + commandLine);
        }

        String[] commandLineWords = commandLine.substring(COMMAND_PREFIX.length()).split("\\s+");
        String executable = commandLineWords[0];

        List<String> args = Collections.emptyList();
        if (commandLineWords.length > 1) {
            args = Arrays.asList(Arrays.copyOfRange(commandLineWords, 1, commandLineWords.length));
        }

        StringBuilder expectedOutput = new StringBuilder();
        int nextCommand = pos + 1;
        while (nextCommand < lines.length && !lines[nextCommand].startsWith(COMMAND_PREFIX)) {
            if (nextCommand > pos + 1) {
                expectedOutput.append("\n");
            }
            expectedOutput.append(lines[nextCommand]);
            nextCommand++;
        }

        Command command = new Command(executable,
            null,
            args,
            Collections.<String>emptyList(),
            expectedOutput.toString(),
            attributes.containsKey("expect-failure"),
            attributes.containsKey("allow-additional-output"),
            attributes.containsKey("allow-disordered-output"),
            Collections.emptyList());
        commands.add(command);
        return nextCommand;
    }

    private static void extractEmbeddedSampleSources(Block sampleBlock, File tempSampleDir) {
        for (AbstractBlock block : sampleBlock.getBlocks()) {
            if (block.getStyle() != null && block.getStyle().equals("source") && block.getTitle() != null) {
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
