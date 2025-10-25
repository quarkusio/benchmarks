package org.acme;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusMainTest
public class GreetingCommandTest {

    @Test
    public void testLaunchWithNoArguments(QuarkusMainLauncher launcher) {
        LaunchResult result = launcher.launch();
        assertTrue(result.getOutput().contains("latest.json"), result.getOutput());
        assertEquals(result.exitCode(), 1);
    }

    @Test
    @Launch({"src/test/resources/data.json"})
    public void testLaunchWithFilename(LaunchResult result) {
        String output = result.getOutput();
        assertTrue(output.contains("data.json"), output);
    }

}
