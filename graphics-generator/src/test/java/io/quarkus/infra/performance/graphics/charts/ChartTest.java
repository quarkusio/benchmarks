package io.quarkus.infra.performance.graphics.charts;

import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

import io.quarkus.infra.performance.graphics.PlotDefinition;
import io.quarkus.infra.performance.graphics.SingleSeriesPlotDefinition;
import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.Config;
import io.quarkus.infra.performance.graphics.model.Framework;
import io.quarkus.infra.performance.graphics.model.FrameworkBuild;
import io.quarkus.infra.performance.graphics.model.Group;
import io.quarkus.infra.performance.graphics.model.KnownFramework;
import io.quarkus.infra.performance.graphics.model.Load;
import io.quarkus.infra.performance.graphics.model.Repo;
import io.quarkus.infra.performance.graphics.model.Resources;
import io.quarkus.infra.performance.graphics.model.Result;
import io.quarkus.infra.performance.graphics.model.Results;
import io.quarkus.infra.performance.graphics.model.Rss;
import io.quarkus.infra.performance.graphics.model.units.DimensionalNumber;
import io.quarkus.infra.performance.graphics.model.units.Memory;
import io.quarkus.infra.performance.graphics.model.units.TransactionsPerSecond;
import org.apache.batik.svggen.SVGGraphics2D;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class ChartTest extends ElasticElementTest {

    protected static final Random RANDOM = new Random();

    @Test
    public void testBoundsOnDimensionsForSmallGroup() {
        BenchmarkData data = mockBenchmarkData(2);
        PlotDefinition plotDefinition = createPlotDefinition();

        Chart chart = createChart(plotDefinition, data);

        int preferredWidth = chart.getPreferredHorizontalSize();
        int preferredHeight = chart.getPreferredVerticalSize();

        int minimumWidth = chart.getMinimumHorizontalSize();
        int minimumHeight = chart.getMinimumVerticalSize();

        int maximumWidth = chart.getMaximumHorizontalSize();
        int maximumHeight = chart.getMaximumVerticalSize();

        assertTrue(preferredWidth <= maximumWidth, preferredWidth + " > " + maximumWidth);
        assertTrue(preferredWidth >= minimumWidth, preferredWidth + " > " + minimumWidth);

        assertTrue(preferredHeight <= maximumHeight, preferredHeight + " > " + maximumHeight);
        assertTrue(preferredHeight >= minimumHeight, preferredHeight + " > " + minimumHeight);

    }

    @Test
    public void testBoundsOnDimensionsForLargeGroup() {
        BenchmarkData data = mockBenchmarkData(KnownFramework.values().length);
        PlotDefinition plotDefinition = createPlotDefinition();

        Chart chart = createChart(plotDefinition, data);

        int preferredWidth = chart.getPreferredHorizontalSize();
        int preferredHeight = chart.getPreferredVerticalSize();

        int minimumWidth = chart.getMinimumHorizontalSize();
        int minimumHeight = chart.getMinimumVerticalSize();

        int maximumWidth = chart.getMaximumHorizontalSize();
        int maximumHeight = chart.getMaximumVerticalSize();

        assertTrue(preferredWidth <= maximumWidth, preferredWidth + " > " + maximumWidth);
        assertTrue(preferredWidth >= minimumWidth, preferredWidth + " > " + minimumWidth);

        assertTrue(preferredHeight <= maximumHeight, preferredHeight + " > " + maximumHeight);
        assertTrue(preferredHeight >= minimumHeight, preferredHeight + " > " + minimumHeight);

    }

    @Test
    public void testCanDrawSmallGroupInMinimumDimensions() {
        BenchmarkData data = mockBenchmarkData(3);
        PlotDefinition plotDefinition = createPlotDefinition();

        Chart chart = createChart(plotDefinition, data);

        int minHeight = chart.getMinimumVerticalSize();
        int minWidth = chart.getMinimumHorizontalSize();

        SVGGraphics2D svgGenerator = getSvgGraphics2D(minWidth, minHeight);
        Theme theme = Theme.DARK;
        chart.draw(new Subcanvas(svgGenerator), theme);
    }

    @Test
    public void testCanDrawLargeGroupInMinimumDimensions() {
        BenchmarkData data = mockBenchmarkData(KnownFramework.values().length);
        PlotDefinition plotDefinition = createPlotDefinition();

        Chart chart = createChart(plotDefinition, data);

        int minHeight = chart.getMinimumVerticalSize();
        int minWidth = chart.getMinimumHorizontalSize();

        SVGGraphics2D svgGenerator = getSvgGraphics2D(minWidth, minHeight);
        Theme theme = Theme.DARK;
        chart.draw(new Subcanvas(svgGenerator), theme);
    }

    @Test
    public void testCanDrawSmallGroupInPreferredDimensions() {
        BenchmarkData data = mockBenchmarkData(3);
        PlotDefinition plotDefinition = createPlotDefinition();

        Chart chart = createChart(plotDefinition, data);

        int width = chart.getPreferredHorizontalSize();
        int height = chart.getPreferredVerticalSize();

        SVGGraphics2D svgGenerator = getSvgGraphics2D(width, height);
        Theme theme = Theme.LIGHT;
        chart.draw(new Subcanvas(svgGenerator), theme);
    }

    @Test
    public void testCanDrawLargeGroupInPreferredDimensions() {
        BenchmarkData data = mockBenchmarkData(KnownFramework.values().length);
        PlotDefinition plotDefinition = createPlotDefinition();

        Chart chart = createChart(plotDefinition, data);

        int width = chart.getPreferredHorizontalSize();
        int height = chart.getPreferredVerticalSize();

        SVGGraphics2D svgGenerator = getSvgGraphics2D(width, height);
        Theme theme = Theme.LIGHT;
        chart.draw(new Subcanvas(svgGenerator), theme);
    }

    @Test
    public void testCanDrawSoloGroupInPreferredDimensions() {
        BenchmarkData data = mockBenchmarkData(1);
        PlotDefinition plotDefinition = createPlotDefinition();

        Chart chart = createChart(plotDefinition, data);

        int width = chart.getPreferredHorizontalSize();
        int height = chart.getPreferredVerticalSize();

        SVGGraphics2D svgGenerator = getSvgGraphics2D(width, height);
        Theme theme = Theme.LIGHT;
        chart.draw(new Subcanvas(svgGenerator), theme);
    }

    // This sometimes fails for the cube chart, if the value is less than the column height
    @Test
    public void testCanDrawSoloGroupWithLowValueInPreferredDimensions() {
        BenchmarkData data = mockBenchmarkData(1, () -> 2.0);
        PlotDefinition plotDefinition = createPlotDefinition();

        Chart chart = createChart(plotDefinition, data);

        int width = chart.getPreferredHorizontalSize();
        int height = chart.getPreferredVerticalSize();

        SVGGraphics2D svgGenerator = getSvgGraphics2D(width, height);
        Theme theme = Theme.LIGHT;
        chart.draw(new Subcanvas(svgGenerator), theme);
    }

    @Test
    public void testCanDrawEmptyGroupInPreferredDimensions() {
        BenchmarkData data = mockBenchmarkData(0);
        PlotDefinition plotDefinition = createPlotDefinition();

        Chart chart = createChart(plotDefinition, data);

        int width = chart.getPreferredHorizontalSize();
        int height = chart.getPreferredVerticalSize();

        SVGGraphics2D svgGenerator = getSvgGraphics2D(width, height);
        Theme theme = Theme.LIGHT;
        chart.draw(new Subcanvas(svgGenerator), theme);
    }

    protected PlotDefinition createPlotDefinition() {
        Function<Result, ? extends DimensionalNumber> fun = framework -> framework.load().avThroughput();
        PlotDefinition plotDefinition = new SingleSeriesPlotDefinition("test plot", "some subtitle", fun);
        return plotDefinition;
    }

    private static BenchmarkData mockBenchmarkData() {
        return mockBenchmarkData(4);
    }


    private static BenchmarkData mockBenchmarkData(int count) {
        return mockBenchmarkData(count, () -> (double) RANDOM.nextInt(400));
    }

    private static BenchmarkData mockBenchmarkData(int count, Supplier<Double> value) {
        BenchmarkData data = mock(BenchmarkData.class);
        Results results = new Results();
        when(data.results()).thenReturn(results);
        when(data.group()).thenReturn(Group.ALL);
        for (int i = 0; i < count; i++) {
            addDatapoint(data, KnownFramework.values()[i], value.get());
        }
        addConfig(data);
        return data;
    }

    protected static void addDatapoint(BenchmarkData data, Framework framework, Double throughput) {
        Result result = mock(Result.class);
        data.results().addFramework(framework.getName(), result);
        Load load = mock(Load.class);
        when(result.load()).thenReturn(load);
        when(load.avThroughput()).thenReturn(new TransactionsPerSecond(throughput));
        Rss rss = mock(Rss.class);
        when(result.rss()).thenReturn(rss);
        when(rss.avFirstRequestRss()).thenReturn(new Memory(Math.max(throughput, 1)));
    }

    protected static void addConfig(BenchmarkData data) {
        Config config = mock(Config.class);
        when(data.config()).thenReturn(config);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.quarkus()).thenReturn(new FrameworkBuild("", "3.28.3"));
        when(config.resources()).thenReturn(new Resources(4, null));
    }

    protected static void draw(Chart chart) {
        SVGGraphics2D svgGenerator = getSvgGraphics2D(chart.getPreferredHorizontalSize(), chart.getPreferredVerticalSize());
        Theme theme = Theme.DARK;
        chart.draw(new Subcanvas(svgGenerator), theme);
    }


    protected abstract Chart createChart(PlotDefinition plotDefinition, BenchmarkData data);
}
