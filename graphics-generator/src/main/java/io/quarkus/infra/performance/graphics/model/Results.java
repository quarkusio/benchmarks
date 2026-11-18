package io.quarkus.infra.performance.graphics.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import io.quarkus.infra.performance.graphics.MissingDataException;
import io.quarkus.infra.performance.graphics.charts.Datapoint;
import io.quarkus.infra.performance.graphics.model.units.DimensionalNumber;

public class Results {

    private final Map<Framework, Result> frameworks;

    @JsonAnySetter
    public void addFramework(String key, Result result) {
        Framework framework = Framework.valueOfIgnoreCase(key);
        frameworks.put(framework, result);
    }

    public Results() {
        this.frameworks = new LinkedHashMap<>();
    }

    private Results(Map<Framework, Result> frameworks) {
        this.frameworks = frameworks;
    }

    public int size() {
        return frameworks.size();
    }

    public Result framework(Framework type) {
        return frameworks.get(type);
    }

    public List<UnknownFramework> unknownFrameworks() {
        return frameworks.keySet().stream()
                .filter(f -> f instanceof UnknownFramework)
                .map(f -> (UnknownFramework) f)
                .collect(Collectors.toList());
    }


    public List<Datapoint> getDatasets(Function<Result, ? extends DimensionalNumber> fun) {
        // Adjust the frameworks to unqualified ones if there's not different versions of the same framework
        // There's no perfect place to do this, but this seems like a reasonable place

        if (hasOnlyOneSpringVersion()) {
            swap(KnownFramework.SPRING3_JVM, KnownFramework.SPRING_JVM);
            swap(KnownFramework.SPRING3_NATIVE, KnownFramework.SPRING_NATIVE);
            swap(KnownFramework.SPRING3_LEYDEN, KnownFramework.SPRING_LEYDEN);
            swap(KnownFramework.SPRING3_JVM_AOT, KnownFramework.SPRING_JVM_AOT);
            swap(KnownFramework.SPRING3_VIRTUAL, KnownFramework.SPRING_VIRTUAL);
            swap(KnownFramework.SPRING3_VIRTUAL_LEYDEN, KnownFramework.SPRING_VIRTUAL_LEYDEN);
            swap(KnownFramework.QUARKUS3_SPRING3_COMPAT, KnownFramework.QUARKUS3_SPRING_COMPAT);

            swap(KnownFramework.SPRING4_JVM, KnownFramework.SPRING_JVM);
            swap(KnownFramework.SPRING4_NATIVE, KnownFramework.SPRING_NATIVE);
            swap(KnownFramework.SPRING4_LEYDEN, KnownFramework.SPRING_LEYDEN);
            swap(KnownFramework.SPRING4_JVM_AOT, KnownFramework.SPRING_JVM_AOT);
            swap(KnownFramework.SPRING4_VIRTUAL, KnownFramework.SPRING_VIRTUAL);
            swap(KnownFramework.SPRING4_VIRTUAL_LEYDEN, KnownFramework.SPRING_VIRTUAL_LEYDEN);
            swap(KnownFramework.QUARKUS3_SPRING4_COMPAT, KnownFramework.QUARKUS3_SPRING_COMPAT);
        }

        // Adjust Quarkus to unqualified entries when there is no Quarkus 4 to distinguish it from.
        // Must run after the Spring-version swap above (it produces QUARKUS3_SPRING_COMPAT).
        if (!hasQuarkus4()) {
            swap(KnownFramework.QUARKUS3_JVM, KnownFramework.QUARKUS_JVM);
            swap(KnownFramework.QUARKUS3_LEYDEN, KnownFramework.QUARKUS_LEYDEN);
            swap(KnownFramework.QUARKUS3_VIRTUAL, KnownFramework.QUARKUS_VIRTUAL);
            swap(KnownFramework.QUARKUS3_VIRTUAL_LEYDEN, KnownFramework.QUARKUS_VIRTUAL_LEYDEN);
            swap(KnownFramework.QUARKUS3_NATIVE, KnownFramework.QUARKUS_NATIVE);
            swap(KnownFramework.QUARKUS3_SPRING_COMPAT, KnownFramework.QUARKUS_SPRING_COMPAT);
            swap(KnownFramework.QUARKUS3_SPRING4_COMPAT, KnownFramework.QUARKUS_SPRING4_COMPAT);
            swap(KnownFramework.QUARKUS3_SPRING3_COMPAT, KnownFramework.QUARKUS_SPRING3_COMPAT);
        }

        // Sort frameworks: known frameworks by enum order, unknown frameworks at the end in insertion order
        return frameworks.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Framework.COMPARATOR))
                .map(e -> getDatapoint(e, fun))
                .toList();

    }

    private boolean hasQuarkus4() {
        return frameworks.keySet().stream().anyMatch(f -> f.hasCategory(Category.QUARKUS4));
    }

    private boolean hasOnlyOneSpringVersion() {
        boolean hasSpring3 = frameworks.containsKey(KnownFramework.SPRING3_JVM) || frameworks.containsKey(KnownFramework.SPRING3_NATIVE)
                || frameworks.containsKey(KnownFramework.SPRING3_JVM_AOT);
        boolean hasSpring4 = frameworks.containsKey(KnownFramework.SPRING4_JVM) || frameworks.containsKey(KnownFramework.SPRING4_NATIVE)
                || frameworks.containsKey(KnownFramework.SPRING4_JVM_AOT);

        // Use xor
        return hasSpring3 ^ hasSpring4;

    }

    private void swap(Framework qualified, Framework simple) {
        if (frameworks.containsKey(qualified)) {
            frameworks.put(simple, frameworks.get(qualified));
            frameworks.remove(qualified);
        }
    }

    private static Datapoint getDatapoint(Map.Entry<? extends Framework, Result> entry,
                                          Function<Result, ? extends DimensionalNumber> fun) {
        Framework framework = entry.getKey();
        try {
            return new Datapoint(framework, fun.apply(entry.getValue()));
        } catch (NullPointerException e) {
            System.out.println("Missing data for the " + framework.getName() + " framework: " + e.getMessage());
            throw new MissingDataException(
                    "Data was missing for the " + framework.getName() + " framework: " + e.getMessage());
        }
    }


    public Results subgroup(Group group) {
        Map<Framework, Result> filtered = frameworks.entrySet()
                .stream()
                .filter(a -> a.getKey()
                        .isInGroup(group))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (existing, replacement) -> existing,
                        LinkedHashMap::new));

        return new Results(filtered);
    }
}
