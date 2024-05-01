package org.gradle.exemplar.test.runner

import org.gradle.exemplar.test.Samples
import org.junit.experimental.categories.Category
import org.junit.runner.Request
import spock.lang.Ignore
import spock.lang.Specification

@Ignore("Find a way to test the JUnit5 engine instead of JUnit4 runner")
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

    @Samples(root = "src/test/samples/cli")
    @Category(CoveredByTests)
    static class HappyDaySamples {}

    def "can use multi-steps with working directory inside sample"() {
        def notifier = new CollectingNotifier()

        when:
        Request.aClass(HappyDayWithWorkingDirectorySamples.class).runner.run(notifier)

        then:
        notifier.tests.size() == 1
        notifier.tests[0].methodName == 'multi-step_multi-step.sample'
        notifier.tests[0].className == HappyDayWithWorkingDirectorySamples.class.name

        notifier.failures.empty
    }

    @Samples(root = "src/test/samples/cli-with-working-directory")
    @Category(CoveredByTests)
    static class HappyDayWithWorkingDirectorySamples {}

    def "warn when using working directory after change directory command instruction"() {
        def notifier = new CollectingNotifier()

        when:
        Request.aClass(HappyDayWithWorkingDirectoryAndChangeDirectoryCommandSamples.class).runner.run(notifier)

        then:
        notifier.tests.size() == 1
        notifier.tests[0].methodName == 'multi-step_multi-step.sample'
        notifier.tests[0].className == HappyDayWithWorkingDirectoryAndChangeDirectoryCommandSamples.class.name

        notifier.failures.empty
    }

    @Samples(root = "src/test/samples/cli-with-working-directory-and-change-directory")
    @Category(CoveredByTests)
    static class HappyDayWithWorkingDirectoryAndChangeDirectoryCommandSamples {}
}
