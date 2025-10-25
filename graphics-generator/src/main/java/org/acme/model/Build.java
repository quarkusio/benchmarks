package org.acme.model;

import java.util.List;

public record Build(
        List<Double> timings,
        NativeBuild nativeBuild,  // maps the "native" JSON object,
        double avBuildTime,
        Double avNativeRSS,     // optional,
        String classCount,
        String fieldCount,
        String methodCount,
        String reflectionClassCount,
        String reflectionFieldCount,
        String reflectionMethodCount) {
}

