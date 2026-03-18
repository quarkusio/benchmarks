package io.quarkus.infra.performance.graphics.model;

import java.util.List;

import io.quarkus.infra.performance.graphics.model.units.Memory;
import io.quarkus.infra.performance.graphics.model.units.TransactionsPerMiB;
import io.quarkus.infra.performance.graphics.model.units.TransactionsPerSecond;

public record Load(
        List<TransactionsPerSecond> throughput,
        List<Memory> rss,
        List<TransactionsPerMiB> throughputDensity,
        TransactionsPerSecond avThroughput,
        Memory avMaxRss,
        TransactionsPerMiB maxThroughputDensity) {
}
