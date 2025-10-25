package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.model.BenchmarkData;
import org.apache.batik.anim.dom.SVGDOMImplementation;
import org.apache.batik.dom.util.DOMUtilities;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

@ApplicationScoped
public class ImageGenerator {
    public void generate(BenchmarkData data, File outFile) throws IOException {
        String svgNS = SVGDOMImplementation.SVG_NAMESPACE_URI;
        DOMImplementation impl = SVGDOMImplementation.getDOMImplementation();
        Document doc = impl.createDocument(svgNS, "svg", null);
        Element svgRoot = doc.getDocumentElement();
        svgRoot.setAttribute("width", "200");
        svgRoot.setAttribute("height", "100");

        outFile.getParentFile().mkdirs();
        try (Writer writer = new FileWriter(outFile)) {
            DOMUtilities.writeDocument(doc, writer);
        }
        System.out.printf("Writing SVG image to %s\n", outFile.getAbsolutePath());
    }
}
