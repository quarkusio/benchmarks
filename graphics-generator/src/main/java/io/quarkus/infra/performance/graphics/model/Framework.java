package io.quarkus.infra.performance.graphics.model;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Framework {
    // The order of these determines the natural order in the charts
    QUARKUS3_JVM("quarkus3-jvm", "Quarkus\nJIT (via OpenJDK)"),
    SPRING4_JVM("spring4-jvm", "Spring 4\nJIT (via OpenJDK)"),
    SPRING_JVM("spring-jvm", "Spring\nJIT (via OpenJDK)"),
    SPRING3_JVM("spring3-jvm", "Spring 3\nJIT (via OpenJDK)"),
    SPRING4_JVM_AOT("spring4-jvm-aot", "Spring 4\nAOT (via OpenJDK)"),
    SPRING_JVM_AOT("spring-jvm-aot", "Spring\nAOT (via OpenJDK)"),
    SPRING3_JVM_AOT("spring3-jvm-aot", "Spring 3\nAOT (via OpenJDK)"),
    QUARKUS3_VIRTUAL("quarkus3-virtual", "Quarkus w/Virtual Threads\nJIT (via OpenJDK)"),
    SPRING4_VIRTUAL("spring4-virtual", "Spring 4 w/Virtual Threads\nJIT (via OpenJDK)"),
    SPRING_VIRTUAL("spring-virtual", "Spring w/Virtual Threads\nJIT (via OpenJDK)"),
    SPRING3_VIRTUAL("spring3-virtual", "Spring 3 w/Virtual Threads\nJIT (via OpenJDK)"),
    QUARKUS3_NATIVE("quarkus3-native", "Quarkus\nNative (via GraalVM)"),
    SPRING4_NATIVE("spring4-native", "Spring 4\nNative (via GraalVM)"),
    SPRING_NATIVE("spring-native", "Spring\nNative (via GraalVM)"),
    SPRING3_NATIVE("spring3-native", "Spring 3\nNative (via GraalVM)"),
    QUARKUS3_SPRING_COMPAT("quarkus3-spring-compat", "Quarkus\nwith Spring compatibility libraries"),
    QUARKUS3_SPRING4_COMPAT("quarkus3-spring4-compat", "Quarkus\nwith Spring 4 compatibility libraries"),
    QUARKUS3_SPRING3_COMPAT("quarkus3-spring3-compat", "Quarkus\nwith Spring 3 compatibility libraries");

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
      ENUM_MAP = Arrays.stream(values())
          .collect(Collectors.toUnmodifiableMap(framework -> framework.getName().toLowerCase(), Function.identity()));
    }

    @JsonCreator
    public static Framework valueOfIgnoreCase(String name) {
        return ENUM_MAP.get(name.toLowerCase());
    }

}
