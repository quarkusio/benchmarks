package io.quarkus.infra.performance.graphics.model;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import static io.quarkus.infra.performance.graphics.model.Category.AOT;
import static io.quarkus.infra.performance.graphics.model.Category.COMPATIBILITY;
import static io.quarkus.infra.performance.graphics.model.Category.JVM;
import static io.quarkus.infra.performance.graphics.model.Category.LEYDEN;
import static io.quarkus.infra.performance.graphics.model.Category.NATIVE;
import static io.quarkus.infra.performance.graphics.model.Category.OLD;
import static io.quarkus.infra.performance.graphics.model.Category.QUARKUS;
import static io.quarkus.infra.performance.graphics.model.Category.QUARKUS4;
import static io.quarkus.infra.performance.graphics.model.Category.SPRING;
import static io.quarkus.infra.performance.graphics.model.Category.VANILLA_JIT;
import static io.quarkus.infra.performance.graphics.model.Category.VIRTUAL_THREADS;


public enum KnownFramework implements Framework {
    // The order of these determines the natural order in the charts
    QUARKUS4_JVM("quarkus4-jvm", "Quarkus 4\nJIT (via OpenJDK)", EnumSet.of(QUARKUS, QUARKUS4, JVM, VANILLA_JIT)),
    QUARKUS_JVM("quarkus-jvm", "Quarkus\nJIT (via OpenJDK)", EnumSet.of(QUARKUS, JVM, VANILLA_JIT)),
    QUARKUS3_JVM("quarkus3-jvm", "Quarkus 3\nJIT (via OpenJDK)", EnumSet.of(QUARKUS, JVM, VANILLA_JIT)),
    SPRING4_JVM("spring4-jvm", "Spring Boot 4\nJIT (via OpenJDK)", EnumSet.of(SPRING, JVM, VANILLA_JIT)),
    SPRING_JVM("spring-jvm", "Spring Boot\nJIT (via OpenJDK)", EnumSet.of(SPRING, JVM, VANILLA_JIT)),
    SPRING3_JVM("spring3-jvm", "Spring Boot 3\nJIT (via OpenJDK)", EnumSet.of(SPRING, JVM, OLD, VANILLA_JIT)),
    SPRING4_JVM_AOT("spring4-jvm-aot", "Spring Boot 4\nAOT (via OpenJDK)", EnumSet.of(SPRING, JVM, AOT)),
    SPRING_JVM_AOT("spring-jvm-aot", "Spring Boot\nAOT (via OpenJDK)", EnumSet.of(SPRING, JVM, AOT)),
    SPRING3_JVM_AOT("spring3-jvm-aot", "Spring Boot 3\nAOT (via OpenJDK)", EnumSet.of(SPRING, JVM, AOT, OLD)),
    QUARKUS4_LEYDEN("quarkus4-leyden", "Quarkus 4\nLeyden (via OpenJDK)", EnumSet.of(QUARKUS, QUARKUS4, JVM, LEYDEN)),
    QUARKUS_LEYDEN("quarkus-leyden", "Quarkus\nLeyden (via OpenJDK)", EnumSet.of(QUARKUS, JVM, LEYDEN)),
    QUARKUS3_LEYDEN("quarkus3-leyden", "Quarkus 3\nLeyden (via OpenJDK)", EnumSet.of(QUARKUS, JVM, LEYDEN)),
    SPRING4_LEYDEN("spring4-leyden", "Spring Boot 4\nLeyden (via OpenJDK)", EnumSet.of(SPRING, JVM, LEYDEN)),
    SPRING3_LEYDEN("spring3-leyden", "Spring Boot 3\nLeyden (via OpenJDK)", EnumSet.of(SPRING, JVM, LEYDEN, OLD)),
    SPRING_LEYDEN("spring-leyden", "Spring Boot\nLeyden (via OpenJDK)", EnumSet.of(SPRING, JVM, LEYDEN, OLD)),
    QUARKUS4_VIRTUAL("quarkus4-virtual", "Quarkus 4 w/Virtual Threads\nJIT (via OpenJDK)", EnumSet.of(QUARKUS, QUARKUS4, JVM, VIRTUAL_THREADS)),
    QUARKUS_VIRTUAL("quarkus-virtual", "Quarkus w/Virtual Threads\nJIT (via OpenJDK)", EnumSet.of(QUARKUS, JVM, VIRTUAL_THREADS)),
    QUARKUS3_VIRTUAL("quarkus3-virtual", "Quarkus 3 w/Virtual Threads\nJIT (via OpenJDK)", EnumSet.of(QUARKUS, JVM, VIRTUAL_THREADS)),
    SPRING4_VIRTUAL("spring4-virtual", "Spring Boot 4 w/Virtual Threads\nJIT (via OpenJDK)", EnumSet.of(SPRING, JVM, VIRTUAL_THREADS)),
    SPRING_VIRTUAL("spring-virtual", "Spring Boot w/Virtual Threads\nJIT (via OpenJDK)", EnumSet.of(SPRING, JVM, VIRTUAL_THREADS)),
    SPRING3_VIRTUAL("spring3-virtual", "Spring Boot 3 w/Virtual Threads\nJIT (via OpenJDK)", EnumSet.of(SPRING, JVM, VIRTUAL_THREADS, OLD)),
    QUARKUS4_VIRTUAL_LEYDEN("quarkus4-virtual-leyden", "Quarkus 4 w/Virtual Threads\nLeyden (via OpenJDK)", EnumSet.of(QUARKUS, QUARKUS4, JVM, VIRTUAL_THREADS, LEYDEN)),
    QUARKUS_VIRTUAL_LEYDEN("quarkus-virtual-leyden", "Quarkus w/Virtual Threads\nLeyden (via OpenJDK)", EnumSet.of(QUARKUS, JVM, VIRTUAL_THREADS, LEYDEN)),
    QUARKUS3_VIRTUAL_LEYDEN("quarkus3-virtual-leyden", "Quarkus 3 w/Virtual Threads\nLeyden (via OpenJDK)", EnumSet.of(QUARKUS, JVM, VIRTUAL_THREADS, LEYDEN)),
    SPRING4_VIRTUAL_LEYDEN("spring4-virtual-leyden", "Spring Boot 4 w/Virtual Threads\nLeyden (via OpenJDK)", EnumSet.of(SPRING, JVM, VIRTUAL_THREADS, LEYDEN)),
    SPRING3_VIRTUAL_LEYDEN("spring3-virtual-leyden", "Spring Boot 3 w/Virtual Threads\nLeyden (via OpenJDK)", EnumSet.of(SPRING, JVM, VIRTUAL_THREADS, LEYDEN, OLD)),
    SPRING_VIRTUAL_LEYDEN("spring-virtual-leyden", "Spring Boot w/Virtual Threads\nLeyden (via OpenJDK)", EnumSet.of(SPRING, JVM, VIRTUAL_THREADS, LEYDEN, OLD)),
    QUARKUS4_NATIVE("quarkus4-native", "Quarkus 4\nNative (via GraalVM)", EnumSet.of(QUARKUS, QUARKUS4, NATIVE)),
    QUARKUS_NATIVE("quarkus-native", "Quarkus\nNative (via GraalVM)", EnumSet.of(QUARKUS, NATIVE)),
    QUARKUS3_NATIVE("quarkus3-native", "Quarkus 3\nNative (via GraalVM)", EnumSet.of(QUARKUS, NATIVE)),
    SPRING4_NATIVE("spring4-native", "Spring Boot 4\nNative (via GraalVM)", EnumSet.of(SPRING, NATIVE)),
    SPRING_NATIVE("spring-native", "Spring Boot\nNative (via GraalVM)", EnumSet.of(SPRING, NATIVE)),
    SPRING3_NATIVE("spring3-native", "Spring Boot 3\nNative (via GraalVM)", EnumSet.of(SPRING, NATIVE, OLD)),
    QUARKUS4_SPRING4_COMPAT("quarkus4-spring4-compat", "Quarkus 4\nwith Spring Boot 4 compatibility libraries", EnumSet.of(SPRING, QUARKUS4, COMPATIBILITY, JVM)),
    QUARKUS_SPRING_COMPAT("quarkus-spring-compat", "Quarkus\nwith Spring compatibility libraries", EnumSet.of(SPRING, COMPATIBILITY, JVM)),
    QUARKUS3_SPRING_COMPAT("quarkus3-spring-compat", "Quarkus 3\nwith Spring compatibility libraries", EnumSet.of(SPRING, COMPATIBILITY, JVM)),
    QUARKUS_SPRING4_COMPAT("quarkus-spring4-compat", "Quarkus\nwith Spring Boot 4 compatibility libraries", EnumSet.of(SPRING, COMPATIBILITY, JVM)),
    QUARKUS3_SPRING4_COMPAT("quarkus3-spring4-compat", "Quarkus 3\nwith Spring Boot 4 compatibility libraries", EnumSet.of(SPRING, COMPATIBILITY, JVM)),
    QUARKUS_SPRING3_COMPAT("quarkus-spring3-compat", "Quarkus\nwith Spring Boot 3 compatibility libraries", EnumSet.of(SPRING, COMPATIBILITY, JVM, OLD)),
    QUARKUS3_SPRING3_COMPAT("quarkus3-spring3-compat", "Quarkus 3\nwith Spring Boot 3 compatibility libraries", EnumSet.of(SPRING, COMPATIBILITY, JVM, OLD));

    private final String name;
    private final String expandedName;
    private static final Map<String, KnownFramework> ENUM_MAP;
    private final Set<Category> categories;

    KnownFramework(String name, String expandedName, Set<Category> categories) {
        this.name = name;
        this.expandedName = expandedName;
        this.categories = categories;
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
    public static KnownFramework valueOfIgnoreCase(String name) {
        return ENUM_MAP.get(name.toLowerCase());
    }

    public boolean isInGroup(Group group) {
        return group.contains(this);
    }

    public boolean hasCategory(Category category) {
        return categories.contains(category);
    }

    public Category getPartitionableCategory() {
        return categories.stream().filter(c -> c.isPartitionable()).findFirst().orElse(null);
    }

    @Override
    public Set<Category> getCategories() {
        return Set.copyOf(categories);
    }
}
