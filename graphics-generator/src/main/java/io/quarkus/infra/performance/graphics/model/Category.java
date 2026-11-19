package io.quarkus.infra.performance.graphics.model;

public enum Category {
    JVM,
    VANILLA_JIT(true),
    QUARKUS,
    QUARKUS4,
    NATIVE(true),
    AOT(true),
    LEYDEN(true),
    VIRTUAL_THREADS(true),
    COMPATIBILITY,
    SPRING,
    OLD;

    // True if a change in this value should trigger a divider in a plot
    boolean isPartitionable = false;

    Category() {
        this.isPartitionable = false;
    }

    Category(boolean b) {
        this.isPartitionable = b;
    }

    public boolean isPartitionable() {
        return isPartitionable;
    }
}
