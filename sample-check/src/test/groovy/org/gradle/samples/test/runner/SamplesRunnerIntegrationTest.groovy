package org.gradle.samples.test.runner

import org.junit.experimental.categories.Category
import org.junit.runner.Request
import org.junit.runner.RunWith
import spock.lang.Specification

class SamplesRunnerIntegrationTest extends Specification {
    def "runs samples-check CLI samples"() {
        def notifier = new CollectingNotifier()

        when:
        Request.aClass(HappyDaySamples.class).runner.run(notifier)

        then:
        notifier.tests.size() == 2
        notifier.tests[0].methodName == 'multi-step_multi-step.sample'
        notifier.tests[0].className == HappyDaySamples.class.name

        notifier.tests[1].methodName == 'quickstart_quickstart.sample'
        notifier.tests[1].className == HappyDaySamples.class.name

        notifier.failures.empty
    }

    @RunWith(SamplesRunner.class)
    @SamplesRoot("src/test/samples/cli")
    @Category(CoveredByTests)
    static class HappyDaySamples {
    }
}
