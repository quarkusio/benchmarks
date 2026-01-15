package io.quarkus.infra.performance.graphics;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import jakarta.inject.Inject;

import io.quarkus.infra.performance.graphics.charts.BarChart;
import io.quarkus.infra.performance.graphics.charts.Chart;
import io.quarkus.infra.performance.graphics.charts.CubeChart;
import io.quarkus.infra.performance.graphics.charts.Datapoint;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.Config;
import io.quarkus.infra.performance.graphics.model.Repo;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "graphics", mixinStandardHelpOptions = true)
public class GraphicsCommand implements Runnable {

  private static final PlotDefinition THROUGHPUT = new PlotDefinition("Throughput", "(Higher is better)", framework -> framework.load().avThroughput());
  private static final PlotDefinition RSS = new PlotDefinition("Memory (RSS after 1st request)", "memory-rss", "(Smaller is better)", framework -> framework.rss().avFirstRequestRss());
  private static final PlotDefinition TIME_TO_FIRST_REQUEST = new PlotDefinition("Boot + First Response Time", "(Lower is better)", framework -> framework.startup().avStartTime());
  private static final PlotDefinition BUILD_TIME = new PlotDefinition("Build Duration", "(Lower is better)", framework -> framework.build().avBuildTime());

  @Parameters(paramLabel = "<filename>", defaultValue = "latest.json", description = "A filename of json-formatted data, or a directory. For directories, .json files in the directory will be processed recursively.")
  Path filename;

  @Parameters(paramLabel = "<outputDir>", defaultValue = ".", description = "The directory for generated images")
  File outputDirectory;

  @Parameters(paramLabel = "<tolerateMissingData>", defaultValue = "true", description = "Whether to suppress exceptions caused by missing data")
  boolean tolerateMissingData;

  @Inject
  DataIngester ingester;

  @Inject
  ImageGenerator generator;

  @Override
  public void run() {

    processPath(filename.toFile());

  }

  private void processPath(File inputFile) {
    if (inputFile.isDirectory()) {
      processDirectory(inputFile);
    }
    else {
      processFile(inputFile);
    }
  }

  private void processDirectory(File directory) {
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isFile()) {
          if (file.getName().endsWith(".json")) {
            processFile(file);
          }
        }
        else if (file.isDirectory()) {
          processDirectory(file);

        }
      }
    }
  }

  private void processFile(File file) {
    BenchmarkData data = ingester.ingest(file);

    File parent = file.getParentFile();
    File qualifiedOutputDir = outputDirectory;

    if (parent != null) {
      Path relative = filename.relativize(parent.toPath());

      //        If relativize leads to "..", use current dir
      if (!relative.toString().startsWith("..")) {
        qualifiedOutputDir = new File(outputDirectory, relative.toString());
      }
    }
    
    generate(file, qualifiedOutputDir, CubeChart::new, data, RSS);
    generate(file, qualifiedOutputDir, BarChart::new, data, TIME_TO_FIRST_REQUEST);
    generate(file, qualifiedOutputDir, BarChart::new, data, THROUGHPUT);
    generate(file, qualifiedOutputDir, BarChart::new, data, BUILD_TIME);
  }

    private void generate(File file, File qualifiedOutputDir,
            TriFunction<PlotDefinition, List<Datapoint>, Config, Chart> chartConstructor,
            BenchmarkData data, PlotDefinition plotDefinition) {
        try {
            {
                File outFile = new File(qualifiedOutputDir,
                        deriveOutputFilename(file, plotDefinition, data.config().repo(), Theme.LIGHT));
                generator.generate(chartConstructor, data, plotDefinition, outFile, Theme.LIGHT);
            }
            {
                File outFile = new File(qualifiedOutputDir, deriveOutputFilename(file, plotDefinition, data.config().repo(), Theme.DARK));
                generator.generate(chartConstructor, data, plotDefinition, outFile, Theme.DARK);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (MissingDataException e) {
            if (!tolerateMissingData) {
                throw e;
            }
        }
    }

    private static String deriveOutputFilename(File file, PlotDefinition plotDefinition, Repo repo, Theme mode) {
        String filename = plotDefinition.filename().toLowerCase()
            .replaceAll(" ", "-")
            .replaceAll("\\+", "and")
            .replaceAll("\\(", "")
            .replaceAll("\\)", "");

        return (repo.scenario() != null) ?
          file.getName().replace(".json", "-%s-%s-%s.svg".formatted(repo.scenario(), filename, mode.name())) :
          file.getName().replace(".json", "-%s-%s.svg".formatted(filename, mode.name()));
    }
}
