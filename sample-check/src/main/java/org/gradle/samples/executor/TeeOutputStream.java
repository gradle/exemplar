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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class TeeOutputStream extends OutputStream {
    private final List<OutputStream> targets;

    public TeeOutputStream(OutputStream... targets) {
        this.targets = Arrays.asList(targets);
    }

    @Override
    public void write(int b) throws IOException {
        withAll(stream -> stream.write(b));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        withAll(stream -> stream.write(b, off, len));
    }

    @Override
    public void flush() throws IOException {
        withAll(stream -> stream.flush());
    }

    @Override
    public void close() throws IOException {
        withAll(stream -> stream.close());
    }

    private void withAll(IOAction action) throws IOException {
        IOException failure = null;
        for (OutputStream target : targets) {
            try {
                action.withStream(target);
            } catch (IOException e) {
                failure = e;
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    private interface IOAction {
        void withStream(OutputStream stream) throws IOException;
    }
}
