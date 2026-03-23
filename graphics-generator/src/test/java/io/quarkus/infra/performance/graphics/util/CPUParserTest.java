package io.quarkus.infra.performance.graphics.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for CPUParser class that parses Linux CPU list format strings.
 * The parser supports various formats:
 * - Range notation: "0-3" represents CPUs 0, 1, 2, 3
 * - Comma-separated list: "0,1,2,3" represents CPUs 0, 1, 2, 3
 * - Range with step: "0-6:2" represents CPUs 0, 2, 4, 6 (every 2nd CPU)
 * - Mixed formats: "0,2,4,6" represents CPUs 0, 2, 4, 6
 */
public class CPUParserTest {

    @Test
    public void testParseRangeFormat() {
        // Test range format: "0-3" should parse to 4 CPUs (0, 1, 2, 3)
        assertEquals(4, CPUParser.parse("0-3"));
    }

    @Test
    public void testParseCommaSeparatedList() {
        // Test comma-separated list: "0,1,2,3" should parse to 4 CPUs
        assertEquals(4, CPUParser.parse("0,1,2,3"));
    }

    @Test
    public void testParseRangeWithStep() {
        // Test range with step: "0-6:2" should parse to 4 CPUs (0, 2, 4, 6)
        assertEquals(4, CPUParser.parse("0-6:2"));
    }

    @Test
    public void testParseExplicitList() {
        // Test explicit list: "0,2,4,6" should parse to 4 CPUs
        assertEquals(4, CPUParser.parse("0,2,4,6"));
    }

    @Test
    public void testParseSingleCPU() {
        // Test single CPU
        assertEquals(1, CPUParser.parse("0"));
        assertEquals(1, CPUParser.parse("5"));
    }

    @Test
    public void testParseEmptyString() {
        // Test empty string
        assertEquals(0, CPUParser.parse(""));
        assertEquals(0, CPUParser.parse("   "));
    }

    @Test
    public void testParseNull() {
        // Test null input
        assertEquals(0, CPUParser.parse(null));
    }

    @Test
    public void testParseLargerRange() {
        // Test larger range: "0-7" should parse to 8 CPUs
        assertEquals(8, CPUParser.parse("0-7"));
    }

    @Test
    public void testParseRangeWithLargerStep() {
        // Test range with larger step: "0-10:3" should parse to 4 CPUs (0, 3, 6, 9)
        assertEquals(4, CPUParser.parse("0-10:3"));
    }

    @Test
    public void testParseMixedFormat() {
        // Test mixed format: "0-2,5,7-9" should parse to 7 CPUs (0, 1, 2, 5, 7, 8, 9)
        assertEquals(7, CPUParser.parse("0-2,5,7-9"));
    }

    @Test
    public void testParseDuplicates() {
        // Test that duplicates are handled correctly (should count unique CPUs)
        // "0,1,1,2" should parse to 3 CPUs (0, 1, 2)
        assertEquals(3, CPUParser.parse("0,1,1,2"));
    }

    @Test
    public void testParseWithWhitespace() {
        // Test that whitespace is handled correctly
        assertEquals(4, CPUParser.parse(" 0-3 "));
        assertEquals(4, CPUParser.parse("0, 1, 2, 3"));
    }
}

// Made with Bob
