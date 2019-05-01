package org.gradle.samples.test.rule

import org.junit.ClassRule
import org.junit.Rule
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.rules.RuleChain
import org.junit.rules.TemporaryFolder
import org.junit.rules.TestRule
import org.junit.runner.RunWith

@RunWith(Enclosed.class)
class SampleTest {

    static class WithTemporaryFolderRule extends SampleTestCases {

        public TemporaryFolder temporaryFolder = new TemporaryFolder()
        Sample sample = Sample.from("src/test/samples/gradle")
            .into(temporaryFolder)
            .withDefaultSample("basic-sample")

        @Rule
        public TestRule ruleChain = RuleChain.outerRule(temporaryFolder).around(sample)
    }

    static class WithImplicitTemporaryFolder extends SampleTestCases {
        @Rule
        public Sample sample = Sample.from("src/test/samples/gradle")
            .withDefaultSample("basic-sample")

        @Override
        Sample getSample() {
            return this.sample
        }
    }

    static class WithExplicitTemporaryFolder extends SampleTestCases {
        @ClassRule
        public static TemporaryFolder temporaryFolder = new TemporaryFolder()
        @Rule
        public Sample sample = Sample.from("src/test/samples/gradle")
            .intoTemporaryFolder(temporaryFolder.getRoot())
            .withDefaultSample("basic-sample")

        @Override
        Sample getSample() {
            return this.sample
        }
    }

    static abstract class SampleTestCases {
        abstract Sample getSample()

        @Test
        void "copies default sample"() {
            File sampleDir = sample.dir
            assert sampleDir.directory
            assert new File(sampleDir, "build.gradle").file
        }

        @UsesSample("composite-sample/basic")
        @Test
        void "copies sample from annotation"() {
            File sampleDir = sample.dir
            assert sample.dir.directory
            assert new File(sampleDir, "compositeBuildsBasicCli.sample.out").file
        }
    }

}
