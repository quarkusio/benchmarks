package org.acme.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Cgroup(
        @JsonProperty("mem_max")
        String memMax,
        String name,
        String cpu) {
}