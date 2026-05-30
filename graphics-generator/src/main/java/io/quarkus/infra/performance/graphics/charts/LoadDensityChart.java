package io.quarkus.infra.performance.graphics.charts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Polygon;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import io.quarkus.infra.performance.graphics.LoadDensityPlotDefinition;
import io.quarkus.infra.performance.graphics.PlotDefinition;
import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.charts.fonts.Alignment;
import io.quarkus.infra.performance.graphics.charts.fonts.FontStyle;
import io.quarkus.infra.performance.graphics.charts.fonts.Sizer;
import io.quarkus.infra.performance.graphics.charts.fonts.VAlignment;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.Framework;

import static java.util.Collections.emptyList;

public class LoadDensityChart extends Chart {

    private static final int MINIMUM_PLOT_WIDTH = 300;
    private static final int MAXIMUM_NATURAL_WIDTH = 1200;
    private static final int MINIMUM_PLOT_HEIGHT = 200;
    private static final int AXIS_LABEL_FONT_SIZE = 12;
    private static final int TICK_LENGTH = 5;
    private static final int LINE_THICKNESS = 3;
    private static final int LEGEND_LINE_LENGTH = 24;
    private static final int LEGEND_PADDING = 10;
    private static final int LEGEND_ENTRY_GAP = 20;
    private static final int Y_AXIS_LABEL_WIDTH = 40;
    private static final int X_AXIS_LABEL_HEIGHT = 44;
    private static final int KEY_PADDING_LEFT = 8;
    private static final int KEY_PADDING_RIGHT = 14;

    private final Optional<FinePrint> fineprint;
    private final List<FrameworkStepData> frameworkSteps = new ArrayList<>();
    private final int maxInstances;
    private final double maxLoad;

    public LoadDensityChart(PlotDefinition plotDefinition, BenchmarkData bmData) {
        this(plotDefinition, bmData, EmbedOptions.DEFAULT);
    }

    public LoadDensityChart(PlotDefinition plotDefinition, BenchmarkData bmData, EmbedOptions embedOptions) {
        super(plotDefinition, bmData, embedOptions);

        if (!(plotDefinition instanceof LoadDensityPlotDefinition ldDef)) {
            throw new IllegalArgumentException(
                    "Cannot construct a " + this.getClass().getName()
                            + " with a " + plotDefinition.getClass());
        }

        List<Datapoint> throughputData = bmData.results().getDatasets(ldDef.throughputFun());

        double minThroughput = Double.MAX_VALUE;

        for (int i = 0; i < throughputData.size(); i++) {
            Datapoint tp = throughputData.get(i);

            double throughput = tp.value().getValue();

            frameworkSteps.add(new FrameworkStepData(tp.framework(), throughput));

            minThroughput = Math.min(minThroughput, throughput);
        }

        this.maxLoad = ldDef.maxLoadTps();
        this.maxInstances = minThroughput > 0 ? (int) Math.ceil(maxLoad / minThroughput) : 1;

        if (!embedOptions.isEmbedded()) {
            this.fineprint = Optional.of(new FinePrint(bmData,
                    "Assumptions: Throughput is the limiting factor",
                    "Instances = ceil(load / max throughput)"));
            children.add(fineprint.get());
        } else {
            fineprint = Optional.empty();
        }
    }

    @Override
    public int getMaximumHorizontalSize() {
        return Math.max(
                fineprint.map(ElasticElement::getMaximumHorizontalSize).orElse(0),
                MAXIMUM_NATURAL_WIDTH) + 2 * xmargins;
    }

    @Override
    public int getMinimumHorizontalSize() {
        return Math.max(
                fineprint.map(ElasticElement::getMinimumHorizontalSize).orElse(0),
                MINIMUM_PLOT_WIDTH) + 2 * xmargins;
    }

    @Override
    public int getPreferredHorizontalSize() {
        return Math.max(getMinimumHorizontalSize(), MAXIMUM_NATURAL_WIDTH + 2 * xmargins);
    }

    @Override
    public int getMinimumVerticalSize() {
        return title.getMinimumVerticalSize()
                + MINIMUM_PLOT_HEIGHT
                + fineprint.map(ElasticElement::getMinimumVerticalSize).orElse(0)
                + 2 * ymargins;
    }

    @Override
    public int getPreferredVerticalSize() {
        return title.getPreferredVerticalSize()
                + MINIMUM_PLOT_HEIGHT * 2
                + fineprint.map(ElasticElement::getPreferredVerticalSize).orElse(0)
                + 2 * ymargins;
    }

    @Override
    public int getMaximumVerticalSize() {
        return title.getMaximumVerticalSize()
                + MINIMUM_PLOT_HEIGHT * 3
                + fineprint.map(ElasticElement::getMaximumVerticalSize).orElse(0)
                + 2 * ymargins;
    }

    @Override
    protected void drawNoCheck(Subcanvas canvasWithMargins, Theme theme) {
        canvasWithMargins.setPaint(theme.text());

        int finePrintHeight = fineprint.map(FinePrint::getPreferredVerticalSize).orElse(0);
        int titleHeight = title.getPreferredVerticalSize();

        int availablePlotHeight = canvasWithMargins.getHeight() - titleHeight - finePrintHeight;
        if (availablePlotHeight < MINIMUM_PLOT_HEIGHT) {
            int delta = MINIMUM_PLOT_HEIGHT - availablePlotHeight;
            if (fineprint.isPresent()) {
                finePrintHeight -= delta / 2;
                titleHeight -= delta / 2;
            } else {
                titleHeight -= delta;
            }
        }

        int plotHeight = canvasWithMargins.getHeight() - titleHeight - finePrintHeight;

        Subcanvas titleCanvas = new Subcanvas(canvasWithMargins,
                canvasWithMargins.getWidth(), titleHeight, 0, 0);
        title.draw(titleCanvas, theme);

        Subcanvas plotArea = new Subcanvas(canvasWithMargins,
                canvasWithMargins.getWidth(), plotHeight, 0, titleHeight);

        if (!frameworkSteps.isEmpty()) {
            drawStepChart(plotArea, theme);
        }

        drawFinePrint(canvasWithMargins, theme, finePrintHeight,
                titleHeight + plotHeight, fineprint);
    }

    private void drawStepChart(Subcanvas plotArea, Theme theme) {
        int legendHeight = estimateLegendHeight();

        int chartLeft = Y_AXIS_LABEL_WIDTH;
        int chartBottom = plotArea.getHeight() - X_AXIS_LABEL_HEIGHT - legendHeight;
        int chartTop = 10;
        int chartRight = plotArea.getWidth() - 20;

        int chartWidth = chartRight - chartLeft;
        int chartHeight = chartBottom - chartTop;

        if (chartWidth <= 0 || chartHeight <= 0) {
            return;
        }

        drawAxes(plotArea, theme, chartLeft, chartTop, chartBottom, chartRight, chartWidth, chartHeight);
        drawStepLines(plotArea, theme, chartLeft, chartTop, chartBottom, chartWidth, chartHeight);
        drawLegend(plotArea, theme, chartLeft, chartBottom + X_AXIS_LABEL_HEIGHT, chartWidth);
    }

    private void drawAxes(Subcanvas g, Theme theme, int chartLeft, int chartTop, int chartBottom,
                           int chartRight, int chartWidth, int chartHeight) {
        g.setPaint(theme.text());

        g.drawLine(chartLeft, chartTop, chartLeft, chartBottom, 2);
        g.drawLine(chartLeft, chartBottom, chartRight, chartBottom, 2);

        Label originLabel = new Label("0")
                .setTargetHeight(Math.min(AXIS_LABEL_FONT_SIZE, chartHeight / 2))
                .setHorizontalAlignment(Alignment.RIGHT)
                .setVerticalAlignment(VAlignment.MIDDLE);
        originLabel.draw(g, chartLeft - TICK_LENGTH - 4, chartBottom);

        int yStep = (int) Math.max(1, niceStep(maxInstances, 8));
        for (int i = yStep; i <= maxInstances; i += yStep) {
            int y = chartBottom - (int) ((double) i / maxInstances * chartHeight);
            g.drawLine(chartLeft - TICK_LENGTH, y, chartLeft, y);

            g.setPaint(theme.divider());
            g.drawLine(chartLeft + 1, y, chartRight, y);
            g.setPaint(theme.text());

            int tickCount = maxInstances / yStep;
            Label tickLabel = new Label(String.valueOf(i))
                    .setTargetHeight(Math.min(AXIS_LABEL_FONT_SIZE, chartHeight / (tickCount + 1)))
                    .setHorizontalAlignment(Alignment.RIGHT)
                    .setVerticalAlignment(VAlignment.MIDDLE);
            tickLabel.draw(g, chartLeft - TICK_LENGTH - 4, y);
        }

        int xTickCount = Math.max(2, Math.min(10, (int) (maxLoad / niceStep(maxLoad, 10))));
        double xStep = niceStep(maxLoad, xTickCount);
        for (double load = xStep; load <= maxLoad; load += xStep) {
            int x = chartLeft + (int) (load / maxLoad * chartWidth);
            g.drawLine(x, chartBottom, x, chartBottom + TICK_LENGTH);

            String labelText = formatLoad(load);
            Label tickLabel = new Label(labelText)
                    .setTargetHeight(Math.min(AXIS_LABEL_FONT_SIZE, X_AXIS_LABEL_HEIGHT * 2 / 3))
                    .setHorizontalAlignment(Alignment.CENTER)
                    .setVerticalAlignment(VAlignment.TOP);
            tickLabel.draw(g, x, chartBottom + TICK_LENGTH + 2);
        }

        int yAxisLabelHeight = Math.min(AXIS_LABEL_FONT_SIZE, Y_AXIS_LABEL_WIDTH * 2 / 3);
        Label yAxisLabel = new Label("Instances")
                .setTargetHeight(yAxisLabelHeight)
                .setHorizontalAlignment(Alignment.CENTER)
                .setVerticalAlignment(VAlignment.BOTTOM);
        yAxisLabel.draw(g, chartLeft / 2, chartTop - 2);

        Label xAxisLabel = new Label("Expected load (tps)")
                .setTargetHeight(Math.min(AXIS_LABEL_FONT_SIZE, X_AXIS_LABEL_HEIGHT * 2 / 3))
                .setHorizontalAlignment(Alignment.CENTER)
                .setVerticalAlignment(VAlignment.TOP);
        xAxisLabel.draw(g, chartLeft + chartWidth / 2, chartBottom + TICK_LENGTH + 14);
    }

    private void drawStepLines(Subcanvas g, Theme theme, int chartLeft, int chartTop,
                                int chartBottom, int chartWidth, int chartHeight) {
        List<FrameworkStepData> sortedByThroughput = new ArrayList<>(frameworkSteps);
        sortedByThroughput.sort(Comparator.comparingDouble(s -> s.throughput));

        for (FrameworkStepData step : sortedByThroughput) {
            Color fillColor = theme.fillElements().getOrDefault(step.framework, theme.divider());
            fillStepArea(g, fillColor, step, chartLeft, chartBottom, chartWidth, chartHeight);
        }

        Color lineColor = theme.background().getRed() < 128 ? Color.WHITE : theme.text();
        for (FrameworkStepData step : frameworkSteps) {
            drawStepLine(g, lineColor, step, chartLeft, chartBottom, chartWidth, chartHeight);
        }

        labelAreas(g, theme, sortedByThroughput, chartLeft, chartBottom, chartWidth, chartHeight);
    }

    private void fillStepArea(Subcanvas g, Color fillColor, FrameworkStepData step,
                               int chartLeft, int chartBottom, int chartWidth, int chartHeight) {
        double throughput = step.throughput;
        int instancesAtMaxLoad = throughput > 0 ? (int) Math.ceil(maxLoad / throughput) : 1;

        Polygon poly = new Polygon();
        poly.addPoint(chartLeft + g.getXOffset(), chartBottom + g.getYOffset());

        for (int n = 1; n <= instancesAtMaxLoad; n++) {
            double loadStart = (n - 1) * throughput;
            double loadEnd = n * throughput;

            int x1 = chartLeft + (int) (loadStart / maxLoad * chartWidth);
            int x2 = chartLeft + (int) (Math.min(loadEnd, maxLoad) / maxLoad * chartWidth);
            int y = chartBottom - (int) ((double) n / maxInstances * chartHeight);

            poly.addPoint(x1 + g.getXOffset(), y + g.getYOffset());
            poly.addPoint(x2 + g.getXOffset(), y + g.getYOffset());
        }

        int lastX = poly.xpoints[poly.npoints - 1];
        poly.addPoint(lastX, chartBottom + g.getYOffset());

        g.setPaint(fillColor);
        g.getGraphics().fillPolygon(poly);
    }

    private void drawStepLine(Subcanvas g, Color color, FrameworkStepData step,
                               int chartLeft, int chartBottom, int chartWidth, int chartHeight) {
        g.setPaint(color);

        Stroke oldStroke = g.getGraphics().getStroke();
        g.getGraphics().setStroke(new BasicStroke(LINE_THICKNESS, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));

        double throughput = step.throughput;
        int instancesAtMaxLoad = throughput > 0 ? (int) Math.ceil(maxLoad / throughput) : 1;

        int prevX = chartLeft;
        int prevY = chartBottom;

        for (int n = 1; n <= instancesAtMaxLoad; n++) {
            double loadStart = (n - 1) * throughput;
            double loadEnd = n * throughput;

            int x1 = chartLeft + (int) (loadStart / maxLoad * chartWidth);
            int x2 = chartLeft + (int) (Math.min(loadEnd, maxLoad) / maxLoad * chartWidth);
            int y = chartBottom - (int) ((double) n / maxInstances * chartHeight);

            if (n > 1) {
                g.drawLine(prevX, prevY, prevX, y);
            }

            g.drawLine(x1 == prevX && n > 1 ? prevX : x1, y, x2, y);

            prevX = x2;
            prevY = y;
        }

        g.getGraphics().setStroke(oldStroke);
    }

    private void labelAreas(Subcanvas g, Theme theme, List<FrameworkStepData> sortedByThroughput,
                             int chartLeft, int chartBottom, int chartWidth, int chartHeight) {
        int labelFontSize = Math.min(AXIS_LABEL_FONT_SIZE, 11);
        int textHeight = Sizer.calculateHeight(labelFontSize);
        int labelRight = chartLeft + chartWidth - 20;

        record BandInfo(FrameworkStepData step, String name, int yTop, int yBottom, boolean fits) {}

        List<BandInfo> bands = new ArrayList<>();
        List<FrameworkStepData> needsKey = new ArrayList<>();

        for (int i = 0; i < sortedByThroughput.size(); i++) {
            FrameworkStepData step = sortedByThroughput.get(i);
            String name = step.framework.getExpandedName().replace("\n", " ");
            int textWidth = Sizer.calculateWidth(name, labelFontSize, FontStyle.BOLD);

            double labelLeftLoad = maxLoad * (labelRight - textWidth - chartLeft) / (double) chartWidth;

            int instancesAtLeft = step.throughput > 0 ? (int) Math.ceil(labelLeftLoad / step.throughput) : 1;
            int yTop = chartBottom - (int) ((double) instancesAtLeft / maxInstances * chartHeight);

            int instancesAtRight = step.throughput > 0 ? (int) Math.ceil(maxLoad / step.throughput) : 1;

            int yBottom;
            if (i < sortedByThroughput.size() - 1) {
                FrameworkStepData nextBetter = sortedByThroughput.get(i + 1);
                int nextInstances = nextBetter.throughput > 0 ? (int) Math.ceil(maxLoad / nextBetter.throughput) : 1;
                yBottom = chartBottom - (int) ((double) nextInstances / maxInstances * chartHeight);
            } else {
                yBottom = chartBottom;
            }

            int availableHeight = yBottom - yTop;
            boolean fits = (availableHeight - textHeight >= 4) && (textWidth < labelRight - chartLeft - 20);

            bands.add(new BandInfo(step, name, yTop, yBottom, fits));
            if (!fits) {
                needsKey.add(step);
            }
        }

        int keyTop = Integer.MAX_VALUE;
        if (!needsKey.isEmpty()) {
            keyTop = computeKeyTop(needsKey, chartLeft, chartBottom, chartWidth);
        }

        for (BandInfo band : bands) {
            if (band.fits) {
                int labelY = (band.yTop + band.yBottom) / 2;
                g.setPaint(Color.BLACK);
                Label areaLabel = new Label(band.name)
                        .setTargetHeight(textHeight)
                        .setHorizontalAlignment(Alignment.RIGHT)
                        .setVerticalAlignment(VAlignment.MIDDLE)
                        .setStyle(FontStyle.BOLD);
                areaLabel.draw(g, labelRight, labelY);
            }
        }

        if (!needsKey.isEmpty()) {
            drawKey(g, theme, needsKey, chartLeft, chartBottom, chartWidth);
        }
    }

    private int computeKeyTop(List<FrameworkStepData> entries, int chartLeft, int chartBottom, int chartWidth) {
        return 10;
    }

    private int estimateKeyWidth(List<FrameworkStepData> entries) {
        if (entries.isEmpty()) return 0;
        int keyFontSize = Math.min(AXIS_LABEL_FONT_SIZE, 11);
        int entryHeight = Sizer.calculateHeight(keyFontSize);
        int swatchSize = entryHeight - 2;
        int maxW = 0;
        for (FrameworkStepData step : entries) {
            String name = step.framework.getExpandedName().replace("\n", " ");
            int w = swatchSize + 6 + Sizer.calculateWidth(name, keyFontSize, FontStyle.PLAIN);
            maxW = Math.max(maxW, w);
        }
        return maxW + KEY_PADDING_LEFT + KEY_PADDING_RIGHT;
    }

    private void drawKey(Subcanvas g, Theme theme, List<FrameworkStepData> entries,
                          int chartLeft, int chartBottom, int chartWidth) {
        int keyFontSize = Math.min(AXIS_LABEL_FONT_SIZE, 11);
        int entryHeight = Sizer.calculateHeight(keyFontSize);
        int swatchSize = entryHeight - 2;
        int keyVerticalPadding = 8;
        int entryGap = 4;

        int keyHeight = entries.size() * (entryHeight + entryGap) + 2 * keyVerticalPadding;
        int keyWidth = 0;
        for (FrameworkStepData step : entries) {
            String name = step.framework.getExpandedName().replace("\n", " ");
            int w = swatchSize + 6 + Sizer.calculateWidth(name, keyFontSize, FontStyle.PLAIN);
            keyWidth = Math.max(keyWidth, w);
        }
        keyWidth += KEY_PADDING_LEFT + KEY_PADDING_RIGHT;

        int keyX = chartLeft + 10;
        int keyY = 10;

        int arc = 8;
        g.setPaint(new Color(theme.background().getRed(), theme.background().getGreen(),
                theme.background().getBlue(), 220));
        g.fillRoundRect(keyX, keyY, keyWidth, keyHeight, arc, arc);
        g.setPaint(theme.divider());
        g.drawRoundRect(keyX, keyY, keyWidth, keyHeight, arc, arc);

        int y = keyY + keyVerticalPadding + entryHeight / 2;
        for (FrameworkStepData step : entries) {
            Color fillColor = theme.fillElements().getOrDefault(step.framework, theme.divider());
            g.setPaint(fillColor);
            g.fillRect(keyX + KEY_PADDING_LEFT, y - swatchSize / 2, swatchSize, swatchSize);

            g.setPaint(theme.text());
            String name = step.framework.getExpandedName().replace("\n", " ");
            Label label = new Label(name)
                    .setTargetHeight(entryHeight)
                    .setHorizontalAlignment(Alignment.LEFT)
                    .setVerticalAlignment(VAlignment.MIDDLE);
            label.draw(g, keyX + KEY_PADDING_LEFT + swatchSize + 6, y);

            y += entryHeight + entryGap;
        }
    }

    private void drawLegend(Subcanvas plotArea, Theme theme, int chartLeft, int legendY,
                             int chartWidth) {
    }

    private int estimateLegendHeight() {
        return 0;
    }

    private static double niceStep(double range, int targetTicks) {
        double roughStep = range / targetTicks;
        double magnitude = Math.pow(10, Math.floor(Math.log10(roughStep)));
        double residual = roughStep / magnitude;

        double niceResidual;
        if (residual <= 1.5) {
            niceResidual = 1;
        } else if (residual <= 3.5) {
            niceResidual = 2;
        } else if (residual <= 7.5) {
            niceResidual = 5;
        } else {
            niceResidual = 10;
        }

        return niceResidual * magnitude;
    }

    private static String formatLoad(double load) {
        if (load >= 1000) {
            double k = load / 1000;
            if (k == (int) k) {
                return (int) k + "k";
            }
            return String.format("%.1fk", k);
        }
        if (load == (int) load) {
            return String.valueOf((int) load);
        }
        return String.format("%.0f", load);
    }

    @Override
    public Collection<InlinedSVG> getInlinedSVGs() {
        return fineprint.map(FinePrint::getInlinedSVGs).orElse(emptyList());
    }

    private record FrameworkStepData(Framework framework, double throughput) {
    }

    private record LegendEntry(String name, Framework framework, int width) {
    }
}
