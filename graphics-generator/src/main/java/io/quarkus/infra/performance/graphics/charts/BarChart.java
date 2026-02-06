package io.quarkus.infra.performance.graphics.charts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import io.quarkus.infra.performance.graphics.PlotDefinition;
import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;

public class BarChart extends Chart {

    private final FinePrint fineprint;
    private final List<Bar> bars = new ArrayList<>();

    public BarChart(PlotDefinition plotDefinition, List<Datapoint> data, BenchmarkData bmData) {
        super(plotDefinition, data, bmData);

        this.fineprint = new FinePrint(bmData);
        children.add(fineprint);

        for (Datapoint d : data) {
            Bar e = new Bar(d);
            bars.add(e);
            children.add(e);
        }

    }

    @Override
    protected void drawNoCheck(Subcanvas canvasWithMargins, Theme theme) {

        int finePrintHeight = fineprint.getPreferredVerticalSize();

        canvasWithMargins.setPaint(theme.text());
        Subcanvas titleCanvas = new Subcanvas(canvasWithMargins, canvasWithMargins.getWidth(), title.getPreferredVerticalSize(),
                0, 0);
        title.draw(titleCanvas, theme);

        Subcanvas barArea = new Subcanvas(canvasWithMargins, canvasWithMargins.getWidth(),
                canvasWithMargins.getHeight() - titleCanvas.getHeight() - finePrintHeight, 0, titleCanvas.getHeight());

        int leftLabelWidth = Sizer.calculateWidth(bars.stream().map(Bar::getLeftLabelText).collect(Collectors.toSet()),
                Bar.LEFT_LABEL_SIZE);

        // Set a common left label width before trying to calculate a scale
        for (Bar bar : bars) {
            bar.setLeftLabelWidth(leftLabelWidth);
        }

        double scale = bars.stream().mapToDouble(bar -> bar.getMaximumScale(barArea)).min().orElse(1);

        int y = 0;

        int finePrintPadding = 125; // TODO Arbitrary fudge padding, remove when scaling work is done
        Subcanvas finePrintArea = new Subcanvas(canvasWithMargins, barArea.getWidth() - 2 * finePrintPadding, finePrintHeight,
                finePrintPadding,
                barArea.getHeight() + titleCanvas.getHeight());
        canvasWithMargins.setPaint(theme.text());

        for (Bar bar : bars) {
            bar.setScale(scale);
            // TODO slight hack, assuming the min and max are always equal
            Subcanvas individualBarArea = new Subcanvas(barArea, barArea.getWidth(), bar.getMaximumVerticalSize(), 0, y);
            bar.draw(individualBarArea, theme);
            y += individualBarArea.getHeight();

        }

        fineprint.draw(finePrintArea, theme);
    }

    @Override
    public Collection<InlinedSVG> getInlinedSVGs() {
        return fineprint.getInlinedSVGs();
    }

}
