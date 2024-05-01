package org.gradle.exemplar.test;

import java.io.File;
import java.util.function.Supplier;

public class NoopRootDirSupplier implements Supplier<File> {
    @Override
    public File get() {
        return null;
    }
}
