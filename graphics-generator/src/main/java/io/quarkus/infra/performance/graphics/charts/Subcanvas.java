package io.quarkus.infra.performance.graphics.charts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

import org.apache.batik.svggen.SVGGraphics2D;

import static io.quarkus.infra.performance.graphics.RandomColor.nextColor;

// we won't implement all of Graphics2D, since we only use a few methods
public class Subcanvas {

    public static boolean debug = false;

    private final SVGGraphics2D g;
    private final int width;
    private final int height;
    private final int xOffset;
    private final int yOffset;

    public Subcanvas(SVGGraphics2D g, int width, int height, int xOffset, int yOffset) {
        this.g = g;
        this.width = width;
        this.height = height;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
    }

    public Subcanvas(Subcanvas subcanvas, int width, int height, int xOffset, int yOffset) {
        this.g = subcanvas.g;
        this.width = width;
        this.height = height;
        this.xOffset = subcanvas.xOffset + xOffset;
        this.yOffset = subcanvas.yOffset + yOffset;

        if (height < 0) {
            throw new IllegalArgumentException("Cannot construct a canvas with negative height: " + height);
        }
        if (width < 0) {
            throw new IllegalArgumentException("Cannot construct a canvas with negative width: " + width);
        }

        if (debug) {
            debugBorders();
        }
    }

    public Subcanvas(SVGGraphics2D g) {
        this(g, g.getSVGCanvasSize().width, g.getSVGCanvasSize().height, 0, 0);
    }

    public void setPaint(Color color) {
        g.setPaint(color);
    }

    public void fill() {
        fillRect(0, 0, getWidth(), getHeight());
    }

    public void fillRect(int x, int y, int width, int height) {
        g.fillRect(x + xOffset, y + yOffset, width, height);
    }

    public void drawRect(int x, int y, int width, int height) {
        g.drawRect(x + xOffset, y + yOffset, width, height);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        drawLine(x1, y1, x2, y2, 1);
    }

    public void drawLine(int x1, int y1, int x2, int y2, int thickness) {
        Stroke oldStroke = g.getStroke();
        g.setStroke(new BasicStroke(thickness));
        g.drawLine(x1 + xOffset, y1 + yOffset, x2 + xOffset, y2 + yOffset);
        g.setStroke(oldStroke);
    }

    public void drawCircle(int centerX, int centerY, int diameter) {
        g.drawOval(centerX - diameter / 2 + xOffset, centerY - diameter / 2 + yOffset, diameter, diameter);
    }

    public void fillCircle(int centerX, int centerY, int diameter) {
        g.fillOval(centerX - diameter / 2 + xOffset, centerY - diameter / 2 + yOffset, diameter, diameter);
    }

    public void fill(Rectangle2D.Double aDouble) {
        g.fill(new Rectangle2D.Double(aDouble.x + xOffset, aDouble.y + yOffset, aDouble.getWidth(), aDouble.getHeight()));
    }

    public void drawString(String name, int x, int y) {
        g.drawString(name, x + xOffset, y + yOffset);
    }

    // Access to the underlying graphics object, when we don't have the right abstracted method in place
    public SVGGraphics2D getGraphics() {
        return g;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    // This shouldn't need to be used much
    public int getXOffset() {
        return xOffset;
    }

    // This shouldn't need to be used much
    public int getYOffset() {
        return yOffset;
    }

    public void debugBorders() {
        Color originalColor = g.getColor();
        g.setPaint(nextColor());
        fill();
        g.setPaint(originalColor);
    }

    public void setFont(Font font) {
        g.setFont(font);
    }

    public FontMetrics getFontMetrics(Font font) {
        return g.getFontMetrics(font);
    }
}
