package org.acme.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum Framework {
    QUARKUS3_JVM("quarkus3-jvm"),
    QUARKUS3_NATIVE("quarkus3-native"),
    SPRING3_JVM("spring3-jvm"),
    SPRING3_JVM_AOT("spring3-jvm-aot"),
    SPRING3_NATIVE("spring3-native");

    private String name;
    private static final Map<String, Framework> ENUM_MAP;

    Framework(String name) {
        this.name = name;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }

    // Build an immutable map of String name to enum pairs.

    static {
        Map<String, Framework> map = new ConcurrentHashMap<String, Framework>();
        for (Framework instance : Framework.values()) {
            map.put(instance.getName().toLowerCase(), instance);
        }
        ENUM_MAP = Collections.unmodifiableMap(map);
    }

    @JsonCreator
    public static Framework valueOfIgnoreCase(String name) {
        return ENUM_MAP.get(name.toLowerCase());
    }

}
