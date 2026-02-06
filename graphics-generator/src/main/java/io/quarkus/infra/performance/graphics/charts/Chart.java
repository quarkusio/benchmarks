package io.quarkus.infra.performance.graphics.charts;

import java.awt.geom.Rectangle2D;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.quarkus.infra.performance.graphics.PlotDefinition;
import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.Config;

public abstract class Chart implements ElasticElement {

    protected final List<Datapoint> data;
    protected final Config metadata;
    // For size calculations, the assumption is that these are stacked vertically
    protected final Set<ElasticElement> children;
    protected final Title title;

    protected final double maxValue;

    final int xmargins = 20;
    final int ymargins = 20;

    protected Chart(PlotDefinition plotDefinition, List<Datapoint> datasets, BenchmarkData bmData) {
        this.data = datasets;
        this.metadata = bmData.config();
        children = new HashSet<>();

        this.title = new Title(plotDefinition.title(), plotDefinition.subtitle());
        children.add(this.title);

        maxValue = data.stream().map(d -> d.value().getValue()).max(Double::compare).orElse(1.0);
    }

    public void draw(Subcanvas g, Theme theme) {
        if (getMinimumHorizontalSize() > g.getWidth()) {
            throw new SizeException("Cannot fit " + getMinimumHorizontalSize() + "px of content into a " + g.getHeight()
                    + "px horizontal space.");
        }
        if (getMinimumVerticalSize() > g.getHeight()) {
            throw new SizeException("Cannot fit " + getMinimumHorizontalSize() + "px of content into a " + g.getHeight()
                    + "px vertical space.");
        }

        int canvasHeight = g.getHeight();
        int canvasWidth = g.getWidth();

        g.setPaint(theme.background());
        g.fill(new Rectangle2D.Double(0, 0, canvasWidth, canvasWidth));

        Subcanvas canvasWithMargins = new Subcanvas(g, canvasWidth - 2 * xmargins, canvasHeight - 2 * ymargins, xmargins,
                ymargins);

        drawNoCheck(canvasWithMargins, theme);
    }

    protected abstract void drawNoCheck(Subcanvas g, Theme theme);

    // SVGs after to be done *after* the main drawing, because getting the document root before drawing causes all subsequent draws to be dropped.
    // This seems to be a characteristic of the Batik streaming model.
    public Collection<InlinedSVG> getInlinedSVGs() {
        return Collections.emptyList();
    }

    @Override
    public int getMaximumVerticalSize() {
        return children.stream().mapToInt(ElasticElement::getMaximumVerticalSize).sum() + 2 * ymargins;
    }

    @Override
    public int getMaximumHorizontalSize() {
        return children.stream().mapToInt(ElasticElement::getMaximumHorizontalSize).max().orElse(0) + 2 * xmargins;
    }

    @Override
    public int getMinimumVerticalSize() {
        return children.stream().mapToInt(ElasticElement::getMinimumVerticalSize).sum() + 2 * ymargins;
    }

    @Override
    public int getMinimumHorizontalSize() {
        return children.stream().mapToInt(ElasticElement::getMinimumHorizontalSize).max().orElse(0) + 2 * xmargins;
    }

    @Override
    public int getPreferredVerticalSize() {
        return children.stream().mapToInt(ElasticElement::getPreferredVerticalSize).sum() + 2 * ymargins;
    }

    @Override
    public int getPreferredHorizontalSize() {
        return children.stream().mapToInt(ElasticElement::getPreferredHorizontalSize).max().orElse(0) + 2 * xmargins;
    }
}
