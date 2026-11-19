package io.quarkus.infra.performance.graphics.model;

import java.util.List;

import io.quarkus.infra.performance.graphics.MissingDataException;
import io.quarkus.infra.performance.graphics.charts.Datapoint;
import io.quarkus.infra.performance.graphics.model.units.TransactionsPerSecond;
import org.junit.jupiter.api.Test;

import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS_NATIVE;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS_VIRTUAL;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS_VIRTUAL_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS_SPRING_COMPAT;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS_SPRING3_COMPAT;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS_SPRING4_COMPAT;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_NATIVE;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_SPRING3_COMPAT;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_SPRING4_COMPAT;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_SPRING_COMPAT;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_VIRTUAL;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS3_VIRTUAL_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS4_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.QUARKUS4_SPRING4_COMPAT;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING3_VIRTUAL_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING4_NATIVE;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING_JVM;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING_LEYDEN;
import static io.quarkus.infra.performance.graphics.model.KnownFramework.SPRING_VIRTUAL_LEYDEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ResultsTest {
    @Test
    void getDatasets() {
        Results results = new Results();
        addDatapoint(results, QUARKUS3_JVM, 589.21);
        addDatapoint(results, SPRING3_JVM, 467.87);

        List<Datapoint> datapoints = results.getDatasets(f -> f.load().avThroughput());
        assertEquals(2, datapoints.size());
        assertEquals(589.21, datapoints.get(0).value().getValue());
        assertEquals(QUARKUS_JVM, datapoints.get(0).framework());
        assertEquals(467.87, datapoints.get(1).value().getValue());
    }

    @Test
    void getDatasetsAdjustsFrameworksWhenOnlyOnePresent() {
        Results results = new Results();
        addDatapoint(results, QUARKUS3_JVM, 589.21);
        addDatapoint(results, SPRING3_JVM, 467.87);
        addDatapoint(results, SPRING3_LEYDEN, 467.87);
        addDatapoint(results, SPRING3_VIRTUAL_LEYDEN, 467.87);

        List<Datapoint> datapoints = results.getDatasets(f -> f.load().avThroughput());
        assertEquals(4, datapoints.size());
        assertEquals(589.21, datapoints.get(0).value().getValue());
        // The second framework should be a synthetic one to reflect the fact there's only one Spring version
        assertEquals(SPRING_JVM, datapoints.get(1).framework());
        assertEquals(467.87, datapoints.get(1).value().getValue());
        assertEquals(SPRING_LEYDEN, datapoints.get(2).framework());
        assertEquals(SPRING_VIRTUAL_LEYDEN, datapoints.get(3).framework());

    }

    @Test
    void getDatasetsDoesNotAdjustFrameworksWhenMultiplesPresent() {
        Results results = new Results();
        addDatapoint(results, SPRING4_JVM, 42.1);
        addDatapoint(results, QUARKUS3_JVM, 589.21);
        addDatapoint(results, SPRING3_JVM, 467.87);

        List<Datapoint> datapoints = results.getDatasets(f -> f.load().avThroughput());
        assertEquals(3, datapoints.size());
        assertEquals(589.21, datapoints.get(0).value().getValue());
        assertEquals(QUARKUS_JVM, datapoints.get(0).framework());

        assertEquals(SPRING4_JVM, datapoints.get(1).framework());
        assertEquals(42.1, datapoints.get(1).value().getValue());

        assertEquals(SPRING3_JVM, datapoints.get(2).framework());
        assertEquals(467.87, datapoints.get(2).value().getValue());

    }

    @Test
    void getDatasetsForMissingData() {
        Results results = new Results();
        addDatapoint(results, SPRING3_JVM, 589.21);

        Exception exception = assertThrows(MissingDataException.class,
                () -> results.getDatasets(f -> f.rss().avFirstRequestRss()));
        assertTrue(exception.getMessage().contains("Rss"), exception.getMessage());
        assertTrue(exception.getMessage().contains("pring"), exception.getMessage());

    }

    @Test
    void subgroup() {
        Results results = new Results();
        addDatapoint(results, QUARKUS3_JVM, 589.21);
        addDatapoint(results, SPRING3_JVM, 467.87);
        addDatapoint(results, SPRING4_JVM, 467.87);

        Results subgroup = results.subgroup(Group.MAIN_COMPARISON);
        List<Datapoint> datapoints = subgroup.getDatasets(f -> f.load().avThroughput());
        assertEquals(2, datapoints.size());
        assertEquals(QUARKUS_JVM, datapoints.get(0).framework());
        assertEquals(SPRING_JVM, datapoints.get(1).framework());
    }

    @Test
    void quarkus3OnlyVariantsAreUnqualifiedAndKeepOrder() {
        Results results = new Results();
        addDatapoint(results, QUARKUS3_NATIVE, 1.0);
        addDatapoint(results, SPRING4_JVM, 2.0);
        addDatapoint(results, QUARKUS3_VIRTUAL_LEYDEN, 3.0);
        addDatapoint(results, QUARKUS3_VIRTUAL, 4.0);
        addDatapoint(results, QUARKUS3_LEYDEN, 5.0);
        addDatapoint(results, QUARKUS3_JVM, 6.0);

        List<Framework> frameworks = results.getDatasets(f -> f.load().avThroughput()).stream().map(Datapoint::framework).toList();
        assertEquals(List.of(QUARKUS_JVM, SPRING_JVM, QUARKUS_LEYDEN, QUARKUS_VIRTUAL, QUARKUS_VIRTUAL_LEYDEN, QUARKUS_NATIVE), frameworks);
    }

    @Test
    void quarkus3CompatEntriesAreUnqualifiedWithoutQuarkus4() {
        Results one = new Results();
        addDatapoint(one, QUARKUS3_SPRING4_COMPAT, 1.0);
        addDatapoint(one, SPRING4_JVM, 2.0);
        assertEquals(List.of(SPRING_JVM, QUARKUS_SPRING_COMPAT),
                one.getDatasets(f -> f.load().avThroughput()).stream().map(Datapoint::framework).toList());

        Results both = new Results();
        addDatapoint(both, QUARKUS3_SPRING4_COMPAT, 1.0);
        addDatapoint(both, QUARKUS3_SPRING3_COMPAT, 1.5);
        addDatapoint(both, SPRING4_JVM, 2.0);
        addDatapoint(both, SPRING3_JVM, 3.0);
        List<Framework> frameworks = both.getDatasets(f -> f.load().avThroughput()).stream().map(Datapoint::framework).toList();
        assertTrue(frameworks.contains(QUARKUS_SPRING4_COMPAT));
        assertTrue(frameworks.contains(QUARKUS_SPRING3_COMPAT));
    }

    @Test
    void springOnlyDataIsUnchanged() {
        Results both = new Results();
        addDatapoint(both, SPRING3_JVM, 1.0);
        addDatapoint(both, SPRING4_JVM, 2.0);
        assertEquals(List.of(SPRING4_JVM, SPRING3_JVM),
                both.getDatasets(f -> f.load().avThroughput()).stream().map(Datapoint::framework).toList());

        Results one = new Results();
        addDatapoint(one, SPRING4_JVM, 2.0);
        addDatapoint(one, SPRING4_NATIVE, 2.0);
        assertEquals(List.of(SPRING_JVM, KnownFramework.SPRING_NATIVE),
                one.getDatasets(f -> f.load().avThroughput()).stream().map(Datapoint::framework).toList());
    }

    @Test
    void quarkus3StaysQualifiedWhenQuarkus4IsPresent() {
        Results results = new Results();
        addDatapoint(results, QUARKUS3_JVM, 500.0);
        addDatapoint(results, QUARKUS4_JVM, 600.0);
        addDatapoint(results, SPRING4_JVM, 400.0);

        List<Datapoint> datapoints = results.getDatasets(f -> f.load().avThroughput());
        assertEquals(3, datapoints.size());
        assertEquals(QUARKUS4_JVM, datapoints.get(0).framework());
        assertEquals(QUARKUS3_JVM, datapoints.get(1).framework());
        assertEquals(500.0, datapoints.get(1).value().getValue());
        assertEquals(SPRING_JVM, datapoints.get(2).framework());
    }

    @Test
    void quarkus3IsUnqualifiedWithoutQuarkus4() {
        Results results = new Results();
        addDatapoint(results, QUARKUS3_JVM, 500.0);
        addDatapoint(results, SPRING4_JVM, 400.0);

        assertEquals(QUARKUS_JVM, results.getDatasets(f -> f.load().avThroughput()).get(0).framework());
    }

    @Test
    void quarkus4OnlyIsSwappedToUnqualifiedEntries() {
        Results results = new Results();
        addDatapoint(results, QUARKUS4_JVM, 600.0);
        addDatapoint(results, QUARKUS4_SPRING4_COMPAT, 500.0);

        assertEquals(List.of(QUARKUS_JVM, QUARKUS_SPRING4_COMPAT),
                results.getDatasets(f -> f.load().avThroughput()).stream().map(Datapoint::framework).toList());
    }

    @Test
    void quarkus4OnlySingleDatapointIsUnqualified() {
        Results results = new Results();
        addDatapoint(results, QUARKUS4_JVM, 600.0);

        assertEquals(QUARKUS_JVM, results.getDatasets(f -> f.load().avThroughput()).get(0).framework());
    }

    @Test
    void quarkus3CompatStaysQualifiedAfterTheSpringSwapWhenQuarkus4IsPresent() {
        Results results = new Results();
        addDatapoint(results, QUARKUS4_SPRING4_COMPAT, 600.0);
        addDatapoint(results, QUARKUS3_SPRING4_COMPAT, 500.0);
        addDatapoint(results, SPRING4_JVM, 400.0); // only one Spring version -> Spring swap fires first

        // Compat entries sort after the Spring entries in enum order
        assertEquals(List.of(SPRING_JVM, QUARKUS4_SPRING4_COMPAT, QUARKUS3_SPRING_COMPAT),
                results.getDatasets(f -> f.load().avThroughput()).stream().map(Datapoint::framework).toList());
    }

    @Test
    void qualifiedQuarkus3CompatEntriesStayQualifiedWhenBothSpringVersionsAndQuarkus4ArePresent() {
        Results results = new Results();
        addDatapoint(results, QUARKUS4_SPRING4_COMPAT, 600.0);
        addDatapoint(results, QUARKUS3_SPRING4_COMPAT, 500.0);
        addDatapoint(results, QUARKUS3_SPRING3_COMPAT, 450.0);
        addDatapoint(results, SPRING4_JVM, 400.0);
        addDatapoint(results, SPRING3_JVM, 390.0); // both Spring versions -> no Spring swap

        List<Framework> frameworks = results.getDatasets(f -> f.load().avThroughput()).stream().map(Datapoint::framework).toList();
        assertTrue(frameworks.contains(QUARKUS4_SPRING4_COMPAT));
        assertTrue(frameworks.contains(QUARKUS3_SPRING4_COMPAT));
        assertTrue(frameworks.contains(QUARKUS3_SPRING3_COMPAT));
        assertFalse(frameworks.contains(QUARKUS_SPRING4_COMPAT));
        assertFalse(frameworks.contains(QUARKUS_SPRING3_COMPAT));
    }

    private static void addDatapoint(Results results, KnownFramework framework, Double throughput) {
        Result result = mock(Result.class);
        results.addFramework(framework.getName(), result);
        Load load = mock(Load.class);
        when(result.load()).thenReturn(load);
        when(load.avThroughput()).thenReturn(new TransactionsPerSecond(throughput));
    }

}
