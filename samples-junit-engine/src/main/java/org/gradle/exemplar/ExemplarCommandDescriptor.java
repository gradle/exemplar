package org.gradle.exemplar;

import org.gradle.exemplar.model.Command;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.DirectorySource;

public final class ExemplarCommandDescriptor extends AbstractTestDescriptor {

    private final Command command;

    public ExemplarCommandDescriptor(ExemplarSampleDescriptor parent, Command command) {
        super(
            uniqueId(parent, command),
            displayName(command),
            DirectorySource.from(parent.getSample().getProjectDir())
        );
        setParent(parent);
        this.command = command;
    }

    private static UniqueId uniqueId(ExemplarSampleDescriptor exemplarTestDescriptor, Command command) {
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
