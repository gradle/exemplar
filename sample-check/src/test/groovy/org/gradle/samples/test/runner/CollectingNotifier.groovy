package org.gradle.samples.test.runner

import org.junit.runner.Description
import org.junit.runner.notification.Failure
import org.junit.runner.notification.RunNotifier
import org.junit.runner.notification.StoppedByUserException


class CollectingNotifier extends RunNotifier {
    final List<Description> tests = []
    final List<Failure> failures = []

    @Override
    void fireTestStarted(Description description) throws StoppedByUserException {
        tests.add(description)
    }

    @Override
    void fireTestFailure(Failure failure) {
        failures.add(failure)
    }
}
