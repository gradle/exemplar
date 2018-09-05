package org.gradle.samples.test.runner.modifiers;

import org.gradle.samples.model.Command;
import org.gradle.samples.test.runner.CommandModifier;

import java.util.ArrayList;
import java.util.List;

public class ExtraTaskRequestModifier implements CommandModifier {
    @Override
    public Command update(Command command) {
        List<String> args = new ArrayList<>(command.getArgs());
        args.add("printProperty");
        return command.toBuilder().setArgs(args).build();
    }
}
