package org.gradle.exemplar;

import org.gradle.exemplar.model.Command;
import org.gradle.exemplar.model.Sample;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.DirectorySource;

import java.io.File;

public final class ExemplarSampleDescriptor extends AbstractTestDescriptor {
    private final Sample sample;

    public ExemplarSampleDescriptor(UniqueId parentId, Sample sample) {
        super(
            parentId.append("testDefinitionFile", fileNameWithoutExtension(sample.getConfigFile()))
                    .append("testDefinition", sample.getId()),
            sample.getConfigFile().getParentFile().getName(),
            DirectorySource.from(sample.getProjectDir()));
        this.sample = sample;
        defineTests(sample);
    }

    private void defineTests(Sample sample) {
        for (Command command : sample.getCommands()) {
            children.add(new ExemplarCommandDescriptor(this, command));
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
        return "Sample[file=" + sample.getConfigFile().getName() + ", name=" + sample.getId() + "]";
    }
}
