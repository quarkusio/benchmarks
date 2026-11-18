package io.quarkus.infra.performance.graphics.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_NATIVE;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_SPRING4_COMPAT;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_VIRTUAL;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_JVM_AOT;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_JVM_AOT;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_NATIVE;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_VIRTUAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GroupTest {

    @Test
    public void allIncludesEverything() {
        Group group = Group.ALL;
        assertNotNull(group);
        // This isn't an exhaustive test, but it's indicative
        assertTrue(group.contains(SPRING4_VIRTUAL));
        assertTrue(group.contains(QUARKUS3_JVM));
        assertTrue(group.contains(SPRING4_JVM_AOT));
    }

    // A group constructed by hand
    @Test
    public void mainComparisonHasRightEntries() {
        Group group = Group.MAIN_COMPARISON;
        assertTrue(group.contains(QUARKUS3_NATIVE));
        assertTrue(group.contains(QUARKUS3_JVM));
        assertTrue(group.contains(SPRING4_JVM));
        assertTrue(group.contains(SPRING4_NATIVE));
        assertFalse(group.contains(QUARKUS3_SPRING4_COMPAT));
        assertFalse(group.contains(QUARKUS3_VIRTUAL));
    }

    // A group constructed using a category
    @Test
    public void quarkusIncludesQuarkusButNotSpring() {
        Group group = Group.QUARKUS;
        assertTrue(group.contains(QUARKUS3_NATIVE));
        assertTrue(group.contains(QUARKUS3_JVM));
        assertFalse(group.contains(SPRING4_JVM));
    }

    // A group using an exclusion
    // A group constructed using a category
    @Test
    public void javaFrameworksExcludesOlderSpringVersions() {
        Group group = Group.JAVA_AND_NATIVE_AND_LEYDEN_FRAMEWORKS;
        assertTrue(group.contains(QUARKUS3_NATIVE));
        assertTrue(group.contains(QUARKUS3_JVM));
        assertTrue(group.contains(SPRING4_JVM));
        assertTrue(group.contains(SPRING4_LEYDEN));
        assertTrue(group.contains(SPRING4_JVM_AOT));
        assertFalse(group.contains(SPRING4_VIRTUAL));
        assertFalse(group.contains(SPRING3_JVM));
        assertFalse(group.contains(SPRING3_JVM_AOT));
    }

    @Test
    public void categoryChecks() {
        Group group = Group.JAVA_AND_NATIVE_AND_LEYDEN_FRAMEWORKS;
        assertTrue(group.containsAny(Category.QUARKUS));
        assertTrue(group.containsAny(Category.JVM));
        assertTrue(group.containsAny(Category.NATIVE));
        assertFalse(group.containsAny(Category.VIRTUAL_THREADS));


        group = Group.QUARKUS;
        assertTrue(group.containsAny(Category.QUARKUS));
        assertTrue(group.containsAny(Category.JVM));
        assertTrue(group.containsAny(Category.NATIVE));
        assertFalse(group.containsAny(Category.SPRING));

    }

    // Characterization: members of each group (in enum order) before Quarkus 4 support
    private static final Map<Group, List<String>> BASELINE_MEMBERS = Map.ofEntries(
            Map.entry(Group.MAIN_COMPARISON, List.of(
                    "quarkus3-jvm",
                    "spring4-jvm",
                    "quarkus3-native",
                    "spring4-native")),
            Map.entry(Group.LEYDEN_COMPARISON, List.of(
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
                    "quarkus3-spring-compat",
                    "quarkus3-spring4-compat",
                    "quarkus3-spring3-compat")),
            Map.entry(Group.JAVA_FRAMEWORKS_WITH_COMPATIBILITY, List.of(
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
                    "quarkus3-spring-compat",
                    "quarkus3-spring4-compat",
                    "quarkus3-spring3-compat")),
            Map.entry(Group.QUARKUS, List.of(
                    "quarkus3-jvm",
                    "quarkus3-leyden",
                    "quarkus3-virtual",
                    "quarkus3-virtual-leyden",
                    "quarkus3-native")),
            Map.entry(Group.VIRTUAL_THREADS, List.of(
                    "quarkus3-jvm",
                    "spring4-jvm",
                    "spring-jvm",
                    "spring3-jvm",
                    "spring4-jvm-aot",
                    "spring-jvm-aot",
                    "spring3-jvm-aot",
                    "quarkus3-virtual",
                    "spring4-virtual",
                    "spring-virtual",
                    "spring3-virtual",
                    "quarkus3-spring-compat",
                    "quarkus3-spring4-compat",
                    "quarkus3-spring3-compat")),
            Map.entry(Group.JAVA_AND_NATIVE_FRAMEWORKS, List.of(
                    "quarkus3-jvm",
                    "spring4-jvm",
                    "spring-jvm",
                    "spring3-jvm",
                    "spring4-jvm-aot",
                    "spring-jvm-aot",
                    "spring3-jvm-aot",
                    "quarkus3-native",
                    "spring4-native",
                    "spring-native",
                    "spring3-native",
                    "quarkus3-spring-compat",
                    "quarkus3-spring4-compat",
                    "quarkus3-spring3-compat")),
            Map.entry(Group.ALL, List.of(
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
                    "quarkus3-spring3-compat")),
            Map.entry(Group.JAVA_AND_NATIVE_AND_LEYDEN_FRAMEWORKS, List.of(
                    "quarkus3-jvm",
                    "spring4-jvm",
                    "spring-jvm",
                    "spring4-jvm-aot",
                    "spring-jvm-aot",
                    "quarkus3-leyden",
                    "spring4-leyden",
                    "quarkus3-native",
                    "spring4-native",
                    "spring-native",
                    "quarkus3-spring-compat",
                    "quarkus3-spring4-compat")),
            Map.entry(Group.QUARKUS_JAVA_AND_NATIVE_AND_LEYDEN_FRAMEWORKS, List.of(
                    "quarkus3-jvm",
                    "quarkus3-leyden",
                    "quarkus3-native")));

    @Test
    public void existingGroupMembershipIsUnchanged() {
        for (Group group : Group.values()) {
            List<String> actual = Arrays.stream(KnownFramework.values())
                    .filter(f -> f.isInGroup(group))
                    .map(KnownFramework::getName)
                    .filter(n -> !n.startsWith("quarkus4-") && !n.startsWith("quarkus-"))
                    .toList();
            assertEquals(BASELINE_MEMBERS.get(group), actual, group.name());
        }
    }

    @Test
    public void quarkus4IsPickedUpByCategoryGroups() {
        assertTrue(Group.ALL.contains(KnownFramework.QUARKUS4_NATIVE));
        assertTrue(Group.QUARKUS.contains(KnownFramework.QUARKUS4_JVM));
        assertTrue(Group.JAVA_AND_NATIVE_FRAMEWORKS.contains(KnownFramework.QUARKUS4_JVM));
        assertTrue(Group.VIRTUAL_THREADS.contains(KnownFramework.QUARKUS4_VIRTUAL));
        assertTrue(Group.LEYDEN_COMPARISON.contains(KnownFramework.QUARKUS4_LEYDEN));
        assertTrue(Group.JAVA_AND_NATIVE_AND_LEYDEN_FRAMEWORKS.contains(KnownFramework.QUARKUS4_NATIVE));
        assertTrue(Group.JAVA_FRAMEWORKS_WITH_COMPATIBILITY.contains(KnownFramework.QUARKUS4_SPRING4_COMPAT));
    }

    @Test
    public void quarkus4CompatIsNotInQuarkusGroupLikeQuarkus3Compat() {
        assertFalse(Group.QUARKUS.contains(KnownFramework.QUARKUS4_SPRING4_COMPAT));
    }

    @Test
    public void mainComparisonIsUnchangedByQuarkus4() {
        assertFalse(Group.MAIN_COMPARISON.contains(KnownFramework.QUARKUS4_JVM));
        assertFalse(Group.MAIN_COMPARISON.contains(KnownFramework.QUARKUS4_NATIVE));
        assertFalse(Group.MAIN_COMPARISON.containsAny(Category.QUARKUS4));
    }

    @Test
    public void containsAnyExcludingSkipsQuarkus4() {
        assertTrue(Group.QUARKUS.containsAnyExcluding(Category.QUARKUS, Category.QUARKUS4));
        assertTrue(Group.QUARKUS.containsAny(Category.QUARKUS4));
        assertFalse(Group.MAIN_COMPARISON.containsAny(Category.QUARKUS4));
    }
}
