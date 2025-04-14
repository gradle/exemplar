package org.gradle.exemplar.test.runner.modifiers;

import org.gradle.exemplar.model.Command;
import org.gradle.exemplar.model.Sample;
import org.gradle.exemplar.test.SampleModifier;

import java.util.ArrayList;
import java.util.List;

public class ExtraCommandArgumentsSampleModifier implements SampleModifier {
    @Override
    public Sample modify(Sample sampleIn) {
        List<Command> newCommands = new ArrayList<>();
        for (Command command : sampleIn.getCommands()) {
            List<String> args = new ArrayList<>(command.getArgs());
            args.add("printProperty");
            args.add("-DmyProp=myValue");
            newCommands.add(command.toBuilder().setArgs(args).build());
        }
        return new Sample(sampleIn.getId(), sampleIn.getProjectDir(), newCommands);
    }
}
