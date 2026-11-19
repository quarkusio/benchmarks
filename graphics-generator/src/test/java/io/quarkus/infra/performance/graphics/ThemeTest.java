package io.quarkus.infra.performance.graphics;

import io.quarkus.infra.performance.graphics.model.Category;
import io.quarkus.infra.performance.graphics.model.KnownFramework;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ThemeTest {
    @Test
    void everyQuarkusAndCompatibilityFrameworkHasColours() {
        for (KnownFramework f : KnownFramework.values()) {
            if (f.hasCategory(Category.QUARKUS) || f.hasCategory(Category.QUARKUS4) || f.hasCategory(Category.COMPATIBILITY)) {
                assertNotNull(Theme.LIGHT.chartElements().get(f), f.getName() + " chart colour");
                assertNotNull(Theme.LIGHT.fillElements().get(f), f.getName() + " fill colour");
                assertNotNull(Theme.DARK.chartElements().get(f), f.getName() + " chart colour (dark)");
                assertNotNull(Theme.DARK.fillElements().get(f), f.getName() + " fill colour (dark)");
            }
        }
    }
}
