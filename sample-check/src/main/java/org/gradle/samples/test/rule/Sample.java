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

package org.gradle.samples.test.rule;

import org.apache.commons.io.FileUtils;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;

/**
 * A JUnit rule which copies a sample into the test directory before the test executes.
 *
 * <p>Looks for a {@link UsesSample} annotation on the test method to determine which sample the
 * test requires. If not found, uses the default sample provided in the constructor.
 */
public class Sample implements TestRule {

    private final SourceSampleDirSupplier sourceSampleDirSupplier;
    private final TargetBaseDirSupplier targetBaseDirSupplier;
    private final String defaultSampleName;

    private String sampleName;
    private File targetDir;

    public Sample(String sourceBaseDirPath, TemporaryFolder temporaryFolder) {
        this(sourceBaseDirPath, temporaryFolder, null);
    }

    public Sample(final String sourceBaseDirPath, final TemporaryFolder temporaryFolder, @Nullable String defaultSampleName) {
        this(eagerSourceSampleDirSupplier(sourceBaseDirPath),
                new TargetBaseDirSupplier() {
                    @Override
                    public File getDir() {
                        try {
                            return temporaryFolder.newFolder("samples");
                        } catch (IOException e) {
                            throw new RuntimeException("Could not create samples target base dir", e);
                        }
                    }
                },
                defaultSampleName);
    }

    private static SourceSampleDirSupplier eagerSourceSampleDirSupplier(final String sourceBaseDirPath) {
        return new SourceSampleDirSupplier() {
                 @Override
                 public File getDir(String sampleName) {
                     return new File(sourceBaseDirPath, sampleName);
                 }
             };
    }

    public Sample(SourceSampleDirSupplier sourceSampleDirSupplier, TargetBaseDirSupplier targetBaseDirSupplier) {
        this(sourceSampleDirSupplier, targetBaseDirSupplier, null);
    }

    public Sample(SourceSampleDirSupplier sourceSampleDirSupplier, TargetBaseDirSupplier targetBaseDirSupplier, @Nullable String defaultSampleName) {
        this.sourceSampleDirSupplier = sourceSampleDirSupplier;
        this.targetBaseDirSupplier = targetBaseDirSupplier;
        this.defaultSampleName = defaultSampleName;
    }

    public interface SourceSampleDirSupplier {
        File getDir(String sampleName);
    }

    public interface TargetBaseDirSupplier {
        File getDir();
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        sampleName = getSampleName(description);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                File srcDir = sourceSampleDirSupplier.getDir(sampleName);
                FileUtils.copyDirectory(srcDir, getDir());
                base.evaluate();
            }
        };
    }

    private String getSampleName(Description description) {
        UsesSample annotation = description.getAnnotation(UsesSample.class);
        return annotation != null
            ? annotation.value()
            : defaultSampleName;
    }

    public File getDir() {
        if (targetDir == null) {
            targetDir = computeSampleDir();
        }
        return targetDir;
    }

    private File computeSampleDir() {
        String subDirName = getSampleTargetDirName();
        return new File(targetBaseDirSupplier.getDir(), subDirName);
    }

    private String getSampleTargetDirName() {
        if (sampleName == null) {
            throw new IllegalStateException("This rule hasn't been applied, yet.");
        }
        return sampleName;
    }
}
