package io.quarkus.infra.performance.graphics.charts;

import io.quarkus.infra.performance.graphics.model.units.Memory;
import org.junit.jupiter.api.Test;

import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_NATIVE;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_NATIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackedNodeTest extends ElasticElementTest {

    @Test
    void testInstanceCountCalculation() {
        PackedNode node = createNode(QUARKUS3_JVM, 276, 8192, 4);
        assertEquals(29, node.getInstanceCount());
    }

    @Test
    void testInstanceCountForLargerMemory() {
        PackedNode node = createNode(SPRING3_JVM, 390, 8192, 4);
        assertEquals(21, node.getInstanceCount());
    }

    @Test
    void testInstanceCountForZeroMemory() {
        PackedNode node = createNode(QUARKUS3_JVM, 0, 8192, 4);
        assertEquals(0, node.getInstanceCount());
    }

    @Test
    void testBoundsOnDimensions() {
        PackedNode node = createNode(QUARKUS3_JVM, 276, 8192, 4);

        assertTrue(node.getPreferredHorizontalSize() >= node.getMinimumHorizontalSize());
        assertTrue(node.getPreferredHorizontalSize() <= node.getMaximumHorizontalSize());
        assertTrue(node.getPreferredVerticalSize() >= node.getMinimumVerticalSize());
        assertTrue(node.getPreferredVerticalSize() <= node.getMaximumVerticalSize());
    }

    @Test
    void testCanDrawInPreferredDimensions() {
        PackedNode node = createNode(QUARKUS3_JVM, 276, 8192, 4);
        node.setHeightScale(1.0);

        var svgGenerator = getSvgGraphics2D(
                node.getPreferredHorizontalSize(),
                node.getPreferredVerticalSize());
        Subcanvas canvas = new Subcanvas(svgGenerator);
        node.draw(canvas, io.quarkus.infra.performance.graphics.Theme.LIGHT);
    }

    @Test
    void testCommonScaleProducesProportionalBlockHeights() {
        double rssQuarkus = 276;
        double rssSpring = 552;
        PackedNode quarkusNode = createNode(QUARKUS3_JVM, rssQuarkus, 8192, 4);
        PackedNode springNode = createNode(SPRING3_JVM, rssSpring, 8192, 4);

        double heightScale = 0.5;
        quarkusNode.setHeightScale(heightScale);
        springNode.setHeightScale(heightScale);

        int quarkusBlockHeight = quarkusNode.getBlockHeight();
        int springBlockHeight = springNode.getBlockHeight();

        assertEquals(heightScale, quarkusNode.getHeightScale());
        assertEquals(heightScale, springNode.getHeightScale());

        // Block height ratio should match RSS ratio (within 1 pixel of rounding)
        // rssSpring / rssQuarkus = 2.0, so springBlockHeight / quarkusBlockHeight ≈ 2.0
        double expectedRatio = rssSpring / rssQuarkus;
        double actualRatio = (double) springBlockHeight / quarkusBlockHeight;
        assertEquals(expectedRatio, actualRatio, 0.02,
                "Block height ratio (%s) should match RSS ratio (%s)".formatted(actualRatio, expectedRatio));
    }

    @Test
    void testBlockWidthIsIndependentOfRss() {
        PackedNode quarkusNode = createNode(QUARKUS3_JVM, 276, 8192, 4);
        PackedNode springNode = createNode(SPRING3_JVM, 550, 8192, 4);

        int containerWidth = 100;
        assertEquals(quarkusNode.getBlockWidth(containerWidth),
                springNode.getBlockWidth(containerWidth),
                "Block width must be the same regardless of RSS");
    }

    @Test
    void testBlockAreaStrictlyProportionalToRss() {
        double rssA = 200;
        double rssB = 400;
        double rssC = 100;

        PackedNode nodeA = createNode(QUARKUS3_JVM, rssA, 8192, 4);
        PackedNode nodeB = createNode(SPRING3_JVM, rssB, 8192, 4);
        PackedNode nodeC = createNode(QUARKUS3_NATIVE, rssC, 8192, 4);

        double heightScale = 0.3;
        nodeA.setHeightScale(heightScale);
        nodeB.setHeightScale(heightScale);
        nodeC.setHeightScale(heightScale);

        int containerWidth = 100;
        int blockWidthA = nodeA.getBlockWidth(containerWidth);
        int blockWidthB = nodeB.getBlockWidth(containerWidth);
        int blockWidthC = nodeC.getBlockWidth(containerWidth);

        // All block widths must be identical (common scale)
        assertEquals(blockWidthA, blockWidthB);
        assertEquals(blockWidthB, blockWidthC);

        long areaA = (long) blockWidthA * nodeA.getBlockHeight();
        long areaB = (long) blockWidthB * nodeB.getBlockHeight();
        long areaC = (long) blockWidthC * nodeC.getBlockHeight();

        // area_B / area_A should equal rssB / rssA = 2.0
        assertEquals(rssB / rssA, (double) areaB / areaA, 0.02,
                "Area ratio B/A should match RSS ratio");
        // area_A / area_C should equal rssA / rssC = 2.0
        assertEquals(rssA / rssC, (double) areaA / areaC, 0.02,
                "Area ratio A/C should match RSS ratio");
        // area_B / area_C should equal rssB / rssC = 4.0
        assertEquals(rssB / rssC, (double) areaB / areaC, 0.05,
                "Area ratio B/C should match RSS ratio");
    }

    @Test
    void testScaleIsCommonAcrossAllNodesInChart() {
        double rssQuarkusJvm = 276;
        double rssSpringJvm = 550;
        double rssQuarkusNative = 76;
        double rssSpringNative = 200;

        PackedNode[] nodes = {
                createNode(QUARKUS3_JVM, rssQuarkusJvm, 8192, 4),
                createNode(SPRING3_JVM, rssSpringJvm, 8192, 4),
                createNode(QUARKUS3_NATIVE, rssQuarkusNative, 8192, 4),
                createNode(SPRING3_NATIVE, rssSpringNative, 8192, 4),
        };

        double sharedScale = 0.42;
        for (PackedNode node : nodes) {
            node.setHeightScale(sharedScale);
        }

        // All nodes must report the same height scale
        for (PackedNode node : nodes) {
            assertEquals(sharedScale, node.getHeightScale(),
                    "All nodes must use the same height scale");
        }

        // Verify proportionality: blockHeight / RSS should be constant (≈ heightScale)
        for (PackedNode node : nodes) {
            double ratio = (double) node.getBlockHeight() / node.getInstanceMemory();
            assertEquals(sharedScale, ratio, 0.03,
                    "blockHeight / RSS should equal the shared height scale");
        }
    }

    private static PackedNode createNode(io.quarkus.infra.performance.graphics.model.Framework framework,
                                         double rssMiB, int schedulableMemoryMiB, int columns) {
        Datapoint d = new Datapoint(framework, new Memory(rssMiB));
        return new PackedNode(d, schedulableMemoryMiB, columns, new LabelGroup(), new LabelGroup(), new LabelGroup());
    }
}
