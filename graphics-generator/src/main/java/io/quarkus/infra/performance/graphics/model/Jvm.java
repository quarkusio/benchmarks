package io.quarkus.infra.performance.graphics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Jvm(
    String args,
    String memory,

    @JsonProperty("graalvm")
    GraalVM graalVM,
    String version) {
}
