package org.acme.model;

import java.util.List;

public record NativeBuild(
        List<Double> rss,
        double binarySize
) {
}
