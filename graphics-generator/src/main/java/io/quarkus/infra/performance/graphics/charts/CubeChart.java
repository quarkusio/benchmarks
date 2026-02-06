package io.quarkus.infra.performance.graphics.charts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import io.quarkus.infra.performance.graphics.PlotDefinition;
import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;

public class CubeChart extends Chart {

    private static final int MINIMUM_PADDING_BETWEEN_DATASETS = 10;
    private final FinePrint fineprint;
    private final List<Cubes> cubes = new ArrayList<>();
    int numCubesPerColumn = 16;

    public CubeChart(PlotDefinition plotDefinition, List<Datapoint> data, BenchmarkData bmData) {
        super(plotDefinition, data, bmData);

        this.fineprint = new FinePrint(bmData);
        children.add(fineprint);

        Cubes.setNumCubesPerColumn(numCubesPerColumn);

        // Make sure the preferred size calculations don't return something absurdly huge for the number of datasets
        int approximateTotalNumberOfColumns = (int) (data.stream().mapToDouble(d -> d.value().getValue()).sum()
                / numCubesPerColumn);
        Cubes.setMaxCubeSize(2000 / approximateTotalNumberOfColumns);

        for (Datapoint d : data) {
            Cubes c = new Cubes(d);
            cubes.add(c);

        }

        // Add the tallest cube to the children, for height calculations
        cubes.stream()
                .max(Comparator.comparing(Cubes::getPreferredVerticalSize))
                .ifPresent(children::add);
    }

    @Override
    public int getMaximumHorizontalSize() {
        return cubes.stream().mapToInt(ElasticElement::getMaximumHorizontalSize).sum() + 2 * xmargins;
    }

    @Override
    public int getMinimumHorizontalSize() {
        return cubes.stream().mapToInt(ElasticElement::getMinimumHorizontalSize).sum() + 2 * xmargins;
    }

    @Override
    public int getPreferredHorizontalSize() {
        return cubes.stream().mapToInt(ElasticElement::getPreferredHorizontalSize).sum() + 2 * xmargins;
    }

    @Override
    protected void drawNoCheck(Subcanvas canvasWithMargins, Theme theme) {

        int finePrintHeight = fineprint.getPreferredVerticalSize();

        canvasWithMargins.setPaint(theme.text());

        Subcanvas titleCanvas = new Subcanvas(canvasWithMargins, canvasWithMargins.getWidth(), title.getPreferredVerticalSize(),
                0, 0);
        title.draw(titleCanvas, theme);

        Subcanvas plotArea = new Subcanvas(canvasWithMargins, canvasWithMargins.getWidth(),
                canvasWithMargins.getHeight() - titleCanvas.getHeight() - finePrintHeight, 0, titleCanvas.getHeight());
        int finePrintPadding = 300; // TODO Arbitrary fudge padding, remove when scaling work is done
        Subcanvas finePrintArea = new Subcanvas(canvasWithMargins, plotArea.getWidth() - 2 * finePrintPadding, finePrintHeight,
                finePrintPadding,
                plotArea.getHeight() + titleCanvas.getHeight());

        int dataPadding = workOutCubeSizes(canvasWithMargins);

        while (dataPadding <= MINIMUM_PADDING_BETWEEN_DATASETS) {
            // If it doesn't fit, shrink fonts
            for (Cubes c : cubes) {
                c.decrementFonts();
            }
            dataPadding = workOutCubeSizes(canvasWithMargins);
        }

        int x = 0;

        for (Cubes c : cubes) {

            int width = c.getActualHorizontalSize();

            Subcanvas dataArea = new Subcanvas(plotArea, width, plotArea.getHeight(), x, 0);
            c.draw(dataArea, theme);

            x += dataArea.getWidth() + dataPadding;

        }
        fineprint.draw(finePrintArea, theme);
    }

    private int workOutCubeSizes(Subcanvas canvasWithMargins) {
        int minimumDataPadding = 8;

        int totalColumnsContributingToWidth = 0;

        int unitsPerCube = 1; // For now, assume 1mb per square
        int maxColumns = (int) Math.ceil(maxValue / (numCubesPerColumn * unitsPerCube));

        int numGutters = data.size() - 1;
        int gutterPadding = minimumDataPadding * numGutters;
        int availableWidth = canvasWithMargins.getWidth() - gutterPadding;

        int minColumnWidth = availableWidth / (maxColumns * data.size());
        int widthOfThinSections = 0;

        int smallestCubeSize = Integer.MAX_VALUE;

        for (Cubes c : cubes) {
            // Iterate to a correct value; to start off with, set a column width
            c.setCubeSize(minColumnWidth);

            // Estimate the likely width of a column, assuming evenly spaced sections
            if (c.getActualHorizontalSize() > c.getColumnCount() * minColumnWidth) {
                widthOfThinSections += c.getMinimumHorizontalSize();
                int cubeSizeForThisSection = c.getMinimumHorizontalSize() / c.getColumnCount();

                smallestCubeSize = Math.min(smallestCubeSize, cubeSizeForThisSection);
            } else {
                totalColumnsContributingToWidth += c.getColumnCount();
            }
        }

        int cubeWithPaddingSize = totalColumnsContributingToWidth > 0
                ? (availableWidth - widthOfThinSections) / totalColumnsContributingToWidth
                :smallestCubeSize;
        // If no columns go outside the border, then we use the maximum column count and divide it by the average width occupied by captions

        int actualOccupiedArea = 0;

        // Work out how much space is used, so we can space the sections evenly
        for (Cubes c : cubes) {
            c.setCubeSize(cubeWithPaddingSize);
            int width = c.getActualHorizontalSize();
            actualOccupiedArea += width;
        }

        return (canvasWithMargins.getWidth() - actualOccupiedArea) / numGutters;
    }

    @Override
    public Collection<InlinedSVG> getInlinedSVGs() {
        return fineprint.getInlinedSVGs();
    }
}
