package io.quarkus.infra.performance.graphics.model;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Framework {
    QUARKUS3_JVM("quarkus3-jvm", "Quarkus + JIT\n(via OpenJDK)"),
    QUARKUS3_NATIVE("quarkus3-native", "Quarkus + Native\n(via GraalVM)"),
    SPRING3_JVM("spring3-jvm", "Spring + JIT\n(via OpenJDK)"),
    SPRING3_JVM_AOT("spring3-jvm-aot", "Spring AOT\n(via OpenJDK)"),
    SPRING3_NATIVE("spring3-native", "Spring + Native\n(via GraalVM)"),
    QUARKUS3_SPRING_COMPAT("quarkus3-spring-compat", "Spring\n with Quarkus compatibility libraries");

    private final String name;
    private final String expandedName;
    private static final Map<String, Framework> ENUM_MAP;

    Framework(String name, String expandedName) {
        this.name = name;
        this.expandedName = expandedName;
    }

    @JsonValue
    public String getName() {
        return this.name;
    }

    public String getExpandedName() {
        return this.expandedName;
    }

    // Build an immutable map of String name to enum pairs.

    static {
        Map<String, Framework> map = new ConcurrentHashMap<>();
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
