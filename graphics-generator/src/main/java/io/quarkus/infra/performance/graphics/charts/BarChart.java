package io.quarkus.infra.performance.graphics.charts;

import static java.lang.Math.round;

import java.awt.Font;
import java.awt.geom.Rectangle2D;
import java.util.List;

import org.apache.batik.svggen.SVGGraphics2D;

import io.quarkus.infra.performance.graphics.Theme;

public class BarChart implements Chart {
    private static final int BAR_THICKNESS = 70;
    private final SVGGraphics2D g;
    private final int canvasHeight;
    private final int canvasWidth;
    private final Theme theme;
    private int labelSize = 24;

    private static final Font titleFont = new Font(Theme.FONT, Font.BOLD, 24);
    private static final Font keyFont = new Font(Theme.FONT, Font.BOLD, 18);
    private static final Font labelFont = new Font(Theme.FONT, Font.PLAIN, 14);

    public BarChart(SVGGraphics2D g, Theme theme) {
        this.g = g;
        this.theme = theme;
        this.canvasHeight = g.getSVGCanvasSize().height;
        this.canvasWidth = g.getSVGCanvasSize().width;

    }

    @Override
    public void draw(String title, List<Datapoint> data) {

        g.setPaint(theme.background());
        g.fill(new Rectangle2D.Double(0, 0, canvasWidth, canvasWidth));

        // --- Draw section titles ---
        g.setPaint(theme.text());
        g.setFont(titleFont);

        double maxValue = data.stream().map(d -> d.value().getValue()).max(Double::compare).orElse(1.0);

        int plotHeight = canvasHeight - 3 * labelSize;
        int barSpacing = plotHeight / data.size() - BAR_THICKNESS;

        int labelAllowance = 250;
        int leftMargin = 20;
        int barWidth = canvasWidth - 2 * labelAllowance;

        int verticalOffset = 2 * labelSize;
        g.drawString(title, leftMargin, verticalOffset);

        int plotWidth = canvasWidth - 2 * leftMargin;
        Subcanvas chartArea = new Subcanvas(g, plotWidth, plotHeight, leftMargin, barSpacing / 2 + verticalOffset);

        int y = 0;
        int labelPadding = 20;

        Subcanvas labelArea = new Subcanvas(chartArea, labelAllowance, plotHeight, 0, 0);
        // Fudge factor for asymmetric margins
        int fudge = 40;
        Subcanvas barArea = new Subcanvas(chartArea, barWidth, plotHeight, labelAllowance - fudge, 0);

        for (Datapoint d : data) {
            // If this framework isn't found, it will just be the text colour, which is fine
            barArea.setPaint(theme.chartElements().get(d.framework()));
            double val = d.value().getValue();
            double scale = barWidth / maxValue;
            int length = (int) (val * scale);
            barArea.fillRect(0, y, length, BAR_THICKNESS);

            labelArea.setPaint(theme.text());
            g.setFont(labelFont);
            // Vertically align text with the centre of the bars
            // The SVG attribute alignment-baseline="middle" is not supported by Batik.
            // Why do we divide by 4 instead of 2? I don't know. :)
            int labelY = y + labelSize / 4 + BAR_THICKNESS / 2;
            labelArea.drawString(d.framework().getName(), 0, labelY);
            g.setFont(keyFont);
            barArea.drawString(java.lang.String.format("%d %s", round(val), d.value().getUnits()),
                    length + labelPadding,
                    labelY);

            y += BAR_THICKNESS + barSpacing;

        }
    }

}
