package io.quarkus.infra.performance.graphics;

import java.util.function.Function;

import io.quarkus.infra.performance.graphics.model.Result;
import io.quarkus.infra.performance.graphics.model.units.DimensionalNumber;

public record PlotDefinition(String title, String filename, String subtitle, Function<Result, ? extends DimensionalNumber> fun) {
  public PlotDefinition(String title, String subtitle, Function<Result, ? extends DimensionalNumber> fun) {
    this(title, title, subtitle, fun);
  }
}
