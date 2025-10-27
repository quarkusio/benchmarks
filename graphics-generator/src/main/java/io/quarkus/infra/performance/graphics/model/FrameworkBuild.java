package io.quarkus.infra.performance.graphics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FrameworkBuild(
    @JsonProperty("native_build_options")
    String nativeBuildOptions,
    String version
) {
}
