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

import java.lang.annotation.*;

/**
 * Specifies the directory to find samples, be they external or embedded.
 *
 * This directory is relative to project where Exemplar is invoked.
 *
 * For example, given this structure:
 *
 * <pre>
 * monorepo/
 * ├── build.gradle
 * ├── subprojectBar/
 * │   └── build.gradle
 * │   └── src/
 * │       ├── samples/
 * │       │   └── bar.adoc
 * │       └── test/
 * │           └── java/
 * │               └── DocsSampleTest.java
 * └── subprojectFoo/
 *     └── src/
 * </pre>
 *
 * ...DocsSampleTest should declare @AsciidocSourcesRoot("src/samples").
 *
 * @see SamplesRunner
 */
@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SamplesRoot {
    String value();
}
