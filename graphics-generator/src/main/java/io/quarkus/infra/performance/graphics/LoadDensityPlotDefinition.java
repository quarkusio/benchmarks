package io.quarkus.infra.performance.graphics;

import java.util.function.Function;

import io.quarkus.infra.performance.graphics.model.Result;
import io.quarkus.infra.performance.graphics.model.units.DimensionalNumber;

public record LoadDensityPlotDefinition(String title, String filename, String subtitle,
                                        int schedulableMemoryMiB,
                                        double maxLoadTps,
                                        Function<Result, ? extends DimensionalNumber> throughputFun,
                                        Function<Result, ? extends DimensionalNumber> rssFun) implements PlotDefinition {
}
