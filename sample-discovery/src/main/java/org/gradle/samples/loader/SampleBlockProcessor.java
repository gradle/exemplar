/*
 * Copyright 2018 the original author or authors.
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

import com.beust.jcommander.internal.Lists;
import org.asciidoctor.ast.AbstractBlock;
import org.asciidoctor.extension.BlockProcessor;
import org.asciidoctor.extension.Reader;
import org.gradle.samples.model.Sample;

import java.util.List;
import java.util.Map;

public class SampleBlockProcessor extends BlockProcessor {
    private final List<Sample> samples = Lists.newArrayList();

    public SampleBlockProcessor() {
        super("SampleBlockProcessor");
    }

    @Override
    public Object process(AbstractBlock parent, Reader reader, Map<String, Object> attributes) {
        System.out.println(reader.readLines());
        return null;
    }

    public List<Sample> getSamples() {
        return samples;
    }
}
