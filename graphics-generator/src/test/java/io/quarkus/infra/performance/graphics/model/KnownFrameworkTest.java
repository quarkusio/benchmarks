package io.quarkus.infra.performance.graphics.model;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnownFrameworkTest {
    @Test
    void getExpandedNameForSimpleQuarkusCase() {
        assertEquals("Quarkus\nJIT (via OpenJDK)", KnownFramework.QUARKUS_JVM.getExpandedName());
        assertEquals("Quarkus 3\nJIT (via OpenJDK)", KnownFramework.QUARKUS3_JVM.getExpandedName());
    }

    @Test
    void getExpandedNameForSimpleSpringCase() {
        assertEquals("Spring Boot 3\nJIT (via OpenJDK)", KnownFramework.SPRING3_JVM.getExpandedName());
    }

    @Test
    void getCategories() {
        assertTrue(KnownFramework.QUARKUS3_JVM.hasCategory(Category.QUARKUS));
        assertFalse(KnownFramework.SPRING3_JVM.hasCategory(Category.QUARKUS));
    }

    @Test
    void isInGroupItIsIn() {
        assertTrue(KnownFramework.QUARKUS3_VIRTUAL.isInGroup(Group.QUARKUS));
    }

    @Test
    void isAlwaysInAllGroup() {
        assertTrue(KnownFramework.QUARKUS3_VIRTUAL.isInGroup(Group.ALL));
        assertTrue(KnownFramework.SPRING3_NATIVE.isInGroup(Group.ALL));
        assertTrue(KnownFramework.SPRING_NATIVE.isInGroup(Group.ALL));
        assertTrue(KnownFramework.SPRING3_JVM_AOT.isInGroup(Group.ALL));
        assertTrue(KnownFramework.SPRING4_VIRTUAL.isInGroup(Group.ALL));
        assertTrue(KnownFramework.QUARKUS3_JVM.isInGroup(Group.ALL));
    }

    @Test
    void isInGroupItIsNotIn() {
        assertFalse(KnownFramework.SPRING3_JVM.isInGroup(Group.QUARKUS));
    }

    @Test
    void partitionableCategory() {
        assertEquals(KnownFramework.SPRING3_JVM.getPartitionableCategory(), Category.VANILLA_JIT);
        assertEquals(KnownFramework.SPRING3_NATIVE.getPartitionableCategory(), Category.NATIVE);
        assertEquals(KnownFramework.SPRING3_VIRTUAL.getPartitionableCategory(), Category.VIRTUAL_THREADS);
        assertEquals(KnownFramework.QUARKUS3_VIRTUAL.getPartitionableCategory(), Category.VIRTUAL_THREADS);
    }

    // Characterization: names of frameworks that existed before Quarkus 4 support, in chart order
    static final List<String> BASELINE_NAMES = List.of(
            "quarkus3-jvm",
            "spring4-jvm",
            "spring-jvm",
            "spring3-jvm",
            "spring4-jvm-aot",
            "spring-jvm-aot",
            "spring3-jvm-aot",
            "quarkus3-leyden",
            "spring4-leyden",
            "spring3-leyden",
            "spring-leyden",
            "quarkus3-virtual",
            "spring4-virtual",
            "spring-virtual",
            "spring3-virtual",
            "quarkus3-virtual-leyden",
            "spring4-virtual-leyden",
            "spring3-virtual-leyden",
            "spring-virtual-leyden",
            "quarkus3-native",
            "spring4-native",
            "spring-native",
            "spring3-native",
            "quarkus3-spring-compat",
            "quarkus3-spring4-compat",
            "quarkus3-spring3-compat");

    @Test
    void existingFrameworkOrderIsUnchanged() {
        assertEquals(BASELINE_NAMES, Arrays.stream(KnownFramework.values())
                .map(KnownFramework::getName)
                .filter(n -> !n.startsWith("quarkus4-") && !n.startsWith("quarkus-"))
                .toList());
    }

    @Test
    void quarkus4KeysAreKnownQuarkus4Frameworks() {
        for (String key : new String[]{"quarkus4-jvm", "quarkus4-leyden", "quarkus4-virtual", "quarkus4-virtual-leyden", "quarkus4-native"}) {
            KnownFramework f = KnownFramework.valueOfIgnoreCase(key);
            assertNotNull(f, key);
            assertTrue(f.hasCategory(Category.QUARKUS), key);
            assertTrue(f.hasCategory(Category.QUARKUS4), key);
        }
    }

    @Test
    void quarkus4Spring4CompatIsTaggedLikeQuarkus3Compat() {
        KnownFramework f = KnownFramework.valueOfIgnoreCase("quarkus4-spring4-compat");
        assertNotNull(f);
        assertTrue(f.hasCategory(Category.SPRING));
        assertTrue(f.hasCategory(Category.COMPATIBILITY));
        assertTrue(f.hasCategory(Category.QUARKUS4));
        assertFalse(f.hasCategory(Category.QUARKUS));
        assertEquals("Quarkus 4\nwith Spring Boot 4 compatibility libraries", f.getExpandedName());
    }

    @Test
    void quarkus4Labels() {
        assertEquals("Quarkus 4\nJIT (via OpenJDK)", KnownFramework.QUARKUS4_JVM.getExpandedName());
        assertEquals("Quarkus 4 w/Virtual Threads\nLeyden (via OpenJDK)", KnownFramework.QUARKUS4_VIRTUAL_LEYDEN.getExpandedName());
        assertEquals("Quarkus 4\nNative (via GraalVM)", KnownFramework.QUARKUS4_NATIVE.getExpandedName());
    }

    @Test
    void quarkus3IsQualifiedAndUnqualifiedTwinsAreNotQuarkus4() {
        assertEquals("Quarkus 3\nJIT (via OpenJDK)", KnownFramework.QUARKUS3_JVM.getExpandedName());
        assertEquals("Quarkus 3\nwith Spring compatibility libraries", KnownFramework.QUARKUS3_SPRING_COMPAT.getExpandedName());
        assertEquals("Quarkus 3\nwith Spring Boot 4 compatibility libraries", KnownFramework.QUARKUS3_SPRING4_COMPAT.getExpandedName());
        assertEquals("Quarkus 3\nwith Spring Boot 3 compatibility libraries", KnownFramework.QUARKUS3_SPRING3_COMPAT.getExpandedName());
        assertEquals("Quarkus\nwith Spring compatibility libraries", KnownFramework.QUARKUS_SPRING_COMPAT.getExpandedName());
        assertEquals("Quarkus\nwith Spring Boot 3 compatibility libraries", KnownFramework.QUARKUS_SPRING3_COMPAT.getExpandedName());
        assertEquals("Quarkus\nwith Spring Boot 4 compatibility libraries", KnownFramework.QUARKUS_SPRING4_COMPAT.getExpandedName());
        assertTrue(KnownFramework.QUARKUS3_SPRING3_COMPAT.hasCategory(Category.OLD));
        assertFalse(KnownFramework.QUARKUS3_SPRING4_COMPAT.hasCategory(Category.OLD));
        assertFalse(KnownFramework.QUARKUS_JVM.hasCategory(Category.QUARKUS4));
        assertFalse(KnownFramework.QUARKUS3_JVM.hasCategory(Category.QUARKUS4));
        assertTrue(KnownFramework.QUARKUS4_JVM.compareTo(KnownFramework.QUARKUS3_JVM) < 0);
        assertTrue(KnownFramework.QUARKUS4_NATIVE.compareTo(KnownFramework.QUARKUS3_NATIVE) < 0);
    }
}
