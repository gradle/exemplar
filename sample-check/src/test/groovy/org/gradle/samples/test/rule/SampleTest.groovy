package org.gradle.samples.test.rule

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SampleTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Rule
    public Sample sample = new Sample("src/test/samples/gradle", temporaryFolder, "basic-sample")

    @Test
    void "copies default sample"() {
        assert sample.dir == new File(temporaryFolder.getRoot(), "samples/basic-sample")
        assert sample.dir.directory
        assert new File(sample.dir, "build.gradle").file
    }

    @UsesSample("composite-sample/basic")
    @Test
    void "copies sample from annotation"() {
        assert sample.dir == new File(temporaryFolder.getRoot(), "samples/composite-sample/basic")
        assert sample.dir.directory
        assert new File(sample.dir, "compositeBuildsBasicCli.sample.out").file
    }
}
