package org.gradle.samples.executor

import org.gradle.samples.model.Command
import org.gradle.samples.test.customizer.CommandCustomizer
import spock.lang.Specification
import spock.lang.Subject

class CustomizationCommandExecutorTest extends Specification {
    CommandCustomizer customizer1 = Mock(CommandCustomizer)
    CommandCustomizer customizer2 = Mock(CommandCustomizer)
    CommandExecutor delegate = Mock(CommandExecutor)
    Command command = Mock(Command)
    Command command1 = Mock(Command)
    Command command2 = Mock(Command)
    ExecutionMetadata metadata = Mock(ExecutionMetadata)

    @Subject
    CustomizationCommandExecutor executor = new CustomizationCommandExecutor(delegate, [customizer1, customizer2])

    def 'can run customizers before delegating it'() {
        when:
        executor.execute(command, metadata)

        then:
        1 * customizer1.customize(command) >> command1

        and:
        1 * customizer2.customize(command1) >> command2

        and:
        1 * delegate.execute(command2, metadata)
    }
}
