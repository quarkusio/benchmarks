package org.acme.model;

import java.util.List;

public record Startup(
        List<Double> timings,
        Double avStartTime
) {
}