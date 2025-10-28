package io.quarkus.infra.performance.graphics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Config(
        Jvm jvm,

        @JsonProperty("CMD_PREFIX")
        String cmdPrefix,

        @JsonProperty("num_iterations")
        int numIterations,

        FrameworkBuild quarkus,

        @JsonProperty("repo")
        Repo repo,

        @JsonProperty("profiler")
        Profiler profiler,

        @JsonProperty("cgroup")
        Cgroup cgroup,

        FrameworkBuild springboot
) {
}
