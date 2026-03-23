package io.quarkus.infra.performance.graphics.util;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Parser for Linux CPU list format strings.
 * Supports formats like:
 * - Range: "0-3" (CPUs 0, 1, 2, 3)
 * - List: "0,1,2,3" (CPUs 0, 1, 2, 3)
 * - Range with step: "0-6:2" (CPUs 0, 2, 4, 6)
 * - Mixed: "0,2,4,6" (CPUs 0, 2, 4, 6)
 */
public class CPUParser {

    /**
     * Parse a CPU list string and return the count of CPUs.
     *
     * @param cpuList the CPU list string (e.g., "0-3", "0,1,2,3", "0-6:2")
     * @return the number of CPUs in the list
     */
    public static int parse(String cpuList) {
        if (cpuList == null || cpuList.isBlank()) {
            return 0;
        }

        return (int) Arrays.stream(cpuList.split(","))
                .map(String::trim)
                .flatMapToInt(CPUParser::parsePart)
                .distinct()
                .count();
    }

    private static IntStream parsePart(String part) {
        if (part.contains("-")) {
            return parseRange(part);
        }
        return IntStream.of(Integer.parseInt(part));
    }

    private static IntStream parseRange(String range) {
        String[] stepParts = range.split(":");
        int step = stepParts.length > 1 ? Integer.parseInt(stepParts[1]) : 1;
        
        String[] bounds = stepParts[0].split("-");
        int start = Integer.parseInt(bounds[0]);
        int end = Integer.parseInt(bounds[1]);

        return IntStream.iterate(start, i -> i <= end, i -> i + step);
    }
}

// Made with Bob
