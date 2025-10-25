package org.acme.model;


import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.util.EnumMap;
import java.util.Map;

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
}
