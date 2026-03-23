package io.quarkus.infra.performance.graphics.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Resources(
    @JsonProperty("app_cpus") String appCpus,
    Cpu cpu
) {

  public record Cpu(
      String app,
      @JsonProperty("1st_request") String firstRequest,
      @JsonProperty("load_generator") String loadGenerator,
      String db
  ) {}
}
