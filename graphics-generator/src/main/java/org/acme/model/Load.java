package org.acme.model;

import java.util.List;

public record Load(
        List<Double> throughput,
        List<Double> rss,
        List<Double> throughputDensity,
        Double avThroughput,
        Double avMaxRss,
        Double maxThroughputDensity) {
}
