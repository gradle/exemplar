package org.gradle.exemplar;

import org.gradle.exemplar.loader.SamplesDiscovery;
import org.gradle.exemplar.model.Sample;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.discovery.DirectorySelector;
import org.junit.platform.engine.support.discovery.SelectorResolver;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ExemplarTestResolver implements SelectorResolver {
    public static final Logger LOGGER = LoggerFactory.getLogger(ExemplarTestResolver.class);

    @Override
    public Resolution resolve(DirectorySelector selector, Context context) {
        LOGGER.info(() -> "Test specification dir: " + selector.getDirectory().getAbsolutePath());
        List<Sample> samples = SamplesDiscovery.externalSamples(selector.getDirectory());
        Set<Match> tests = samples.stream()
            .map(s -> context.addToParent(parent -> Optional.of(new ExemplarSampleDescriptor(parent.getUniqueId(), s))))
            .map(Optional::get)
            .map(Match::exact)
            .collect(Collectors.toSet());

        if (!tests.isEmpty()) {
            return Resolution.matches(tests);
        }
        return Resolution.unresolved();
    }
}
