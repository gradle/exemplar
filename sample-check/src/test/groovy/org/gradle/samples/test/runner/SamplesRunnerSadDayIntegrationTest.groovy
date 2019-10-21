package org.gradle.samples.test.runner

import org.junit.Rule
import org.junit.experimental.categories.Category
import org.junit.rules.TemporaryFolder
import org.junit.runner.Request
import org.junit.runner.RunWith
import spock.lang.Specification

class SamplesRunnerSadDayIntegrationTest extends Specification {
    @Rule
    TemporaryFolder tmpDir = new TemporaryFolder()

    def "tests fail when command fails"() {
        def notifier = new CollectingNotifier()

        when:
        Request.aClass(HasBadCommand.class).runner.run(notifier)

        then:
        notifier.tests.size() == 1
        notifier.tests[0].methodName == '_broken-command.sample'
        notifier.tests[0].className == HasBadCommand.class.name

        notifier.failures.size() == 1
        notifier.failures[0].description == notifier.tests[0]

        def expectedOutput = """
            Expected sample invocation to succeed but it failed.
            Command was: 'bash broken'
            Working directory: '.+/_broken-command.sample'
            \\[BEGIN OUTPUT\\]
            bash: broken: No such file or directory
            
            \\[END OUTPUT\\]
        """.stripIndent().trim()
        notifier.failures[0].message.trim() ==~ /${expectedOutput}/
    }

    def "tests fail when command produces unexpected output"() {
        def notifier = new CollectingNotifier()

        when:
        Request.aClass(HasBadOutput.class).runner.run(notifier)

        then:
        notifier.tests.size() == 1
        notifier.tests[0].methodName == '_broken-output.sample'
        notifier.tests[0].className == HasBadOutput.class.name

        notifier.failures.size() == 1
        notifier.failures[0].description == notifier.tests[0]
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
}
