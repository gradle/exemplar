package org.gradle.samples.executor

import org.gradle.samples.model.Command
import org.gradle.samples.test.runner.CommandModifier
import spock.lang.Specification
import spock.lang.Subject

class ModifyingCommandExecutorTest extends Specification {
    CommandModifier commandModifier1 = Mock(CommandModifier)
    CommandModifier commandModifier2 = Mock(CommandModifier)
    CommandExecutor downstreamCommandExecutor = Mock(CommandExecutor)
    Command command = Mock(Command)
    Command command1 = Mock(Command)
    Command command2 = Mock(Command)
    ExecutionMetadata metadata = Mock(ExecutionMetadata)

    @Subject
    ModifyingCommandExecutor modifyingCommandExecutor = new ModifyingCommandExecutor(downstreamCommandExecutor, [commandModifier1, commandModifier2])

    def "transforms commands before executing them"() {
        when:
        modifyingCommandExecutor.execute(command, metadata)

        then:
        1 * commandModifier1.update(command) >> command1

        and:
        1 * commandModifier2.update(command1) >> command2

        and:
        1 * downstreamCommandExecutor.execute(command2, metadata)
    }
}
