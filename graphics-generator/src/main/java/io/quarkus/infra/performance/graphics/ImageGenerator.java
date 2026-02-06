package io.quarkus.infra.performance.graphics;

import static io.quarkus.infra.performance.graphics.SvgAdjuster.adjustSvg;

import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import io.quarkus.infra.performance.graphics.charts.Chart;
import io.quarkus.infra.performance.graphics.charts.Datapoint;
import io.quarkus.infra.performance.graphics.charts.InlinedSVG;
import io.quarkus.infra.performance.graphics.charts.Subcanvas;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;

@ApplicationScoped
public class ImageGenerator {
    private static final String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;

    public void generate(TriFunction<PlotDefinition, List<Datapoint>, BenchmarkData, Chart> chartConstructor, BenchmarkData data,
                         PlotDefinition plotDefinition, File outFile, Theme theme)
            throws IOException {
        if (data!=null && data.results()!=null) {
            DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
            Document doc = impl.createDocument(svgNS, "svg", null);

            Chart chart = chartConstructor.apply(plotDefinition,
                    data.results().getDatasets(plotDefinition.fun()),
                    data);

            SVGGraphics2D svgGenerator = new SVGGraphics2D(doc);
            svgGenerator.setSVGCanvasSize(
                    new Dimension(chart.getPreferredHorizontalSize(), chart.getPreferredVerticalSize())
            );
            chart.draw(new Subcanvas(svgGenerator), theme);

            Element root = svgGenerator.getRoot();
            initialiseFonts(doc, root);
            inlineGraphics(doc, root, chart.getInlinedSVGs());


            outFile.getParentFile().mkdirs();

            StringWriter buffer = new StringWriter();
            svgGenerator.stream(root, buffer, true, false);
            String svg = buffer.toString();

            String adjusted = adjustSvg(svg);

            try (FileWriter writer = new FileWriter(outFile)) {
                writer.write(adjusted);
            }

        } else {
            System.out.printf("\uD83D\uDDD1️ Not generating image for %s (no data)\n", outFile.getAbsolutePath());

        }
    }

    private void inlineGraphics(Document doc, Element root, Collection<InlinedSVG> inlinedSVGs) {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);

        for (InlinedSVG inlinedSVG : inlinedSVGs) {
            inlinedSVG.draw(factory, root, doc);
        }
    }

    private static void initialiseFonts(Document doc, Element root) {
        Element style = doc.createElementNS(svgNS, "style");
        style.setTextContent(Theme.FONT.getCss());
        root.setAttribute("font-family", Theme.FONT.getFamilyDeclaration());
        root.insertBefore(style, root.getFirstChild());

    }

}
