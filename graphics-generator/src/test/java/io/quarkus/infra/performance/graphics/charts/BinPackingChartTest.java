package io.quarkus.infra.performance.graphics.charts;

import java.util.function.Function;

import io.quarkus.infra.performance.graphics.BinPackingPlotDefinition;
import io.quarkus.infra.performance.graphics.PlotDefinition;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.Result;
import io.quarkus.infra.performance.graphics.model.units.DimensionalNumber;

public class BinPackingChartTest extends ChartTest {

    @Override
    protected BinPackingChart createChart(PlotDefinition plotDefinition, BenchmarkData data) {
        return new BinPackingChart(plotDefinition, data);
    }

    @Override
    protected PlotDefinition createPlotDefinition() {
        Function<Result, ? extends DimensionalNumber> fun = framework -> framework.rss().avFirstRequestRss();
        return new BinPackingPlotDefinition("test bin packing", "test bin packing", "some subtitle",
                8192, fun);
    }
}
