package org.gradle.exemplar.test;

import org.gradle.exemplar.model.Sample;

public class NoopSampleModifier implements SampleModifier {
    @Override
    public Sample modify(Sample sampleIn) {
        return sampleIn;
    }
}
