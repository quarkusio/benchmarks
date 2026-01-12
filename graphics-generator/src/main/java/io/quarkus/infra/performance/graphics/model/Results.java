package io.quarkus.infra.performance.graphics.model;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import io.quarkus.infra.performance.graphics.MissingDataException;
import io.quarkus.infra.performance.graphics.charts.Datapoint;
import io.quarkus.infra.performance.graphics.model.units.DimensionalNumber;

public class Results {

    private final Map<Framework, Result> frameworks = new EnumMap<>(Framework.class);

    @JsonAnySetter
    public void addFramework(String key, Result result) {
        try {
            Framework type = Framework.valueOfIgnoreCase(key);
            frameworks.put(type, result);
        } catch (IllegalArgumentException e) {
            // Optionally log or ignore unknown frameworks
        }
    }

    public Result framework(Framework type) {
        return frameworks.get(type);
    }

    public List<Datapoint> getDatasets(Function<Result, ? extends DimensionalNumber> fun) {
        // Adjust the frameworks to unqualified ones if there's not different versions of the same framework
        // There's no perfect place to do this, but this seems like a reasonable place

        if (hasOnlyOneSpringVersion(frameworks)) {
            swap(Framework.SPRING3_JVM, Framework.SPRING_JVM);
            swap(Framework.SPRING3_NATIVE, Framework.SPRING_NATIVE);
            swap(Framework.SPRING3_JVM_AOT, Framework.SPRING_JVM_AOT);
            swap(Framework.SPRING3_VIRTUAL, Framework.SPRING_VIRTUAL);
            swap(Framework.QUARKUS3_SPRING3_COMPAT, Framework.QUARKUS3_SPRING_COMPAT);

            swap(Framework.SPRING4_JVM, Framework.SPRING_JVM);
            swap(Framework.SPRING4_NATIVE, Framework.SPRING_NATIVE);
            swap(Framework.SPRING4_JVM_AOT, Framework.SPRING_JVM_AOT);
            swap(Framework.SPRING4_VIRTUAL, Framework.SPRING_VIRTUAL);
            swap(Framework.QUARKUS3_SPRING4_COMPAT, Framework.QUARKUS3_SPRING_COMPAT);
        }

        return frameworks.entrySet().stream().map(e -> getDatapoint(e, fun))
                .toList();

    }

    private boolean hasOnlyOneSpringVersion(Map<Framework, Result> frameworks) {
        boolean hasSpring3 = frameworks.containsKey(Framework.SPRING3_JVM) || frameworks.containsKey(Framework.SPRING3_NATIVE)
                || frameworks.containsKey(Framework.SPRING3_JVM_AOT);
        boolean hasSpring4 = frameworks.containsKey(Framework.SPRING4_JVM) || frameworks.containsKey(Framework.SPRING4_NATIVE)
                || frameworks.containsKey(Framework.SPRING4_JVM_AOT);

        // Use xor
        return hasSpring3 ^ hasSpring4;

    }

    private void swap(Framework qualified, Framework simple) {
        if (frameworks.containsKey(qualified)) {
            frameworks.put(simple, frameworks.get(qualified));
            frameworks.remove(qualified);
        }
    }

    private static Datapoint getDatapoint(Map.Entry<Framework, Result> entry,
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
}
