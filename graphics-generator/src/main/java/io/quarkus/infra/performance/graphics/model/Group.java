package io.quarkus.infra.performance.graphics.model;


import java.util.EnumSet;
import java.util.Set;

import static io.quarkus.infra.performance.graphics.model.Category.COMPATIBILITY;
import static io.quarkus.infra.performance.graphics.model.Category.JVM;
import static io.quarkus.infra.performance.graphics.model.Category.LEYDEN;
import static io.quarkus.infra.performance.graphics.model.Category.NATIVE;
import static io.quarkus.infra.performance.graphics.model.Category.OLD;
import static io.quarkus.infra.performance.graphics.model.Category.SPRING;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_NATIVE;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_NATIVE;

/**
 * A group of frameworks for plotting.
 * You can define a group by explicitly listing the frameworks in it, by listing frameworks (which can be empty) and also categories for inclusion (by OR), or frameworks, categories, and categories which should be excluded.
 */
public enum Group {

    MAIN_COMPARISON(EnumSet.of(
            QUARKUS3_JVM, SPRING4_JVM, QUARKUS3_NATIVE, SPRING4_NATIVE
    ), null),

    LEYDEN_COMPARISON(EnumSet.noneOf(KnownFramework.class), Set.of(JVM, LEYDEN), EnumSet.of(Category.VIRTUAL_THREADS)),

    JAVA_FRAMEWORKS_WITH_COMPATIBILITY(EnumSet.of(JVM, COMPATIBILITY)),

    QUARKUS(EnumSet.of(
            Category.QUARKUS
    )),

    QUARKUS_LEYDEN_AND_NATIVE(EnumSet.noneOf(KnownFramework.class), EnumSet.of(
            NATIVE,
            LEYDEN
    ), EnumSet.of(OLD, Category.VIRTUAL_THREADS, SPRING)),

    VIRTUAL_THREADS(EnumSet.noneOf(KnownFramework.class), EnumSet.of(
            Category.VIRTUAL_THREADS,
            JVM
    ), EnumSet.of(Category.LEYDEN)),

    JAVA_AND_NATIVE_FRAMEWORKS(EnumSet.noneOf(KnownFramework.class), EnumSet.of(
            JVM, NATIVE
    ), EnumSet.of(LEYDEN, Category.VIRTUAL_THREADS)),

    ALL(EnumSet.allOf(KnownFramework.class), null),

    JAVA_AND_NATIVE_AND_LEYDEN_FRAMEWORKS(EnumSet.noneOf(KnownFramework.class), EnumSet.of(
            NATIVE,
            LEYDEN,
            JVM
    ), EnumSet.of(OLD, Category.VIRTUAL_THREADS)),

    QUARKUS_JAVA_AND_NATIVE_AND_LEYDEN_FRAMEWORKS(EnumSet.noneOf(KnownFramework.class), EnumSet.of(
            NATIVE,
            LEYDEN,
            JVM
    ), EnumSet.of(OLD, Category.VIRTUAL_THREADS, SPRING));

    private final Set<KnownFramework> frameworks;

    Group(Set<Category> categories) {
        this(EnumSet.noneOf(KnownFramework.class), categories);
    }

    Group(EnumSet<KnownFramework> frameworks, Set<Category> categories) {
        this(frameworks, categories, EnumSet.noneOf(Category.class));
    }

    /**
     * Include all the named frameworks and also everything with the named category.
     */
    Group(EnumSet<KnownFramework> frameworks, Set<Category> categories, Set<Category> exclusions) {
        this.frameworks = EnumSet.copyOf(frameworks);
        if (categories != null) {
            for (Category category : categories) {
                for (KnownFramework candidate : KnownFramework.values()) {
                    if (candidate.hasCategory(category)) {
                        // Now check the candidate doesn't have any of the exclusions
                        boolean isExcluded = false;
                        for (Category exclusion : exclusions) {
                            if (candidate.hasCategory(exclusion)) {
                                isExcluded = true;
                                break;
                            }
                        }
                        if (! isExcluded) {
                            this.frameworks.add(candidate);
                        }
                    }
                }
            }
        }
    }

    public boolean contains(Framework framework) {
        return frameworks.contains(framework);
    }

    public boolean containsAny(Category category) {
        for (Framework f : frameworks) {
            if (f.hasCategory(category)) {
                return true;
            }
        }
        return false;
    }
}
