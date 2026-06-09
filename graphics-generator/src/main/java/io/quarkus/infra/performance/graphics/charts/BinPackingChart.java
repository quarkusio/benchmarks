package io.quarkus.infra.performance.graphics.charts;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import io.quarkus.infra.performance.graphics.BinPackingPlotDefinition;
import io.quarkus.infra.performance.graphics.PlotDefinition;
import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.Config;
import io.quarkus.infra.performance.graphics.model.Resources;

import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.BOLD;
import static java.util.Collections.emptyList;

public class BinPackingChart extends Chart {

    private static final int MINIMUM_GUTTER = 14;
    private static final int MAXIMUM_NATURAL_WIDTH = 2048;

    private static final int DEFAULT_COLUMNS = 4;

    private static final int PLATFORM_HEIGHT_FRACTION = 14;
    private static final int PLATFORM_GAP = 6;

    private final Optional<FinePrint> fineprint;
    private final List<PackedNode> nodes = new ArrayList<>();
    private final List<Datapoint> data;
    private final LabelGroup frameworkLabelGroup = new LabelGroup();
    private final LabelGroup countLabelGroup = new LabelGroup();
    private final LabelGroup infrastructureLabelGroup = new LabelGroup();
    private final int schedulableMemoryMiB;
    private final int columns;

    public BinPackingChart(PlotDefinition plotDefinition, BenchmarkData bmData) {
        this(plotDefinition, bmData, EmbedOptions.DEFAULT);
    }

    public BinPackingChart(PlotDefinition plotDefinition, BenchmarkData bmData, EmbedOptions embedOptions) {
        super(plotDefinition, bmData, embedOptions);

        if (plotDefinition instanceof BinPackingPlotDefinition bpDef) {
            this.schedulableMemoryMiB = bpDef.schedulableMemoryMiB();
            this.data = bmData.results().getDatasets(bpDef.fun());
        } else {
            throw new IllegalArgumentException(
                    "Cannot construct a " + this.getClass().getName()
                            + " with a " + plotDefinition.getClass());
        }

        this.columns = Optional.ofNullable(bmData.config())
                .map(Config::resources)
                .map(Resources::appCpus)
                .filter(cpus -> cpus > 0)
                .orElse(DEFAULT_COLUMNS);

        if (!embedOptions.isEmbedded()) {
            this.fineprint = Optional.of(new FinePrint(bmData,
              "Assumptions: Memory is limiting factor",
              "Node with 8GB RAM, 1 GB K8s overhead"));
            children.add(fineprint.get());
        } else {
            fineprint = Optional.empty();
        }

        for (Datapoint d : data) {
            PackedNode node = new PackedNode(d, schedulableMemoryMiB, columns,
                    frameworkLabelGroup, countLabelGroup, infrastructureLabelGroup);
            nodes.add(node);
        }

        nodes.stream()
                .max(Comparator.comparing(PackedNode::getPreferredVerticalSize))
                .ifPresent(children::add);
    }

    @Override
    public int getMaximumHorizontalSize() {
        return Math.max(
                Math.max(
                        fineprint.map(ElasticElement::getMaximumHorizontalSize).orElse(0),
                        nodes.stream().mapToInt(ElasticElement::getMaximumHorizontalSize).sum()),
                title.getMaximumHorizontalSize()) + 2 * xmargins;
    }

    @Override
    public int getMinimumHorizontalSize() {
        return Math.max(
                Math.max(
                        fineprint.map(ElasticElement::getMinimumHorizontalSize).orElse(0),
                        nodes.stream().mapToInt(ElasticElement::getMinimumHorizontalSize).sum()),
                title.getMinimumHorizontalSize()) + 2 * xmargins;
    }

    @Override
    public int getPreferredHorizontalSize() {
        int numGutters = Math.max(0, nodes.size() - 1);
        int naturalWidth = Math.max(
                Math.max(
                        fineprint.map(ElasticElement::getPreferredHorizontalSize).orElse(0),
                        nodes.stream().mapToInt(ElasticElement::getPreferredHorizontalSize).sum()
                                + numGutters * MINIMUM_GUTTER),
                title.getPreferredHorizontalSize()) + 2 * xmargins;
        if (naturalWidth > MAXIMUM_NATURAL_WIDTH) {
            return Math.max(getMinimumHorizontalSize(), MAXIMUM_NATURAL_WIDTH);
        }
        return naturalWidth;
    }

    @Override
    protected void drawNoCheck(Subcanvas canvasWithMargins, Theme theme) {
        canvasWithMargins.setPaint(theme.text());

        int finePrintHeight = fineprint.map(FinePrint::getPreferredVerticalSize).orElse(0);
        int titleHeight = title.getPreferredVerticalSize();
        int plotHeight = canvasWithMargins.getHeight() - titleHeight - finePrintHeight;

        int minimumPlotHeight = nodes.stream()
                .mapToInt(PackedNode::getMinimumVerticalSize).max().orElse(0);
        if (plotHeight < minimumPlotHeight) {
            int delta = minimumPlotHeight - plotHeight;
            if (fineprint.isPresent()) {
                finePrintHeight -= delta / 2;
                titleHeight -= delta / 2;
            } else {
                titleHeight -= delta;
            }
            plotHeight = canvasWithMargins.getHeight() - titleHeight - finePrintHeight;
        }

        Subcanvas titleCanvas = new Subcanvas(canvasWithMargins,
                canvasWithMargins.getWidth(), titleHeight, 0, 0);
        title.draw(titleCanvas, theme);

        Subcanvas plotArea = new Subcanvas(canvasWithMargins,
                canvasWithMargins.getWidth(), plotHeight, 0, titleHeight);

        int numNodes = nodes.size();
        int gutterSize = MINIMUM_GUTTER;
        int totalGutter = Math.max(0, numNodes - 1) * gutterSize;
        int nodeWidth = numNodes > 0 ? (plotArea.getWidth() - totalGutter) / numNodes : plotArea.getWidth();

        int labelHeight = nodes.isEmpty() ? 0 : nodes.getFirst().getLabelEstimate();
        int boxHeight = plotArea.getHeight() - labelHeight;
        int platformHeight = boxHeight / PLATFORM_HEIGHT_FRACTION;
        boxHeight -= platformHeight + PLATFORM_GAP;

        int nodeAreaHeight = boxHeight / 8;
        int commonFontHeight = Math.min(nodeAreaHeight, platformHeight) * 2 / 3;
        for (PackedNode node : nodes) {
            node.setInfrastructureFontHeight(commonFontHeight);
        }

        computeAndSetHeightScale(boxHeight + labelHeight);

        int x = 0;
        for (PackedNode node : nodes) {
            Subcanvas labelArea = new Subcanvas(plotArea, nodeWidth, labelHeight, x, 0);
            node.drawLabels(labelArea, theme);
            x += nodeWidth + gutterSize;
        }

        int boxY = labelHeight;
        x = 0;
        for (PackedNode node : nodes) {
            Subcanvas boxArea = new Subcanvas(plotArea, nodeWidth, boxHeight, x, boxY);
            node.drawBox(boxArea, theme);
            x += nodeWidth + gutterSize;
        }

        if (!nodes.isEmpty()) {
            int platformY = boxY + boxHeight + PLATFORM_GAP;
            int totalNodesWidth = numNodes * nodeWidth + (numNodes - 1) * gutterSize;
            int platformX = PackedNode.CONTAINER_INSET;
            int platformWidth = totalNodesWidth - 2 * PackedNode.CONTAINER_INSET;

            int platformShadowOffset = 3;
            Subcanvas platformCanvas = new Subcanvas(plotArea,
                    platformWidth + platformShadowOffset, platformHeight + platformShadowOffset,
                    platformX, platformY);

            int platformArc = 6;
            Color shadowColor = PackedNode.blendColors(theme.text(), theme.background(), 0.75);
            platformCanvas.setPaint(shadowColor);
            platformCanvas.fillRoundRect(platformShadowOffset, platformShadowOffset,
                    platformWidth, platformHeight, platformArc, platformArc);

            platformCanvas.setPaint(theme.divider());
            platformCanvas.fillRoundRect(0, 0, platformWidth, platformHeight, platformArc, platformArc);

            platformCanvas.setPaint(theme.background());
            Label platformLabel = new Label("container platform", infrastructureLabelGroup)
                    .setHorizontalAlignment(io.quarkus.infra.performance.graphics.charts.fonts.Alignment.CENTER)
                    .setVerticalAlignment(io.quarkus.infra.performance.graphics.charts.fonts.VAlignment.MIDDLE)
                    .setStyle(BOLD)
                    .setTargetHeight(commonFontHeight);
            platformLabel.draw(platformCanvas, platformWidth / 2, platformHeight / 2);
        }

        drawFinePrint(canvasWithMargins, theme, finePrintHeight,
                titleHeight + plotHeight, fineprint);
    }

    private void computeAndSetHeightScale(int boxPlusLabelHeight) {
        int labelHeight = nodes.stream()
                .mapToInt(PackedNode::getLabelEstimate)
                .max().orElse(0);
        int boxHeight = Math.max(1, boxPlusLabelHeight - labelHeight);
        int containerHeight = boxHeight - boxHeight / 8;

        double heightScale = Double.MAX_VALUE;
        for (PackedNode node : nodes) {
            int rows = node.getRowsPerColumn();
            if (rows > 0 && node.getInstanceMemory() > 0) {
                int available = containerHeight - 2 * PackedNode.CONTAINER_INSET
                        - Math.max(0, rows - 1) * PackedNode.BLOCK_PADDING;
                double scale = (double) available / (rows * node.getInstanceMemory());
                heightScale = Math.min(heightScale, scale);
            }
        }

        if (heightScale == Double.MAX_VALUE) {
            heightScale = 1.0;
        }

        for (PackedNode node : nodes) {
            node.setHeightScale(heightScale);
        }
    }

    @Override
    public Collection<InlinedSVG> getInlinedSVGs() {
        return fineprint.map(FinePrint::getInlinedSVGs).orElse(emptyList());
    }
}
