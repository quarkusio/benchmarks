package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.acme.model.BenchmarkData;
import org.acme.model.Framework;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        assertEquals("25-tem", data.config().javaVersion());
        assertEquals("3.28.4", data.config().quarkusVersion());

        // CGroup
        assertEquals("14G", data.config().cgroup().memMax());

        // Results

        assertEquals(27586.713333333333, data.results().framework(Framework.QUARKUS3_JVM).load().avThroughput());
        assertEquals(6.496666666666667, data.results().framework(Framework.SPRING3_NATIVE).build().avNativeRSS());
    }

}