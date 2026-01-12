package io.quarkus.infra.performance.graphics.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class FrameworkTest {
    @Test
    void getExpandedNameForSimpleQuarkusCase() {
        assertEquals("Quarkus\nJIT (via OpenJDK)", Framework.QUARKUS3_JVM.getExpandedName());
    }

    @Test
    void getExpandedNameForSimpleSpringCase() {
        assertEquals("Spring 3\nJIT (via OpenJDK)", Framework.SPRING3_JVM.getExpandedName());
    }

}