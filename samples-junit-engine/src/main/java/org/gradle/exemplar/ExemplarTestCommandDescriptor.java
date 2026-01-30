package org.gradle.exemplar;

import org.gradle.exemplar.model.Command;
import org.gradle.exemplar.model.Sample;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.DirectorySource;

public final class ExemplarTestCommandDescriptor extends AbstractTestDescriptor {

    private final Command command;

    public ExemplarTestCommandDescriptor(ExemplarTestDescriptor exemplarTestDescriptor, Sample sample, Command command) {
        super(
            uniqueId(exemplarTestDescriptor, command),
            displayName(command),
            DirectorySource.from(sample.getProjectDir())
        );
        setParent(exemplarTestDescriptor);
        this.command = command;
    }

    private static UniqueId uniqueId(ExemplarTestDescriptor exemplarTestDescriptor, Command command) {
        return exemplarTestDescriptor.getUniqueId().append("commandName", displayName(command));
    }

    private static String displayName(Command command) {
        StringBuilder displayName = new StringBuilder(command.getExecutable());
        for (String flag : command.getFlags()) {
            displayName.append(" ");
            displayName.append(flag);
        }
        for (String arg : command.getArgs()) {
            displayName.append(" ");
            displayName.append(arg);
        }
        return displayName.toString();
    }

    @Override
    public Type getType() {
        return Type.TEST;
    }

    public Command getCommand() {
        return command;
    }
}
