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
package org.gradle.exemplar;

import org.gradle.api.Project;
import org.gradle.api.file.DirectoryProperty;

public class ExemplarPluginExtension {
    private final DirectoryProperty inputDir;
    private final DirectoryProperty outputDir;
//    private final Property<String> minVersion;
//    private final Property<String> maxVersion;

    public ExemplarPluginExtension(Project project) {
        inputDir = project.getLayout().directoryProperty();
        outputDir = project.getLayout().directoryProperty();
//        minVersion = project.getObjects().property(String.class);
//        maxVersion = project.getObjects().property(String.class);
    }

    public DirectoryProperty getInputDir() {
        return inputDir;
    }

    public DirectoryProperty getOutputDir() {
        return outputDir;
    }

//    public Property<String> getMinVersion() {
//        return minVersion;
//    }
//
//    public Property<String> getMaxVersion() {
//        return maxVersion;
//    }
}
