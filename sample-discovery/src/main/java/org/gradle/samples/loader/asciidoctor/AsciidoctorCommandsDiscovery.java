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

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.ListImpl;
import org.gradle.samples.InvalidSampleException;
import org.gradle.samples.model.Command;

import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.function.Consumer;

import static org.asciidoctor.OptionsBuilder.options;

public class AsciidoctorCommandsDiscovery {

    private static final String COMMAND_PREFIX = "$ ";

    public static List<Command> extractFromAsciidoctorFile(File documentFile) throws IOException {
        return extractFromAsciidoctorFile(documentFile, it -> {});
    }

    public static List<Command> extractFromAsciidoctorFile(File documentFile, Consumer<OptionsBuilder> action) throws IOException {
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();

        try {
            OptionsBuilder options = options();
            action.accept(options);

            Document document = asciidoctor.loadFile(documentFile, options.asMap());
            return extractAsciidocCommands(document);
        } finally {
            asciidoctor.shutdown();
        }
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
                attributes.containsKey("user-inputs") ? toUserInputs(attributes.get("user-inputs").toString()) : Collections.emptyList());
        commands.add(command);
        return nextCommand;
    }

    private static List<String> toUserInputs(String value) {
        return Arrays.asList(value.split("\\|", -1));
    }
}
