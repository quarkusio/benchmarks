package io.quarkus.infra.performance.graphics;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.quarkus.infra.performance.graphics.charts.BarChart;
import io.quarkus.infra.performance.graphics.charts.Chart;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;

@ApplicationScoped
public class ImageGenerator {
    private static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

    // To make fonts work, we need a css declaration (below), and we also need to tell Java about the font (done here)
    static {
        // Java needs a ttf font, not a woff, so read from the github repo
        String fontUrl = "https://github.com/googlefonts/opensans/raw/refs/heads/main/fonts/variable/OpenSans%5Bwdth,wght%5D.ttf";
        try (InputStream fontStream = new URI(fontUrl).toURL().openStream()) {
            Font openSans = Font.createFont(Font.TRUETYPE_FONT, fontStream);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(openSans);
        } catch (FontFormatException | URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void generate(BenchmarkData data, PlotDefinition plotDefinition, File outFile, Theme theme)
            throws IOException {
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        Document doc = impl.createDocument(svgNS, "svg", null);

        SVGGraphics2D svgGenerator = new SVGGraphics2D(doc);
        svgGenerator.setSVGCanvasSize(new Dimension(1200, 600));

        Chart chart = new BarChart(svgGenerator, theme);
        chart.draw(plotDefinition.title(), data.results().getDatasets(plotDefinition.fun()));

        Element root = svgGenerator.getRoot();
        initialiseFonts(doc, root);

        outFile.getParentFile().mkdirs();
        try (Writer out = new OutputStreamWriter(new FileOutputStream(outFile), StandardCharsets.UTF_8)) {
            svgGenerator.stream(root, out, true, false);
            System.out.printf("\uD83C\uDFA8 Wrote SVG image to %s\n", outFile.getAbsolutePath());
        }
    }

    private static void initialiseFonts(Document doc, Element root) {
        //Annoyingly, we can't use the same URLs as we use to register the font, and we also can't use the current v44 versions of the fonts
        String styleContent = """
                @font-face {
                    font-family: 'Open Sans';
                    font-style: normal;
                    font-weight: 300;
                    src: url(https://fonts.gstatic.com/s/opensans/v20/mem5YaGs126MiZpBA-UN_r8OUuhpKKSTjw.woff2) format('woff2');
                                                                                unicode-range: U+0000-00FF, U+0131, U+0152-0153, U+02BB-02BC, U+02C6, U+02DA, U+02DC, U+2000-206F, U+2074, U+20AC, U+2122, U+2191, U+2193, U+2212, U+2215, U+FEFF, U+FFFD;
                }
                @font-face {
                    font-family: 'Open Sans';
                    font-style: normal;
                    font-weight: 400 700;
                    src: url(https://fonts.gstatic.com/s/opensans/v20/mem8YaGs126MiZpBA-UFVZ0bf8pkAg.woff2) format('woff2');
                                                                                unicode-range: U+0000-00FF, U+0131, U+0152-0153, U+02BB-02BC, U+02C6, U+02DA, U+02DC, U+2000-206F, U+2074, U+20AC, U+2122, U+2191, U+2193, U+2212, U+2215, U+FEFF, U+FFFD;
                }
                """;
        Element style = doc.createElementNS(svgNS, "style");
        style.setTextContent(styleContent);
        root.insertBefore(style, root.getFirstChild());
    }

}
