package io.quarkus.infra.performance.graphics;

import java.awt.Color;
import java.util.Map;

import io.quarkus.infra.performance.graphics.charts.fonts.EmbeddableFont;
import io.quarkus.infra.performance.graphics.model.Framework;

import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_NATIVE;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_SPRING_COMPAT;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_VIRTUAL;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_VIRTUAL_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_JVM_AOT;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_NATIVE;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_VIRTUAL;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_VIRTUAL_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_JVM_AOT;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_NATIVE;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_VIRTUAL;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_VIRTUAL_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING_JVM_AOT;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING_NATIVE;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING_VIRTUAL;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING_VIRTUAL_LEYDEN;
import static java.awt.Color.decode;

public record Theme(String name, Color background, Color text, Color divider, Color finePrint,
                    Map<Framework, Color> chartElements, Map<Framework, Color> fillElements) {
    public static final EmbeddableFont FONT = EmbeddableFont.OPENSANS;
    public static final Color QUARKUS_BLUE = decode("#4695eb");
    public static final Color QUARKUS_MID_BLUE = decode("#8fc6ef");
    public static final Color QUARKUS_LIGHT_BLUE = decode("#b8dcf2");
    public static final Color SPRING_GREEN = decode("#6ab443");
    public static final Color SPRING_MID_GREEN = decode("#96d174");
    public static final Color SPRING_LIGHT_GREEN = decode("#c3e1b4");
    public static final Color SPRING_PALE_GREEN = decode("#d4ebc6");

    public static final Color LIGHT_DIVIDER = decode("#AAAAAA");
    public static final Color DARK_DIVIDER = decode("#555555");

    public static final Color BARELY_GREY = decode("#F9FBFB");
    public static final Color OFF_BLACK = decode("#212121");

    private static final Map<Framework, Color> DEFAULT_CHART_ELEMENTS = Map.ofEntries(
            // Quarkus
            Map.entry(QUARKUS3_JVM, QUARKUS_BLUE),
            Map.entry(QUARKUS3_VIRTUAL, QUARKUS_BLUE),
            Map.entry(QUARKUS3_NATIVE, QUARKUS_BLUE),
            Map.entry(QUARKUS3_SPRING_COMPAT, QUARKUS_BLUE),
            Map.entry(QUARKUS3_LEYDEN, QUARKUS_BLUE),
            Map.entry(QUARKUS3_VIRTUAL_LEYDEN, QUARKUS_BLUE),

            // Spring 3
            Map.entry(SPRING3_JVM, SPRING_LIGHT_GREEN),
            Map.entry(SPRING3_NATIVE, SPRING_LIGHT_GREEN),
            Map.entry(SPRING3_JVM_AOT, SPRING_LIGHT_GREEN),
            Map.entry(SPRING3_VIRTUAL, SPRING_LIGHT_GREEN),
            Map.entry(SPRING3_LEYDEN, SPRING_LIGHT_GREEN),
            Map.entry(SPRING3_VIRTUAL_LEYDEN, SPRING_LIGHT_GREEN),

            // Spring 4
            Map.entry(SPRING4_JVM, SPRING_GREEN),
            Map.entry(SPRING4_NATIVE, SPRING_GREEN),
            Map.entry(SPRING4_JVM_AOT, SPRING_GREEN),
            Map.entry(SPRING4_VIRTUAL, SPRING_GREEN),
            Map.entry(SPRING4_LEYDEN, SPRING_GREEN),
            Map.entry(SPRING4_VIRTUAL_LEYDEN, SPRING_GREEN),

            // Spring
            Map.entry(SPRING_JVM, SPRING_LIGHT_GREEN),
            Map.entry(SPRING_NATIVE, SPRING_LIGHT_GREEN),
            Map.entry(SPRING_LEYDEN, SPRING_LIGHT_GREEN),
            Map.entry(SPRING_JVM_AOT, SPRING_LIGHT_GREEN),
            Map.entry(SPRING_VIRTUAL, SPRING_LIGHT_GREEN),
            Map.entry(SPRING_VIRTUAL_LEYDEN, SPRING_LIGHT_GREEN));

    private static final Map<Framework, Color> DEFAULT_FILL_ELEMENTS = Map.ofEntries(
            // Quarkus - JVM variants get mid blue, Native gets light blue
            Map.entry(QUARKUS3_JVM, QUARKUS_MID_BLUE),
            Map.entry(QUARKUS3_VIRTUAL, QUARKUS_BLUE),
            Map.entry(QUARKUS3_NATIVE, QUARKUS_LIGHT_BLUE),
            Map.entry(QUARKUS3_SPRING_COMPAT, QUARKUS_MID_BLUE),
            Map.entry(QUARKUS3_LEYDEN, QUARKUS_MID_BLUE),
            Map.entry(QUARKUS3_VIRTUAL_LEYDEN, QUARKUS_BLUE),

            // Spring 3 - JVM variants get mid green, Native gets light green
            Map.entry(SPRING3_JVM, SPRING_MID_GREEN),
            Map.entry(SPRING3_NATIVE, SPRING_LIGHT_GREEN),
            Map.entry(SPRING3_JVM_AOT, SPRING_MID_GREEN),
            Map.entry(SPRING3_VIRTUAL, SPRING_GREEN),
            Map.entry(SPRING3_LEYDEN, SPRING_MID_GREEN),
            Map.entry(SPRING3_VIRTUAL_LEYDEN, SPRING_GREEN),

            // Spring 4 - JVM variants get mid green, Native gets light green
            Map.entry(SPRING4_JVM, SPRING_MID_GREEN),
            Map.entry(SPRING4_NATIVE, SPRING_LIGHT_GREEN),
            Map.entry(SPRING4_JVM_AOT, SPRING_MID_GREEN),
            Map.entry(SPRING4_VIRTUAL, SPRING_GREEN),
            Map.entry(SPRING4_LEYDEN, SPRING_MID_GREEN),
            Map.entry(SPRING4_VIRTUAL_LEYDEN, SPRING_GREEN),

            // Spring (unversioned)
            Map.entry(SPRING_JVM, SPRING_MID_GREEN),
            Map.entry(SPRING_NATIVE, SPRING_LIGHT_GREEN),
            Map.entry(SPRING_LEYDEN, SPRING_MID_GREEN),
            Map.entry(SPRING_JVM_AOT, SPRING_MID_GREEN),
            Map.entry(SPRING_VIRTUAL, SPRING_GREEN),
            Map.entry(SPRING_VIRTUAL_LEYDEN, SPRING_GREEN));

    public static final Theme LIGHT = new Theme("light", Color.WHITE, decode("#121212"), LIGHT_DIVIDER, BARELY_GREY);
    public static final Theme DARK = new Theme("dark", decode("#121212"), decode("#B5B5B5"), DARK_DIVIDER, OFF_BLACK);

    public Theme(String name, Color background, Color text, Color divider, Color finePrint) {
        this(name, background, text, divider, finePrint, DEFAULT_CHART_ELEMENTS, DEFAULT_FILL_ELEMENTS);
    }

}
