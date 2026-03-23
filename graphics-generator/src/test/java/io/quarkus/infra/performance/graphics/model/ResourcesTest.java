package io.quarkus.infra.performance.graphics.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for Resources deserialization that verifies appCpus is calculated from cpu.app field.
 */
public class ResourcesTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testDeserializeWithRangeFormat() throws Exception {
        String json = """
            {
                "cpu": {
                    "app": "0-3",
                    "1st_request": "4-7",
                    "load_generator": "8-11",
                    "db": "12-15"
                }
            }
            """;

        Resources resources = mapper.readValue(json, Resources.class);
        
        assertEquals(4, resources.appCpus(), "Should parse '0-3' as 4 CPUs");
        assertEquals("0-3", resources.cpu().app());
    }

    @Test
    public void testDeserializeWithCommaSeparatedList() throws Exception {
        String json = """
            {
                "cpu": {
                    "app": "0,1,2,3",
                    "1st_request": "4,5,6,7",
                    "load_generator": "8,9,10,11",
                    "db": "12,13,14,15"
                }
            }
            """;

        Resources resources = mapper.readValue(json, Resources.class);
        
        assertEquals(4, resources.appCpus(), "Should parse '0,1,2,3' as 4 CPUs");
        assertEquals("0,1,2,3", resources.cpu().app());
    }

    @Test
    public void testDeserializeWithRangeAndStep() throws Exception {
        String json = """
            {
                "cpu": {
                    "app": "0-6:2",
                    "1st_request": "1-7:2",
                    "load_generator": "8-14:2",
                    "db": "9-15:2"
                }
            }
            """;

        Resources resources = mapper.readValue(json, Resources.class);
        
        assertEquals(4, resources.appCpus(), "Should parse '0-6:2' as 4 CPUs (0,2,4,6)");
        assertEquals("0-6:2", resources.cpu().app());
    }

    @Test
    public void testDeserializeWithExplicitList() throws Exception {
        String json = """
            {
                "cpu": {
                    "app": "0,2,4,6",
                    "1st_request": "1,3,5,7",
                    "load_generator": "8,10,12,14",
                    "db": "9,11,13,15"
                }
            }
            """;

        Resources resources = mapper.readValue(json, Resources.class);
        
        assertEquals(4, resources.appCpus(), "Should parse '0,2,4,6' as 4 CPUs");
        assertEquals("0,2,4,6", resources.cpu().app());
    }

    @Test
    public void testDeserializeWithNullCpu() throws Exception {
        String json = "{}";

        Resources resources = mapper.readValue(json, Resources.class);
        
        assertEquals(0, resources.appCpus(), "Should return 0 when cpu is null");
    }

    @Test
    public void testDeserializeWithNullAppCpu() throws Exception {
        String json = """
            {
                "cpu": {
                    "1st_request": "4-7",
                    "load_generator": "8-11",
                    "db": "12-15"
                }
            }
            """;

        Resources resources = mapper.readValue(json, Resources.class);
        
        assertEquals(0, resources.appCpus(), "Should return 0 when cpu.app is null");
    }

    @Test
    public void testDeserializeWithLegacyAppCpusField() throws Exception {
        // Test backwards compatibility if app_cpus is explicitly provided
        String json = """
            {
                "app_cpus": 8,
                "cpu": {
                    "app": "0-3",
                    "1st_request": "4-7",
                    "load_generator": "8-11",
                    "db": "12-15"
                }
            }
            """;

        Resources resources = mapper.readValue(json, Resources.class);
        
        // The @JsonCreator should calculate from cpu.app, overriding any explicit app_cpus
        assertEquals(4, resources.appCpus(), "Should calculate from cpu.app, not use explicit app_cpus");
    }
}

// Made with Bob
