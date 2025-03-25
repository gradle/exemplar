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
package org.gradle.exemplar.test;

import org.gradle.exemplar.executor.CommandExecutor;
import org.gradle.exemplar.test.engine.CommandExecutorParams;
import org.gradle.exemplar.test.engine.DefaultCommandExecutorFunction;
import org.gradle.exemplar.test.normalizer.OutputNormalizer;

import java.io.File;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;
import java.util.function.Supplier;

@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Samples {
    String root();

    SamplesType samplesType() default SamplesType.DEFAULT;

    Class<? extends SampleModifier>[] modifiers() default { NoopSampleModifier.class };

    Class<? extends OutputNormalizer>[] outputNormalizers() default { NoopOutputNormalizer.class };

    Class<? extends Supplier<File>> implicitRootDirSupplier() default NoopRootDirSupplier.class;

    Class<? extends Function<CommandExecutorParams, CommandExecutor>> commandExecutorFunction() default DefaultCommandExecutorFunction.class;

    enum SamplesType {
        DEFAULT,
        EMBEDDED
    }
}
