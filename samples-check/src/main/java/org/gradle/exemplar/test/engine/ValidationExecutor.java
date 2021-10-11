package org.gradle.exemplar.test.engine;

import org.gradle.exemplar.model.Sample;

public interface ValidationExecutor {

    void executeValidation(Sample testSpecificSample);
}
