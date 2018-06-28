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
package org.gradle.samples.executor;

import org.apache.commons.lang3.StringUtils;
import org.gradle.samples.model.Command;

import javax.annotation.Nullable;
import java.io.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public abstract class CommandExecutor {
    private final File directory;

    public CommandExecutor() {
        this.directory = null;
    }

    protected CommandExecutor(final File directory) {
        this.directory = directory;
    }

    protected abstract int run(final String executable, final List<String> args, final List<String> flags, final OutputStream output);

    public void run(ProcessBuilder processBuilder, final OutputStream outputStream) {
        run(processBuilder, outputStream, null, null).waitForSuccess();
    }

    protected CommandExecutor.RunHandle run(final ProcessBuilder processBuilder, final OutputStream outputStream, @Nullable final OutputStream errorStream, @Nullable final InputStream inputStream) {
        if (directory != null) {
            processBuilder.directory(directory);
        }
        final String command = processBuilder.command().get(0);
        try {
            if (errorStream == null) {
                processBuilder.redirectErrorStream(true);
            }
            final Process process = processBuilder.start();
            ExecutorService executor = Executors.newFixedThreadPool(3);
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    byte[] buffer = new byte[4096];
                    while (true) {
                        if (readStream(process.getInputStream(), outputStream, command, buffer)) break;
                    }
                }
            });

            if (errorStream != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        byte[] buffer = new byte[4096];
                        while (true) {
                            if (readStream(process.getErrorStream(), errorStream, command, buffer)) break;
                        }
                    }
                });
            }
            if (inputStream != null) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        byte[] buffer = new byte[4096];
                        OutputStream output = process.getOutputStream();
                        while (true) {
                            try {
                                int read = inputStream.read(buffer);
                                output.write(buffer);
                                if (read == -1) {
                                    output.flush();
                                    output.close();
                                    break;
                                }
                            } catch (IOException e) {
                                throw new RuntimeException("Could not write input", e);
                            }
                        }
                    }
                });
            }
            return new CommandExecutor.RunHandle(processBuilder, process, executor);
        } catch (IOException e) {
            throw new RuntimeException(commandErrorMessage(processBuilder), e);
        }
    }

    private boolean readStream(InputStream inputStream, OutputStream outputStream, String command, byte[] buffer) {
        int nread;
        try {
            nread = inputStream.read(buffer);
        } catch (IOException e) {
            throw new RuntimeException("Could not read input from child process for command '" + command + "'", e);
        }
        if (nread < 0) {
            return true;
        }
        try {
            outputStream.write(buffer, 0, nread);
        } catch (IOException e) {
            throw new RuntimeException("Could not write output from child process for command '" + command + "'", e);
        }
        return false;
    }

    private String commandErrorMessage(ProcessBuilder processBuilder) {
        return "Could not run command " + StringUtils.join(processBuilder.command(), " ");
    }

    public class RunHandle {
        private final ProcessBuilder processBuilder;
        private final Process process;

        private final ExecutorService executor;

        RunHandle(ProcessBuilder processBuilder, Process process, ExecutorService executor) {
            this.processBuilder = processBuilder;
            this.process = process;
            this.executor = executor;
        }

        public void waitForSuccess() {
            int result;
            try {
                result = process.waitFor();
            } catch (Exception e) {
                throw new RuntimeException(commandErrorMessage(processBuilder), e);
            } finally {
                shutdownExecutor();
            }
            if (result != 0) {
                throw new RuntimeException(commandErrorMessage(processBuilder) + ". Exited with result " + result);
            }
        }

        private void shutdownExecutor() {
            try {
                executor.shutdown();
                executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public CommandExecutionResult execute(final Command command, final ExecutionMetadata executionMetadata) {
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final int exitCode = run(command.getExecutable(), command.getArgs(), command.getFlags(), outputStream);

        return new CommandExecutionResult(command, exitCode, outputStream.toString(), executionMetadata);
    }
}
