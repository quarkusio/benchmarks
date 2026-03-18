package io.quarkus.infra.performance.graphics.model.units;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonCreator;

public class Memory extends DimensionalNumber {
    private final String units;
    private static final Pattern UNIT_PATTERN = Pattern.compile("([0-9]+)([A-z]*)");

    @JsonCreator
    public Memory(String memWithUnits) {
        super(extractNumber(memWithUnits));
        this.units = extractUnits(memWithUnits);
    }

    @JsonCreator
    public Memory(double mem) {
        super(mem);
        this.units = "MiB";
    }

    @Override
    public String getUnits() {
        return units;
    }

    private static double extractNumber(String s) {
        Matcher m = UNIT_PATTERN.matcher(s.trim());
        return m.matches() ? Double.parseDouble(m.group(1)) : 0;
    }

    private static String extractUnits(String s) {
        Matcher m = UNIT_PATTERN.matcher(s.trim());
        return m.matches() ? m.group(2) : "MiB";
    }
}
