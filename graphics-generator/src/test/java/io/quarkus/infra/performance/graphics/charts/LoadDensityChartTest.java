package io.quarkus.infra.performance.graphics.charts;

import java.util.function.Function;

import io.quarkus.infra.performance.graphics.LoadDensityPlotDefinition;
import io.quarkus.infra.performance.graphics.PlotDefinition;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.Result;
import io.quarkus.infra.performance.graphics.model.units.DimensionalNumber;

public class LoadDensityChartTest extends ChartTest {

    @Override
    protected LoadDensityChart createChart(PlotDefinition plotDefinition, BenchmarkData data) {
        return new LoadDensityChart(plotDefinition, data);
    }

    @Override
    protected PlotDefinition createPlotDefinition() {
        Function<Result, ? extends DimensionalNumber> throughputFun = framework -> framework.load().avThroughput();
        Function<Result, ? extends DimensionalNumber> rssFun = framework -> framework.rss().avFirstRequestRss();
        return new LoadDensityPlotDefinition("test load density", "test-load-density", "some subtitle",
                7168, 200_000, throughputFun, rssFun);
    }
}
