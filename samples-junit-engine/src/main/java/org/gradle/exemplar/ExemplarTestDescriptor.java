package org.gradle.exemplar;

import org.gradle.exemplar.model.Command;
import org.gradle.exemplar.model.Sample;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.DirectorySource;

import java.io.File;

public final class ExemplarTestDescriptor extends AbstractTestDescriptor {
    private final File file;
    private final String name;
    private final Sample sample;

    public ExemplarTestDescriptor(UniqueId parentId, File file, String name, Sample sample) {
        super(
            parentId.append("testDefinitionFile", fileNameWithoutExtension(file)).append("testDefinition", name),
            file.getParentFile().getName(),
            DirectorySource.from(sample.getProjectDir())
        );
        this.file = file;
        this.name = name;
        this.sample = sample;
        for (Command command : sample.getCommands()) {
            children.add(new ExemplarTestCommandDescriptor(this, sample, command));
        }
    }

    private static String fileNameWithoutExtension(File file) {
        String name = file.getName();
        int i = name.indexOf(".sample.conf");
        if (i > 0) {
            return name.substring(0, i);
        }
        return name;
    }

    @Override
    public Type getType() {
        return Type.CONTAINER;
    }

    public Sample getSample() {
        return sample;
    }

    @Override
    public String toString() {
        return "Sample[file=" + file.getName() + ", name=" + name + "]";
    }
}
