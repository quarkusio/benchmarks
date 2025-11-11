package io.quarkus.infra.performance.graphics;

import static java.awt.Color.decode;

import java.awt.Color;
import java.util.Map;

import io.quarkus.infra.performance.graphics.model.Framework;

public record Theme(Color background, Color text, Map<Framework, Color> chartElements) {

    public static final String FONT = "Open Sans";

    public static Theme LIGHT = new Theme(Color.WHITE, Color.BLACK,
            Map.of(Framework.QUARKUS3_JVM, decode("#4695EB"), Framework.QUARKUS3_NATIVE, decode("#FF0000"),
                    Framework.QUARKUS3_SPRING_COMPAT, decode("#0D1C2C"), Framework.SPRING3_JVM, decode("#676767"),
                    Framework.SPRING3_NATIVE, decode("#444444"), Framework.SPRING3_JVM_AOT, decode("#808080")));

    public static final Theme DARK = new Theme(Color.BLACK, Color.WHITE,
            Map.of(Framework.QUARKUS3_JVM, decode("#71AEEF"), Framework.QUARKUS3_NATIVE, decode("#70CA8D"),
                    Framework.QUARKUS3_SPRING_COMPAT, decode("#0D1C2C"),
                    Framework.SPRING3_JVM, decode("#999999"),
                    Framework.SPRING3_NATIVE, decode("#777777"), Framework.SPRING3_JVM_AOT, decode("#AAAAAA")));

}
