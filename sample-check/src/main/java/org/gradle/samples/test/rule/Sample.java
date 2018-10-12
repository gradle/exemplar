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

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 * A JUnit rule which copies a sample into the test directory before the test executes.
 *
 * <p>Looks for a {@link UsesSample} annotation on the test method to determine which sample the
 * test requires. If not found, uses the default sample provided in the constructor.
 */
public class Sample implements TestRule {

    private final SourceSampleDirSupplier sourceSampleDirSupplier;
    private TargetBaseDirSupplier targetBaseDirSupplier;
    private String defaultSampleName;

    private String sampleName;
    private File targetDir;

    public static Sample from(final String sourceBaseDirPath) {
        return from(new SourceSampleDirSupplier() {
            @Override
            public File getDir(String sampleName) {
                return new File(sourceBaseDirPath, sampleName);
            }
        });
    }

    public static Sample from(SourceSampleDirSupplier sourceSampleDirSupplier) {
        return new Sample(sourceSampleDirSupplier);
    }

    private Sample(SourceSampleDirSupplier sourceSampleDirSupplier) {
        this.sourceSampleDirSupplier = sourceSampleDirSupplier;
    }

    /**
     * Copy the samples into the supplied {@link TemporaryFolder}.
     *
     * @deprecated please use {@link #intoTemporaryFolder()} or {@link #intoTemporaryFolder(File)}
     */
    @Deprecated
    public Sample into(final TemporaryFolder temporaryFolder) {
        return into(new TargetBaseDirSupplier() {
            @Override
            public File getDir() {
                try {
                    return temporaryFolder.newFolder("samples");
                } catch (IOException e) {
                    throw new RuntimeException("Could not create samples target base dir", e);
                }
            }
        });
    }

    /**
     * Copy the samples into a temporary folder that is attempted to be deleted afterwards.
     */
    public Sample intoTemporaryFolder() {
        return intoTemporaryFolder(null);
    }

    /**
     * Copy the samples into a temporary folder that is attempted to be deleted afterwards.
     *
     * @param parentFolder The parent folder of the created temporary folder
     */
    public Sample intoTemporaryFolder(File parentFolder) {
        return into(new ManagedTemporaryFolder(parentFolder));
    }

    /**
     * Copy the samples into a folder returned by the supplied {@link TargetBaseDirSupplier}.
     *
     * @see TargetBaseDirSupplier
     */
    public Sample into(TargetBaseDirSupplier targetBaseDirSupplier) {
        this.targetBaseDirSupplier = targetBaseDirSupplier;
        return this;
    }

    public Sample withDefaultSample(String name) {
        this.defaultSampleName = name;
        return this;
    }

    public interface SourceSampleDirSupplier {
        File getDir(String sampleName);
    }

    /**
     * Supplier for the base directory into which samples are copied.
     *
     * May optionally implement {@link Closeable} in which case it will be called after test execution to clean up.
     */
    public interface TargetBaseDirSupplier {
        File getDir();
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        if (targetBaseDirSupplier == null) {
            intoTemporaryFolder();
        }
        sampleName = getSampleName(description);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                assertNotNull("No sample selected. Please use @UsesSample or withDefaultSample()", sampleName);
                try {
                    File srcDir = sourceSampleDirSupplier.getDir(sampleName);
                    FileUtils.copyDirectory(srcDir, getDir());
                    base.evaluate();
                } finally {
                    if (targetBaseDirSupplier instanceof Closeable) {
                        ((Closeable) targetBaseDirSupplier).close();
                    }
                }
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

    private static class ManagedTemporaryFolder implements TargetBaseDirSupplier, Closeable {
        private final TemporaryFolder temporaryFolder;

        public ManagedTemporaryFolder(File parentFolder) {
            this.temporaryFolder = new TemporaryFolder(parentFolder);
        }

        @Override
        public File getDir() {
            try {
                temporaryFolder.create();
                return temporaryFolder.getRoot();
            } catch (IOException e) {
                throw new RuntimeException("Could not create samples target base dir", e);
            }
        }

        @Override
        public void close() {
            temporaryFolder.delete();
        }
    }
}
