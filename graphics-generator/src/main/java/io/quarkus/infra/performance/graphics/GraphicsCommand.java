package io.quarkus.infra.performance.graphics;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import jakarta.inject.Inject;

import io.quarkus.infra.performance.graphics.charts.BarChart;
import io.quarkus.infra.performance.graphics.charts.Chart;
import io.quarkus.infra.performance.graphics.charts.CompositeChart;
import io.quarkus.infra.performance.graphics.charts.CubeChart;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.CompositePlotDefinition;
import io.quarkus.infra.performance.graphics.model.Config;
import io.quarkus.infra.performance.graphics.model.Group;
import io.quarkus.infra.performance.graphics.model.Repo;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "graphics", mixinStandardHelpOptions = true)
public class GraphicsCommand implements Runnable {
    private static final PlotDefinition THROUGHPUT = new SingleSeriesPlotDefinition("Throughput", "(Higher is better)", framework -> framework.load().avThroughput());
    private static final PlotDefinition RSS = new SingleSeriesPlotDefinition("Memory (RSS after first request)", "memory-rss", "(Smaller is better)", framework -> framework.rss().avFirstRequestRss());
    private static final PlotDefinition TIME_TO_FIRST_REQUEST = new SingleSeriesPlotDefinition("Boot + First Response Time", "(Shorter is better)", framework -> framework.startup().avStartTime());
    private static final PlotDefinition BUILD_TIME = new SingleSeriesPlotDefinition("Build Duration", "(Shorter is better)", framework -> framework.build().avBuildTime());
    private static final PlotDefinition FRONT_PAGE = new CompositePlotDefinition("composite", List.of(TIME_TO_FIRST_REQUEST, THROUGHPUT, RSS));

    @Parameters(paramLabel = "<filename>", defaultValue = "latest.json", description = "A filename of json-formatted data, or a directory. For directories, .json files in the directory will be processed recursively.")
    Path filename;

    @Parameters(paramLabel = "<outputDir>", defaultValue = ".", description = "The directory for generated images")
    Path outputDirectory;

    @Parameters(paramLabel = "<tolerateMissingData>", defaultValue = "true", description = "Whether to suppress exceptions caused by missing data")
    boolean tolerateMissingData;

    @Inject
    DataIngester ingester;

    @Inject
    ImageGenerator generator;

    @Override
    public void run() {
        try {
            processPath(filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void processPath(Path inputFile) throws IOException {
        if (Files.isDirectory(inputFile)) {
            processDirectory(inputFile);
        } else {
            processFile(inputFile);
        }
    }

    private void processDirectory(Path directory) throws IOException {
        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        return Optional.ofNullable(dir.getFileName())
                          .map(Path::toString)
                          .filter("hyperfoil"::equalsIgnoreCase)
                          .map(name -> FileVisitResult.SKIP_SUBTREE)
                          .orElse(FileVisitResult.CONTINUE);
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        if (file.getFileName().toString().endsWith(".json")) {
                            processFile(file);
                        }

                        return FileVisitResult.CONTINUE;
                    }
                }
        );
    }

    private void processFile(Path file) {
        if (Files.isRegularFile(file)) {
            var data = ingester.ingest(file);
            var parent = file.getParent();
            var qualifiedOutputDir = outputDirectory;

            if (parent != null) {
                var relative = filename.relativize(parent);

                //        If relativize leads to "..", use current dir
                if (! relative.toString().startsWith("..")) {
                    qualifiedOutputDir = outputDirectory.resolve(relative);
                }
            }

            if (file.getFileName().toString().contains("latest")) {
                // https://github.com/quarkusio/benchmarks/issues/203
                // If the file is a "latest" file, then we want to sub-directory it
                var scenario = Optional.ofNullable(data.config())
                        .map(Config::repo)
                        .map(Repo::scenario)
                        .orElse(".");

                qualifiedOutputDir = qualifiedOutputDir.resolve(scenario).normalize();
            }

            generate(file, qualifiedOutputDir, CubeChart::new, data, RSS);
            generate(file, qualifiedOutputDir, BarChart::new, data, TIME_TO_FIRST_REQUEST);
            generate(file, qualifiedOutputDir, BarChart::new, data, THROUGHPUT);
            generate(file, qualifiedOutputDir, BarChart::new, data, BUILD_TIME);
            generate(file, qualifiedOutputDir, CompositeChart::new, data, FRONT_PAGE);
        }
    }

    private void generate(Path file, Path qualifiedOutputDir, BiFunction<PlotDefinition, BenchmarkData, Chart> chartConstructor, BenchmarkData allData, PlotDefinition plotDefinition) {
        for (Group group : Group.values()) {
            BenchmarkData data = allData.subgroup(group);
            if (data.results() != null && data.results().size() > 0) {
                try {
                    var lightFile = qualifiedOutputDir.resolve(deriveOutputFilename(file, plotDefinition, data, Theme.LIGHT));
                    generator.generate(chartConstructor, data, plotDefinition, lightFile, Theme.LIGHT);

                    var darkFile = qualifiedOutputDir.resolve(deriveOutputFilename(file, plotDefinition, data, Theme.DARK));
                    generator.generate(chartConstructor, data, plotDefinition, darkFile, Theme.DARK);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (MissingDataException e) {
                    if (! tolerateMissingData) {
                        throw e;
                    }
                }
            }
        }
    }

    private static String deriveOutputFilename(Path file, PlotDefinition plotDefinition, BenchmarkData data, Theme mode) {
        var filename = plotDefinition.filename().toLowerCase()
                .replaceAll(" ", "-")
                .replaceAll("\\+", "and")
                .replaceAll("\\(", "")
                .replaceAll("\\)", "");

        var currentFilename = file.getFileName().toString();
        String group = data.group().toString().toLowerCase().replaceAll("_", "-");

        return Optional.ofNullable(data.config().repo())
                .map(repo -> repo.scenario())
                .filter(scenario -> ! currentFilename.contains(scenario))
                .map(scenario -> currentFilename.replace(".json", "-%s-%s-for-%s-%s.svg".formatted(scenario, filename, group, mode.name())))
                .orElseGet(() -> currentFilename.replace(".json", "-%s-for-%s-%s.svg".formatted(filename, group, mode.name())));
    }
}
