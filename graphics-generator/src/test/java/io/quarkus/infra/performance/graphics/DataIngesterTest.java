package io.quarkus.infra.performance.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.time.Instant;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Test;

import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.Framework;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class DataIngesterTest {

    @Inject
    DataIngester dataIngester;

    @Test
    public void testIngest() {
        File file = new File("src/test/resources/data.json");
        BenchmarkData data = dataIngester.ingest(file);
        assertNotNull(data);

        assertNotNull(data.results());
        assertNotNull(data.timing());
        assertNotNull(data.config());

        // Don't check every value, but do some drilling down to sense check

        // Config
        assertEquals("25-tem", data.config().jvm().version());
        assertEquals("25-graalce", data.config().jvm().graalVM().version());
        assertEquals("3.28.4", data.config().quarkus().version());

        // CGroup
        assertEquals("14G", data.config().cgroup().memMax());

        // Results
        assertEquals(27586.713333333333, data.results().framework(Framework.QUARKUS3_JVM).load().avThroughput());
        assertEquals(6.496666666666667, data.results().framework(Framework.SPRING3_NATIVE).build().avNativeRSS());

        // Timing
        assertEquals(Instant.parse("2025-10-20T16:05:51Z"), data.timing().start());
        assertEquals(Instant.parse("2025-10-20T17:34:02Z"), data.timing().stop());

        // Native
        assertEquals("11,620", data.results().framework(Framework.SPRING3_NATIVE).build().reflectionClassCount());
    }

}