package io.quarkus.infra.performance.graphics.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.infra.performance.graphics.util.CPUParser;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Resources(
    @JsonProperty("app_cpus") String appCpus,
    Cpu cpu
) {

  @JsonCreator
  public Resources(@JsonProperty("cpu") Cpu cpu) {
    this(cpu != null && cpu.app() != null ? CPUParser.parse(cpu.app()) : 0, cpu);
  }

  public record Cpu(
      String app,
      @JsonProperty("1st_request") String firstRequest,
      @JsonProperty("load_generator") String loadGenerator,
      String db
  ) {}
}
