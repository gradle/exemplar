package org.gradle.samples.test.runner

import org.junit.Rule
import org.junit.experimental.categories.Category
import org.junit.rules.TemporaryFolder
import org.junit.runner.Request
import org.junit.runner.RunWith
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import spock.lang.Specification

class SamplesRunnerSadDayIntegrationTest extends Specification {
    @Rule
    TemporaryFolder tmpDir = new TemporaryFolder()

    def "tests fail when command fails"() {
        def notifier = new CollectingNotifier()

        when:
        Request.aClass(HasBadCommand.class).runner.run(notifier)

        then:
        notifier.failures.size() == 1
        notifier.failures[0].description.methodName == '_broken-command.sample'
        notifier.failures[0].description.className == HasBadCommand.class.name
        notifier.failures[0].message.trim() == """
            Expected sample invocation to succeed but it failed.
            Command was: 'bash broken'
            [BEGIN OUTPUT]
            bash: broken: No such file or directory
            
            [END OUTPUT]
        """.stripIndent().trim()
    }

    def "tests fail when command produces unexpected output"() {
        def notifier = new CollectingNotifier()

        when:
        Request.aClass(HasBadOutput.class).runner.run(notifier)

        then:
        notifier.failures.size() == 1
        notifier.failures[0].description.methodName == '_broken-output.sample'
        notifier.failures[0].description.className == HasBadOutput.class.name
        notifier.failures[0].message.trim() == """
            Missing text at line 1.
            Expected: not a thing
            Actual: thing
            Actual output:
            thing
        """.stripIndent().trim()
    }

    @SamplesRoot("src/test/resources/broken/command")
    @RunWith(SamplesRunner)
    @Category(CoveredByTests)
    static class HasBadCommand {}

    @SamplesRoot("src/test/resources/broken/output")
    @RunWith(SamplesRunner)
    @Category(CoveredByTests)
    static class HasBadOutput {}

    static class CollectingNotifier extends RunNotifier {
        final List<Failure> failures = []

        @Override
        void fireTestFailure(Failure failure) {
            failures.add(failure)
        }
    }
}
