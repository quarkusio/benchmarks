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
        assertEquals("25.0.1-tem", data.config().jvm().version());
        assertEquals("25.0.1-graalce", data.config().jvm().graalVM().version());
        assertEquals("3.30.5", data.config().quarkus().version());

        // Resources
        assertNotNull(data.config().resources());
        assertNotNull(data.config().resources().cpu());
        assertEquals(4, data.config().resources().appCpus());
        assertEquals("0-3", data.config().resources().cpu().app());
        assertEquals("10", data.config().resources().cpu().firstRequest());
        assertEquals("7-9", data.config().resources().cpu().loadGenerator());
        assertEquals("4-6", data.config().resources().cpu().db());

        // Results
        assertEquals(18615.88333333333, data.results().framework(Framework.QUARKUS3_JVM).load().avThroughput().getValue());
        assertEquals(24146.0333333333, data.results().framework(Framework.QUARKUS3_VIRTUAL).load().avThroughput().getValue());
        assertEquals(7.336666666666666, data.results().framework(Framework.SPRING3_NATIVE).build().avNativeRSS().getValue());
        assertEquals(35845, data.results().framework(Framework.SPRING3_NATIVE).build().classCount().getValue());
        assertEquals(9861.11666666667, data.results().framework(Framework.SPRING3_VIRTUAL).load().avThroughput().getValue());
        assertEquals(7256.486666666667, data.results().framework(Framework.SPRING4_JVM).load().avThroughput().getValue());
        assertEquals(10071.8333333333, data.results().framework(Framework.SPRING4_VIRTUAL).load().avThroughput().getValue());

        // Timing
        assertEquals(Instant.parse("2025-11-18T22:28:52Z"), data.timing().start());
        assertEquals(Instant.parse("2025-11-19T00:19:44Z"), data.timing().stop());
        assertEquals(11.050000000000002,
                data.results().framework(Framework.QUARKUS3_JVM).build().avBuildTime().getValue());

        // Native
        assertEquals(11887, data.results().framework(Framework.SPRING3_NATIVE).build().reflectionClassCount().getValue());
    }

}