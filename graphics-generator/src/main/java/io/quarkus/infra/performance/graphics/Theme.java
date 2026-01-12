package io.quarkus.infra.performance.graphics;

import static java.awt.Color.decode;

import java.awt.Color;
import java.util.Map;

import io.quarkus.infra.performance.graphics.charts.EmbeddableFont;
import io.quarkus.infra.performance.graphics.model.Framework;

public record Theme(String name, Color background, Color text, Map<Framework, Color> chartElements) {
    public static final EmbeddableFont FONT = EmbeddableFont.OPENSANS;
    public static final Color QUARKUS_BLUE = decode("#4695eb");
    public static final Color SPRING_GREEN = decode("#6ab443");
    public static final Color SPRING_LIGHT_GREEN = decode("#c3e1b4");

    private static final Map<Framework, Color> DEFAULT_CHART_ELEMENTS = Map.ofEntries(
        // Quarkus
        Map.entry(Framework.QUARKUS3_JVM, QUARKUS_BLUE),
        Map.entry(Framework.QUARKUS3_VIRTUAL, QUARKUS_BLUE),
        Map.entry(Framework.QUARKUS3_NATIVE, QUARKUS_BLUE),
        Map.entry(Framework.QUARKUS3_SPRING_COMPAT, QUARKUS_BLUE),

        // Spring 3
        Map.entry(Framework.SPRING3_JVM, SPRING_LIGHT_GREEN),
        Map.entry(Framework.SPRING3_NATIVE, SPRING_LIGHT_GREEN),
        Map.entry(Framework.SPRING3_JVM_AOT, SPRING_LIGHT_GREEN),
        Map.entry(Framework.SPRING3_VIRTUAL, SPRING_LIGHT_GREEN),

        // Spring 4
        Map.entry(Framework.SPRING4_JVM, SPRING_GREEN),
        Map.entry(Framework.SPRING4_NATIVE, SPRING_GREEN),
        Map.entry(Framework.SPRING4_JVM_AOT, SPRING_GREEN),
        Map.entry(Framework.SPRING4_VIRTUAL, SPRING_GREEN),

        // Spring
        Map.entry(Framework.SPRING_JVM, SPRING_LIGHT_GREEN),
        Map.entry(Framework.SPRING_NATIVE, SPRING_LIGHT_GREEN),
        Map.entry(Framework.SPRING_JVM_AOT, SPRING_LIGHT_GREEN),
        Map.entry(Framework.SPRING_VIRTUAL, SPRING_LIGHT_GREEN));

    public static final Theme LIGHT = new Theme("light", Color.WHITE, Color.BLACK);
    public static final Theme DARK = new Theme("dark", Color.BLACK, Color.WHITE);

    public Theme(String name, Color background, Color text) {
      this(name, background, text, DEFAULT_CHART_ELEMENTS);
    }

}
