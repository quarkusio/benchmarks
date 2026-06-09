package io.quarkus.infra.performance.graphics;

import java.util.function.Function;

import io.quarkus.infra.performance.graphics.model.Result;
import io.quarkus.infra.performance.graphics.model.units.DimensionalNumber;

public record BinPackingPlotDefinition(String title, String filename, String subtitle,
                                       int schedulableMemoryMiB,
                                       Function<Result, ? extends DimensionalNumber> fun) implements PlotDefinition {
}
