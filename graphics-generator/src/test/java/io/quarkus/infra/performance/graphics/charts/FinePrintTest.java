package io.quarkus.infra.performance.graphics.charts;

import java.time.Instant;
import java.time.ZoneOffset;

import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.Config;
import io.quarkus.infra.performance.graphics.model.FrameworkBuild;
import io.quarkus.infra.performance.graphics.model.GraalVM;
import io.quarkus.infra.performance.graphics.model.Group;
import io.quarkus.infra.performance.graphics.model.Jvm;
import io.quarkus.infra.performance.graphics.model.Repo;
import io.quarkus.infra.performance.graphics.model.Timing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FinePrintTest extends ElasticElementTest {

    private static final String TEXT_TAG = "\\s*</text\\s*><text[^>]*>";

    @Test
    public void quarkusVersionIsPresent() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.quarkus()).thenReturn(new FrameworkBuild("", "3.28.3"));

        FinePrint p = new FinePrint(new BenchmarkData(null, null, config));
        String s = drawSvg(p);
        assertSvgContainsText("3.28.3", s);
    }

    @Test
    public void quarkusVersionIsLabelled() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.quarkus()).thenReturn(new FrameworkBuild("", "3.28.3"));

        FinePrint p = new FinePrint(new BenchmarkData(null, null, config));
        String s = drawSvg(p);
        assertSvgContainsText("Quarkus: 3.28.3", s);
    }

    @Test
    public void springVersionIsNotPresentForQuarkusOnlyRun() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        Group group = Group.QUARKUS;
        when(config.springboot()).thenReturn(new FrameworkBuild("", "3.10.3"));

        FinePrint p = new FinePrint(new BenchmarkData(null, null, config, group));
        String s = drawSvg(p);
        assertFalse(s.contains("Spring"), s);
    }

    @Test
    public void unqualifiedSpringVersionIsLabelled() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.springboot()).thenReturn(new FrameworkBuild("", "3.10.3"));

        FinePrint p = new FinePrint(new BenchmarkData(null, null, config));
        String s = drawSvg(p);
        assertSvgContainsText("Spring Boot: 3.10.3", s);
    }


    @Test
    public void qualifiedSpringVersionIsLabelledForSpring3() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.springboot3()).thenReturn(new FrameworkBuild("", "3.10.3"));
        FinePrint p = new FinePrint(new BenchmarkData(null, null, config));
        String s = drawSvg(p);
        assertSvgContainsText("Spring Boot: 3.10.3", s);

    }

    @Test
    public void qualifiedSpringVersionIsLabelledForSpring4() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.springboot4()).thenReturn(new FrameworkBuild("", "4.10.3"));
        FinePrint p = new FinePrint(new BenchmarkData(null, null, config));
        String s = drawSvg(p);
        assertSvgContainsText("Spring Boot: 4.10.3", s);
    }

    @Test
    public void springVersionsHaveDistinctLabelsWhenMultipleArePresent() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.springboot3()).thenReturn(new FrameworkBuild("", "3.10.3"));
        when(config.springboot4()).thenReturn(new FrameworkBuild("", "4.10.3"));
        FinePrint p = new FinePrint(new BenchmarkData(null, null, config));
        String s = drawSvg(p);
        assertSvgContainsText("Spring Boot 3: 3.10.3", s);
        assertSvgContainsText("Spring Boot 4: 4.10.3", s);

    }

    @Test
    public void springVersionsDoNotHaveDistinctLabelsWhenGroupNarrowsItDown() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.springboot3()).thenReturn(new FrameworkBuild("", "3.10.3"));
        when(config.springboot4()).thenReturn(new FrameworkBuild("", "4.10.3"));
        FinePrint p = new FinePrint(new BenchmarkData(null, null, config, Group.MAIN_COMPARISON));
        String s = drawSvg(p);
        assertSvgDoesNotContainText("Spring 3: 3.10.3", s);
        assertSvgDoesNotContainText("Spring 4: 4.10.3", s);
        assertSvgContainsText("Spring Boot: 4.10.3", s);

    }

    @Test
    public void quarkusVersionsAreDistinctlyLabelledWhenQuarkus4IsPresent() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.quarkus()).thenReturn(new FrameworkBuild("", "3.28.3"));
        when(config.quarkus4()).thenReturn(new FrameworkBuild("", "4.0.1"));

        String s = drawSvg(new FinePrint(new BenchmarkData(null, null, config)));
        assertSvgContainsText("Quarkus 3: 3.28.3", s);
        assertSvgContainsText("Quarkus 4: 4.0.1", s);
    }

    @Test
    public void quarkusVersionIsUnqualifiedWhenNoQuarkus4Metadata() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.quarkus()).thenReturn(new FrameworkBuild("", "3.28.3"));

        String s = drawSvg(new FinePrint(new BenchmarkData(null, null, config)));
        assertSvgContainsText("Quarkus: 3.28.3", s);
        assertSvgDoesNotContainText("Quarkus 4", s);
    }

    @Test
    public void quarkus4VersionIsHiddenWhenGroupHasNoQuarkus4() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        when(config.quarkus()).thenReturn(new FrameworkBuild("", "3.28.3"));
        when(config.quarkus4()).thenReturn(new FrameworkBuild("", "4.0.1"));

        String s = drawSvg(new FinePrint(new BenchmarkData(null, null, config, Group.MAIN_COMPARISON)));
        assertSvgContainsText("Quarkus: 3.28.3", s);
        assertSvgDoesNotContainText("Quarkus 4", s);
    }

    @Test
    public void quarkus3FooterIsUnchangedForEveryExistingGroup() {
        for (Group group : Group.values()) {
            Config config = mock(Config.class);
            when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
            when(config.quarkus()).thenReturn(new FrameworkBuild("", "3.28.3"));

            String s = drawSvg(new FinePrint(new BenchmarkData(null, null, config, group)));
            assertSvgContainsText("Quarkus: 3.28.3", s);
            assertSvgDoesNotContainText("Quarkus 3:", s);
            assertSvgDoesNotContainText("Quarkus 4", s);
        }
    }

    @Test
    public void graalvmVersionIsPresent() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        Group group = Group.QUARKUS;
        GraalVM graal = new GraalVM("6.10.3");
        when(config.jvm()).thenReturn(new Jvm(null, null, graal, null));

        FinePrint p = new FinePrint(new BenchmarkData(null, null, config, group));
        String s = drawSvg(p);
        assertSvgContainsText("GraalVM: 6.10.3", s);
    }

    @Test
    public void graalvmVersionIsNotPresentForJVMOnlyRun() {
        Config config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));
        Group group = Group.VIRTUAL_THREADS;
        GraalVM graal = new GraalVM("6.10.3");
        when(config.jvm()).thenReturn(new Jvm(null, null, graal, null));

        FinePrint p = new FinePrint(new BenchmarkData(null, null, config, group));
        String s = drawSvg(p);
        assertSvgDoesNotContainText("GraalVM: 6.10.3", s);
    }

    @Test
    void scenarioIncluded() {
        var config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));

        var finePrint = new FinePrint(new BenchmarkData(null, null, config));
        var s = drawSvg(finePrint);

        assertSvgContainsText("Scenario: ootb", s);
    }

    @Test
    void commitIncluded() {
        var config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "1234"));

        var finePrint = new FinePrint(new BenchmarkData(null, null, config));
        var s = drawSvg(finePrint);

        assertSvgContainsText("Commit: 1234", s);
    }

    @Test
    void derivedShortCommitIncluded() {
        var config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "12345698712665655"));

        var finePrint = new FinePrint(new BenchmarkData(null, null, config));
        var s = drawSvg(finePrint);

        assertSvgContainsText("Commit: 1234569871", s);
    }

    @Test
    void shortCommitIncluded() {
        var config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "somerepo", "ootb", "12345698712665655", "1234"));

        var finePrint = new FinePrint(new BenchmarkData(null, null, config));
        var s = drawSvg(finePrint);

        assertSvgContainsText("Commit: 1234", s);
    }

    @Test
    void benchmarkDateIncluded() {
        var timing = mock(Timing.class);
        var config = mock(Config.class);
        var now = Instant.now();
        when(timing.stop()).thenReturn(now);

        var finePrint = new FinePrint(new BenchmarkData(timing, null, config));
        var s = drawSvg(finePrint);

        assertSvgContainsText("Execution date: " + now.atZone(ZoneOffset.UTC).format(FinePrint.DATE_TIME_FORMATTER), s);
    }

    @Test
    void repoUrlIncludedAndTrimmed() {
        var timing = mock(Timing.class);
        var config = mock(Config.class);
        when(config.repo()).thenReturn(new Repo("main", "https://github.com/someorg/somerepo.git", "ootb", "1234"));

        var finePrint = new FinePrint(new BenchmarkData(timing, null, config));
        var s = drawSvg(finePrint);

        // We expect a space before for the gap for the logo, and we expect the string to end with the repo name, not the .git
        assertSvgContainsText(" someorg/somerepo<", s);

        // We can't really test the hyperlink because it's injected later on by the image generator
    }

    private static void assertSvgContainsText(String text, String svg) {
        assertTrue(svg.matches("(?s).*" + text.replace(": ", ": " + TEXT_TAG) + "(?s).*"), svg);
    }

    private static void assertSvgDoesNotContainText(String text, String svg) {
        assertFalse(svg.matches("(?s).*" + text.replace(": ", ": " + TEXT_TAG) + "(?s).*"), svg);
    }

}
