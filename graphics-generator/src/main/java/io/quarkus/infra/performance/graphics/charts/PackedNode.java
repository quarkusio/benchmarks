package io.quarkus.infra.performance.graphics.charts;

import java.awt.Color;

import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.charts.fonts.Alignment;
import io.quarkus.infra.performance.graphics.charts.fonts.VAlignment;

import static io.quarkus.infra.performance.graphics.charts.Bar.BAR_THICKNESS;
import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.BOLD;
import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.PLAIN;

public class PackedNode implements ElasticElement {

    static final int BLOCK_PADDING = 2;
    static final int CONTAINER_INSET = 3;
    private static final int FRAMEWORK_LABEL_TOP_PADDING = 2;
    private static final int FRAMEWORK_LABEL_BOTTOM_PADDING = 10;
    private static final int MINIMUM_FONT_SIZE = 8;
    private static final int MAXIMUM_FONT_SIZE = 24;
    private static final int MINIMUM_CONTAINER_HEIGHT = 120;
    private static final int MAXIMUM_CONTAINER_HEIGHT = 500;
    private static final int NODE_FRACTION = 8;
    private static final int SHADOW_OFFSET = 3;
    private static final int BLOCK_ARC = 4;

    private final Datapoint d;
    private final int schedulableMemoryMiB;
    private final int instanceCount;
    private final int columns;
    private final Label frameworkLabel;
    private final Label countLabel;
    private final LabelGroup infrastructureLabelGroup;
    private double heightScale;
    private int infrastructureFontHeight;

    public PackedNode(Datapoint d, int schedulableMemoryMiB, int columns,
                      LabelGroup frameworkLabelGroup, LabelGroup countLabelGroup,
                      LabelGroup infrastructureLabelGroup) {
        this.d = d;
        this.schedulableMemoryMiB = schedulableMemoryMiB;
        this.columns = Math.max(1, columns);
        this.infrastructureLabelGroup = infrastructureLabelGroup;
        double instanceMemory = d.value().getValue();
        this.instanceCount = instanceMemory > 0 ? (int) (schedulableMemoryMiB / instanceMemory) : 0;

        frameworkLabel = new Label(d.framework().getExpandedName(), frameworkLabelGroup)
                .setHorizontalAlignment(Alignment.CENTER)
                .setVerticalAlignment(VAlignment.TOP)
                .setStyles(new io.quarkus.infra.performance.graphics.charts.fonts.FontStyle[]{BOLD, PLAIN})
                .setTargetHeight(BAR_THICKNESS);

        countLabel = new Label(instanceCount + " instances", countLabelGroup)
                .setHorizontalAlignment(Alignment.CENTER)
                .setVerticalAlignment(VAlignment.TOP)
                .setStyle(BOLD)
                .setTargetHeight(BAR_THICKNESS * 2 / 3);
    }

    public void setHeightScale(double heightScale) {
        this.heightScale = heightScale;
    }

    public double getHeightScale() {
        return heightScale;
    }

    public void setInfrastructureFontHeight(int fontHeight) {
        this.infrastructureFontHeight = fontHeight;
    }

    public double getInstanceMemory() {
        return d.value().getValue();
    }

    public int getRowsPerColumn() {
        return instanceCount > 0 ? (int) Math.ceil((double) instanceCount / columns) : 0;
    }

    public int getBlockHeight() {
        return Math.max(1, (int) Math.round(getInstanceMemory() * heightScale));
    }

    public int getBlockWidth(int containerWidth) {
        return (containerWidth - 2 * CONTAINER_INSET - (columns - 1) * BLOCK_PADDING) / columns;
    }

    @Override
    public int getMinimumHorizontalSize() {
        double widestLabel = Math.max(
                countLabel.calculateWidth(MINIMUM_FONT_SIZE),
                frameworkLabel.calculateWidth(MINIMUM_FONT_SIZE));
        return (int) Math.max(widestLabel, 60);
    }

    @Override
    public int getMaximumHorizontalSize() {
        return 400;
    }

    @Override
    public int getMinimumVerticalSize() {
        return MINIMUM_CONTAINER_HEIGHT + getLabelEstimate();
    }

    @Override
    public int getMaximumVerticalSize() {
        return MAXIMUM_CONTAINER_HEIGHT + getLabelEstimate();
    }

    public int getTextWidth() {
        return Math.max(countLabel.calculateWidth(), frameworkLabel.calculateWidth());
    }

    public int getInstanceCount() {
        return instanceCount;
    }

    int getLabelEstimate() {
        return frameworkLabel.getTargetHeight() + countLabel.getTargetHeight()
                + FRAMEWORK_LABEL_TOP_PADDING + FRAMEWORK_LABEL_BOTTOM_PADDING;
    }

    int getNodeAreaHeight(int totalBoxHeight) {
        return totalBoxHeight / NODE_FRACTION;
    }

    /**
     * Draw just the box (backing rectangle, instance blocks, "Machine" section).
     * The dataArea height should be the box height only, not including labels.
     */
    public void drawBox(Subcanvas dataArea, Theme theme) {
        int totalBoxHeight = dataArea.getHeight();
        int nodeAreaHeight = getNodeAreaHeight(totalBoxHeight);
        int instanceAreaHeight = totalBoxHeight - nodeAreaHeight;

        int containerWidth = dataArea.getWidth() - 2 * CONTAINER_INSET;
        int cx = CONTAINER_INSET;
        int arc = 10;

        Color shadowColor = blendColors(theme.text(), theme.background(), 0.75);
        dataArea.setPaint(shadowColor);
        dataArea.fillRoundRect(cx + SHADOW_OFFSET, SHADOW_OFFSET, containerWidth, totalBoxHeight, arc, arc);

        Color containerBg = blendColors(theme.background(), theme.divider(), 0.2);
        dataArea.setPaint(containerBg);
        dataArea.fillRoundRect(cx, 0, containerWidth, totalBoxHeight, arc, arc);

        dataArea.setPaint(theme.divider());
        dataArea.drawRoundRect(cx, 0, containerWidth, totalBoxHeight, arc, arc);

        int outline = 1;
        Subcanvas instanceCanvas = new Subcanvas(dataArea, containerWidth - 2 * outline, instanceAreaHeight - outline, cx + outline, outline);
        drawInstances(instanceCanvas, theme);

        Subcanvas nodeCanvas = new Subcanvas(dataArea, containerWidth - 2 * outline, nodeAreaHeight - outline, cx + outline, instanceAreaHeight);
        drawNodeLabel(nodeCanvas, theme);
    }

    private void drawInstances(Subcanvas canvas, Theme theme) {
        int margin = 1;
        int availableWidth = canvas.getWidth() - 2 * margin;
        int blockWidth = (availableWidth - (columns - 1) * BLOCK_PADDING) / columns;
        int blockHeight = getBlockHeight();
        int rowsPerColumn = getRowsPerColumn();

        int totalBlocksWidth = columns * blockWidth + (columns - 1) * BLOCK_PADDING;
        int xOffset = (canvas.getWidth() - totalBlocksWidth) / 2;

        canvas.setPaint(theme.chartElements().get(d.framework()));

        int drawn = 0;
        for (int row = 0; row < rowsPerColumn && drawn < instanceCount; row++) {
            for (int col = 0; col < columns && drawn < instanceCount; col++) {
                int bx = xOffset + col * (blockWidth + BLOCK_PADDING);
                int by = canvas.getHeight() - (row + 1) * (blockHeight + BLOCK_PADDING);
                if (by < 0) {
                    break;
                }
                canvas.fillRoundRect(bx, by, blockWidth, blockHeight, BLOCK_ARC, BLOCK_ARC);
                drawn++;
            }
        }
    }

    private void drawNodeLabel(Subcanvas canvas, Theme theme) {
        canvas.setPaint(theme.text());
        Label nodeLabel = new Label("machine", infrastructureLabelGroup)
                .setHorizontalAlignment(Alignment.CENTER)
                .setVerticalAlignment(VAlignment.MIDDLE)
                .setStyle(PLAIN)
                .setTargetHeight(infrastructureFontHeight);
        nodeLabel.draw(canvas, canvas.getWidth() / 2, canvas.getHeight() / 2);
    }

    /**
     * Draw just the framework name and count labels.
     */
    public void drawLabels(Subcanvas dataArea, Theme theme) {
        dataArea.setPaint(theme.text());
        frameworkLabel.draw(dataArea, dataArea.getWidth() / 2, FRAMEWORK_LABEL_TOP_PADDING);
        countLabel.draw(dataArea, dataArea.getWidth() / 2,
                FRAMEWORK_LABEL_TOP_PADDING + frameworkLabel.getActualHeight());
    }

    @Override
    public void draw(Subcanvas dataArea, Theme theme) {
        int labelHeight = getLabelEstimate();
        int boxHeight = dataArea.getHeight() - labelHeight;

        Subcanvas labelArea = new Subcanvas(dataArea, dataArea.getWidth(), labelHeight, 0, 0);
        drawLabels(labelArea, theme);

        Subcanvas boxArea = new Subcanvas(dataArea, dataArea.getWidth(), boxHeight, 0, labelHeight);
        drawBox(boxArea, theme);
    }

    static Color blendColors(Color c1, Color c2, double ratio) {
        int r = Math.clamp(Math.round(c1.getRed() * (1 - ratio) + c2.getRed() * ratio), 0, 255);
        int g = Math.clamp(Math.round(c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio), 0, 255);
        int b = Math.clamp(Math.round(c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio), 0, 255);
        return new Color(r, g, b);
    }
}
