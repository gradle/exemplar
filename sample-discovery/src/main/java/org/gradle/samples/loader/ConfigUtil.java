/*
 * Copyright 2016 the original author or authors.
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
package org.gradle.samples.loader;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;

import java.io.File;
import java.util.*;

class ConfigUtil {
    public static Map<String, String> map(Config config, String key, Map<String, String> defaultValues) {
        if (config.hasPath(key)) {
            Map<String, String> props = new LinkedHashMap<>();
            for (Map.Entry<String, ConfigValue> entry : config.getConfig(key).entrySet()) {
                props.put(entry.getKey(), entry.getValue().unwrapped().toString());
            }
            return props;
        } else {
            return defaultValues;
        }
    }

    public static boolean booleanValue(Config config, String key, boolean defaultValue) {
        if (config.hasPath(key)) {
            return Boolean.valueOf(config.getString(key));
        } else {
            return defaultValue;
        }
    }

    public static String string(Config config, String key, String defaultValue) {
        if (config.hasPath(key)) {
            return config.getString(key);
        } else {
            return defaultValue;
        }
    }

    public static List<String> strings(Config config, String key, List<String> defaults) {
        if (config.hasPath(key)) {
            Object value = config.getAnyRef(key);
            if (value instanceof List) {
                List<String> result = new ArrayList<>();
                for (Object o : (List) value) {
                    result.add(o.toString());
                }
                return result;
            } else if (value.toString().length() > 0) {
                return Arrays.asList(value.toString().split(" "));
            }
        }
        return defaults;
    }

    public static File file(Config config, File projectDir, String key, File defaultValue) {
        String fileName = ConfigUtil.string(config, key, null);
        if (fileName == null) {
            return defaultValue;
        } else {
            return new File(projectDir, fileName);
        }
    }
}
