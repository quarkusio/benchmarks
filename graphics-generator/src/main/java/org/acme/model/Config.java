package org.acme.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Config(

        @JsonProperty("QUARKUS_VERSION")
        String quarkusVersion,

        @JsonProperty("CMD_PREFIX")
        String cmdPrefix,

        @JsonProperty("NATIVE_QUARKUS_BUILD_OPTIONS")
        String nativeQuarkusBuildOptions,

        @JsonProperty("NATIVE_SPRING_BUILD_OPTIONS")
        String nativeSpringBuildOptions,

        @JsonProperty("JVM_MEMORY")
        String jvmMemory,

        @JsonProperty("repo")
        Repo repo,

        @JsonProperty("GRAALVM_VERSION")
        String graalvmVersion,

        @JsonProperty("profiler")
        Profiler profiler,

        @JsonProperty("JAVA_VERSION")
        String javaVersion,

        @JsonProperty("cgroup")
        Cgroup cgroup,

        @JsonProperty("SPRING_BOOT_VERSION")
        String springBootVersion
) {
}
