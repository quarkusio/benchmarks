package io.quarkus.infra.performance.graphics.charts;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;

import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.Config;
import io.quarkus.infra.performance.graphics.model.FrameworkBuild;
import io.quarkus.infra.performance.graphics.model.Repo;
import io.quarkus.infra.performance.graphics.model.Timing;

class FinePrintTest extends ElasticElementTest {

    @Test
    public void quarkusVersionIsPresent() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.quarkus()).thenReturn(new FrameworkBuild("", "3.28.3"));

        FinePrint p = new FinePrint(new BenchmarkData(null, null, config));
        String s = drawSvg(p);
        assertTrue(s.contains("3.28.3"), s);
    }

    @Test
    public void quarkusVersionIsLabelled() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.quarkus()).thenReturn(new FrameworkBuild("", "3.28.3"));

        FinePrint p = new FinePrint(new BenchmarkData(null, null, config));
        String s = drawSvg(p);
        assertTrue(s.contains("Quarkus: 3.28.3"), s);
    }

    @Test
    public void unqualifiedSpringVersionIsLabelled() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.springboot()).thenReturn(new FrameworkBuild("", "3.10.3"));

        FinePrint p = new FinePrint(new BenchmarkData(null, null, config));
        String s = drawSvg(p);
        assertTrue(s.contains("Spring: 3.10.3"), s);
    }

    @Test
    public void qualifiedSpringVersionIsLabelledForSpring3() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.springboot3()).thenReturn(new FrameworkBuild("", "3.10.3"));
        FinePrint p = new FinePrint(new BenchmarkData(null, null, config));
        String s = drawSvg(p);
        assertTrue(s.contains("Spring: 3.10.3"), s);

    }

    @Test
    public void qualifiedSpringVersionIsLabelledForSpring4() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.springboot4()).thenReturn(new FrameworkBuild("", "4.10.3"));
        FinePrint p = new FinePrint(new BenchmarkData(null, null, config));
        String s = drawSvg(p);
        assertTrue(s.contains("Spring: 4.10.3"), s);
    }

    @Test
    public void springVersionsHaveDistinctLabelsWhenMultipleArePresent() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.springboot3()).thenReturn(new FrameworkBuild("", "3.10.3"));
        when(config.springboot4()).thenReturn(new FrameworkBuild("", "4.10.3"));
        FinePrint p = new FinePrint(new BenchmarkData(null, null, config));
        String s = drawSvg(p);
        assertTrue(s.contains("Spring 3: 3.10.3"), s);
        assertTrue(s.contains("Spring 4: 4.10.3"), s);

    }

    @Test
    void scenarioIncluded() {
      var config = mock(Config.class);
      when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));

      var finePrint = new FinePrint(new BenchmarkData(null, null, config));
      var s = drawSvg(finePrint);

      assertTrue(s.contains("Scenario: ootb"));
    }

    @Test
    void commitIncluded() {
      var config = mock(Config.class);
      when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));

      var finePrint = new FinePrint(new BenchmarkData(null, null, config));
      var s = drawSvg(finePrint);

      assertTrue(s.contains("Commit: 1234"));
    }

    @Test
    void derivedShortCommitIncluded() {
      var config = mock(Config.class);
      when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "12345698712665655"));

      var finePrint = new FinePrint(new BenchmarkData(null, null, config));
      var s = drawSvg(finePrint);

      assertTrue(s.contains("Commit: 1234569871"));
    }

    @Test
    void shortCommitIncluded() {
      var config = mock(Config.class);
      when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "12345698712665655", "1234"));

      var finePrint = new FinePrint(new BenchmarkData(null, null, config));
      var s = drawSvg(finePrint);

      assertTrue(s.contains("Commit: 1234"));
    }

    @Test
    void benchmarkDateIncluded() {
      var timing = mock(Timing.class);
      var config = mock(Config.class);
      var now = Instant.now();
      when(timing.stop()).thenReturn(now);

      var finePrint = new FinePrint(new BenchmarkData(timing, null, config));
      var s = drawSvg(finePrint);

      assertTrue(s.contains("Execution date: " + now.atZone(ZoneOffset.UTC).format(FinePrint.DATE_TIME_FORMATTER)));
    }
}
