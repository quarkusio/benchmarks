# Benchmarks

This repository hosts results and generated graphics of benchmark executions from the Quarkus performance lab, along with the graphics code for generating charts.

## To generate your own charts 

Assuming you have a file called `myfile.json` in the expected data format, you can run:
```
git clone git@github.com:quarkusio/benchmarks.git # this repo!
mvn -f benchmarks/graphics-generator verify -DskipTests
mkdir -p images
java -jar benchmarks/graphics-generator/target/quarkus-app/quarkus-run.jar myfile.json images true
```

This will generate a set of plots in an `images` directory. 

## Results 

### spring-quarkus-perf-comparison

Benchmark source code: https://github.com/quarkusio/spring-quarkus-perf-comparison

#### Tuned
![](images/spring-quarkus-perf-comparison/tuned/results-latest-tuned-throughput-light.svg#gh-light-mode-only)
![](images/spring-quarkus-perf-comparison/tuned/results-latest-tuned-throughput-dark.svg#gh-dark-mode-only)

![](images/spring-quarkus-perf-comparison/tuned/results-latest-tuned-boot-and-first-response-time-light.svg#gh-light-mode-only)
![](images/spring-quarkus-perf-comparison/tuned/results-latest-tuned-boot-and-first-response-time-dark.svg#gh-dark-mode-only)

![](images/spring-quarkus-perf-comparison/tuned/results-latest-tuned-memory-rss-light.svg#gh-light-mode-only)
![](images/spring-quarkus-perf-comparison/tuned/results-latest-tuned-memory-rss-dark.svg#gh-dark-mode-only)

![](images/spring-quarkus-perf-comparison/tuned/results-latest-tuned-build-duration-light.svg#gh-light-mode-only)
![](images/spring-quarkus-perf-comparison/tuned/results-latest-tuned-build-duration-dark.svg#gh-dark-mode-only)

#### Out of the box
![](images/spring-quarkus-perf-comparison/ootb/results-latest-ootb-throughput-light.svg#gh-light-mode-only)
![](images/spring-quarkus-perf-comparison/ootb/results-latest-ootb-throughput-dark.svg#gh-dark-mode-only)

![](images/spring-quarkus-perf-comparison/ootb/results-latest-ootb-boot-and-first-response-time-light.svg#gh-light-mode-only)
![](images/spring-quarkus-perf-comparison/ootb/results-latest-ootb-boot-and-first-response-time-dark.svg#gh-dark-mode-only)

![](images/spring-quarkus-perf-comparison/ootb/results-latest-ootb-memory-rss-light.svg#gh-light-mode-only)
![](images/spring-quarkus-perf-comparison/ootb/results-latest-ootb-memory-rss-dark.svg#gh-dark-mode-only)

![](images/spring-quarkus-perf-comparison/ootb/results-latest-ootb-build-duration-light.svg#gh-light-mode-only)
![](images/spring-quarkus-perf-comparison/ootb/results-latest-ootb-build-duration-dark.svg#gh-dark-mode-only)
