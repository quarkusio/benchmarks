package io.quarkus.infra.performance.graphics.charts;

import java.awt.Font;
import java.awt.FontMetrics;
import java.util.Arrays;
import java.util.Comparator;

import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.charts.fonts.Alignment;
import io.quarkus.infra.performance.graphics.charts.fonts.FontStyle;
import io.quarkus.infra.performance.graphics.charts.fonts.Sizer;
import io.quarkus.infra.performance.graphics.charts.fonts.VAlignment;

import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.PLAIN;

public class Label {

    public static final String LINE_BREAK = "\n";
    private static final int DEFAULT_TARGET_HEIGHT = 24;

    private final String[] strings;
    private int targetHeight = 24; // Arbitrary default
    private double lineSpacing = 1;
    private DelimitedStyles styles = new DelimitedStyles(new FontStyle[]{PLAIN}, LINE_BREAK);
    private Alignment alignment = Alignment.LEFT;
    private VAlignment valignment = VAlignment.MIDDLE;
    private FontMetrics fontMetrics;
    private final LabelGroup labelGroup;

    /**
     * @param text Use \n for multiline text
     */
    public Label(String text) {
        this(text.split(LINE_BREAK));
    }

    public Label(String[] lines) {
        this(lines, new LabelGroup());
    }

    public Label(String text, LabelGroup labelGroup) {
        this(text.split(LINE_BREAK), labelGroup);
    }

    public Label(String[] lines, LabelGroup labelGroup) {
        this.strings = lines;
        this.labelGroup = labelGroup;
        setTargetHeight(DEFAULT_TARGET_HEIGHT);
    }

    public void draw(Subcanvas g) {
        draw(g, 0, 0);
    }

    public void draw(Subcanvas g, int x, int y) {

        g.getGraphics().setFont(labelGroup.getBaseFont());

        // The SVG attribute alignment-baseline="middle" is not supported by Batik.
        // The value we pass in to drawString is the position of the bottom baseline

        // Four variables describe the height of a font: leading (pronounced like the metal), ascent, descent, and height. Leading is the amount of space required between lines of the same font. Ascent is the space above the baseline required by the tallest character in the font. Descent is the space required below the baseline by the lowest descender (the "tail" of a character like "y"). Height is the total of the three: ascent, baseline, and descent.

        // Should be the same as the targetHeight, but recalculate in case of rounding errors
        fontMetrics = g.getGraphics().getFontMetrics();
        int lineHeight = (int) (fontMetrics.getHeight() * lineSpacing);
        int textBlockHeight = lineHeight * strings.length;

        // Compute starting y to vertically center the text block
        int yPosition = switch (valignment) {
            case TOP -> y + fontMetrics.getAscent();
            case BOTTOM -> y;
            case MIDDLE -> y - textBlockHeight / 2 + fontMetrics.getAscent();
        };

        for (int i = 0; i < strings.length; i++) {

            String string = strings[i];
            // Set a base font for the line height
            Font font = labelGroup.getFont(i);
            g.setFont(font);

            FontMetrics metrics = g.getFontMetrics(font);

            // String bounds is a bit more accurate than getWidth() for alignment
            int width = (int) Math.round(metrics.getStringBounds(string, g.getGraphics()).getWidth());

            int alignedX = switch (alignment) {
                case LEFT -> x;
                case RIGHT -> x - width;
                case CENTER -> x - width / 2;
            };

            // Now we may need to split further; if the delimiter was a line-break, this will be a no-op
            String[] segments = string.split(styles.delimiter());
            int segmentX = alignedX;

            for (int j = 0; j < segments.length; j++) {
                int index;
                if (LINE_BREAK.equals(styles.delimiter())) {
                    index = i;
                } else if (segments.length == 1) {
                    index = styles.styles().length - 1;
                } else {
                    index = j;
                }
                font = labelGroup.getFont(index);
                g.getGraphics().setFont(font);
                metrics = g.getGraphics().getFontMetrics(font);

                String segment = segments[j];
                // Add back the delimiter, except at the end
                if (j < segments.length - 1) {
                    segment += styles.delimiter();
                }
                // String bounds is a bit more accurate than getWidth() for alignment
                int segmentWidth = (int) Math.round(metrics.getStringBounds(segment, g.getGraphics()).getWidth());

                g.drawString(segment, segmentX, yPosition);
                segmentX += segmentWidth;
            }
            yPosition += metrics.getHeight() * lineSpacing; // Recalculate the line height in case the style affected the height slightly

        }


    }

    int getAscent() {
        return fontMetrics.getAscent();
    }

    public Label setTargetHeight(int height) {
        this.targetHeight = height;
        int size = strings.length > 1 ? Sizer.calculateFontSize((int) (targetHeight / (strings.length * lineSpacing))):Sizer.calculateFontSize(targetHeight);
        Font baseFont = Theme.FONT.getFont(PLAIN, size);
        labelGroup.setBaseFont(baseFont);

        return this;
    }

    public int getTargetHeight() {
        return targetHeight;
    }

    public Label setHorizontalAlignment(Alignment alignment) {
        this.alignment = alignment;
        return this;
    }

    public Alignment getHorizontalAlignment() {
        return alignment;
    }

    /**
     * Should be one of the constants declared in #Font or their combination
     */
    public Label setStyle(FontStyle style) {
        return setStyles(new FontStyle[]{style});
    }

    /**
     * Should be one of the constants declared in #Font or their combination
     * The different styles are applied to different parts of the text, with the default delimiter being \n
     */
    public Label setStyles(FontStyle[] styles) {
        return setStyles(styles, LINE_BREAK);
    }


    /*
     *    Note that if a style delimiter is set (other than a line break), line breaks will no longer be style delimiters.
     */
    public Label setStyles(FontStyle[] styles, String newDelimiter) {

        this.styles = new DelimitedStyles(styles, newDelimiter);

        labelGroup.setStyles(this.styles.styles());

        return this;
    }

    public Label setVerticalAlignment(VAlignment vAlignment) {
        this.valignment = vAlignment;
        return this;
    }

    public int getLineHeight() {
        if (fontMetrics != null) {
            return fontMetrics.getHeight();
        } else {
            return Sizer.calculateHeight(labelGroup.getFontSize());
        }
    }

    public int calculateWidth(String s) {
        if (fontMetrics != null) {
            return fontMetrics.stringWidth(s);
        } else {
            return Sizer.calculateWidth(s, labelGroup.getFontSize());
        }
    }

    public int calculateWidth(String s, FontStyle style) {
        return Sizer.calculateWidth(s, Theme.FONT.getFont(style, labelGroup.getFontSize()));
    }

    public int getActualHeight() {
        return (int) (strings.length * lineSpacing * getLineHeight());
    }

    public int getDescent() {
        return fontMetrics.getDescent();
    }

    public int calculateWidth() {
        if (strings.length > 0) {
            String longestText = getLongestText();
            int index = Arrays.asList(strings).indexOf(longestText);
            Font font = labelGroup.getFont(index);

            return Sizer.calculateWidth(longestText, font);
        } else {
            return 0;
        }
    }


    public double calculateWidth(int fontSize) {
        String longestText = getLongestText();
        return Sizer.calculateWidth(longestText, fontSize);
    }


    private String getLongestText() {
        // It would be cheaper to just count characters, but sometimes the longest string isn't the fattest – including for framework labels we are using
        return Arrays.stream(strings).max(Comparator.comparingInt(this::calculateWidth)).orElse("");
    }

    public String toString() {
        return "Label[" + getLongestText() + "]";
    }

    public void setLineSpacing(int l) {
        this.lineSpacing = l;
    }

    private record DelimitedStyles(FontStyle[] styles, String delimiter) {
    }

}
