package test;

import org.gradle.exemplar.model.Sample;
import org.gradle.exemplar.test.runner.SampleModifier;

@SuppressWarnings("unused") // used by system property
public class TestSampleModifier implements SampleModifier {
    @Override
    public Sample modify(Sample sampleIn) {
        return new Sample(sampleIn.getId(), sampleIn.getProjectDir(), sampleIn.getCommands());
    }
}
