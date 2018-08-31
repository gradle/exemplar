package org.gradle.samples.test.customizer;

import org.gradle.samples.model.Command;

import java.util.ArrayList;
import java.util.List;

public class ExtraSystemPropertyCustomizer implements CommandCustomizer {
    @Override
    public Command customize(Command command) {
        List<String> args = new ArrayList<>(command.getArgs());
        args.add("-DmyProp=myValue");
        return CommandBuilder.fromCommand(command).setArgs(args).build();
    }
}
