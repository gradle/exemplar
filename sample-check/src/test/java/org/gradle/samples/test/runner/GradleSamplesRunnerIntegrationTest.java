/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.samples.test.runner;

import org.gradle.samples.test.normalizer.FileSeparatorOutputNormalizer;
import org.gradle.samples.test.normalizer.GradleOutputNormalizer;
import org.gradle.samples.test.normalizer.JavaObjectSerializationOutputNormalizer;
import org.junit.runner.RunWith;

@RunWith(GradleSamplesRunner.class)
@SamplesRoot("src/test/samples/gradle")
// tag::sample-output-normalizers[]
@SamplesOutputNormalizers({JavaObjectSerializationOutputNormalizer.class, FileSeparatorOutputNormalizer.class, GradleOutputNormalizer.class})
// end::sample-output-normalizers[]
public class GradleSamplesRunnerIntegrationTest {
}
