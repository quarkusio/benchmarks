package io.quarkus.infra.performance.graphics;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusMainTest
public class GraphicsCommandTest {

    // We need some files to live at the root of the working directory to test some scenarios
    @BeforeAll
    public static void setupInputFiles() throws IOException, URISyntaxException {
        // Prepare some files at root level to test unqualified and default paths
        Path source = Path.of(
                GraphicsCommandTest.class.getClassLoader()
                        .getResource("data.json") // your file inside src/test/resources
                        .toURI());
        Files.copy(source, new File("./latest.json").toPath(), StandardCopyOption.REPLACE_EXISTING);
        Files.copy(source, new File("./tempfile.json").toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    @AfterAll
    public static void tidyInputFiles() throws IOException {
        Files.deleteIfExists(new File("./latest.json").toPath());
        Files.deleteIfExists(new File("./tempfile.json").toPath());

        // Tidy up generated svg files from the default-args test
        deleteDir(Path.of("tuned"));
        deleteDir(Path.of("../graphics"));
    }

    @BeforeEach
    public void setup() throws IOException {

        Path targetDir = Paths.get("target"); // adjust path if needed

        if (Files.exists(targetDir) && Files.isDirectory(targetDir)) {
            deleteSvgFiles(targetDir);
        } else {
            throw new RuntimeException("How can this be? Target directory not found: " + targetDir.toAbsolutePath());
        }

        Path outputDir = Paths.get("target/test-output"); // adjust path if needed

        if (Files.exists(outputDir) && Files.isDirectory(outputDir)) {
            deleteSvgFiles(outputDir);
        }

    }

    private static void deleteDir(Path dir) throws IOException {
        if (Files.isDirectory(dir)) {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            // Eat it and move on
                        }
                    });
        }
    }

    private static void deleteSvgFiles(Path targetDir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir, "*-for-all-light.svg")) {
            for (Path file : stream) {
                Files.deleteIfExists(file);
            }
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir, "*-for-all-dark.svg")) {
            for (Path file : stream) {
                Files.deleteIfExists(file);
            }
        }
    }

    @Test
    public void testLaunchWithNoArguments(QuarkusMainLauncher launcher) throws IOException {
        // It would be nice to suppress output but redirecting stderr doesn't suppress the stack trace
        LaunchResult result = launcher.launch();
        assertTrue(result.getOutput().contains("latest.json"), result.getOutput());
        assertEquals(0, result.exitCode());

        var tunedDir = Path.of("tuned");
        assertTrue(Files.exists(tunedDir));
        assertTrue(Files.isDirectory(tunedDir));
        assertTrue(Files.list(tunedDir).count() > 0);
    }

    @Test
    @Launch({"src/test/resources/data.json", "target/test-output/filename"})
    public void testLaunchWithFilename(LaunchResult result) {
        String output = result.getOutput();
        assertTrue(output.contains("data.json"), output);

        File image = new File("target/test-output/filename/data-tuned-throughput-for-all-light.svg");
        assertTrue(image.exists());
        assertTrue(new File("target/test-output/filename/data-tuned-throughput-for-all-light.png").exists());

        // Check groups
        image = new File("target/test-output/filename/data-tuned-throughput-for-quarkus-dark.svg");
        assertTrue(image.exists());
        assertTrue(new File("target/test-output/filename/data-tuned-throughput-for-quarkus-dark.png").exists());

        image = new File("target/test-output/filename/data-tuned-throughput-for-main-comparison-light.svg");
        assertTrue(image.exists());
        assertTrue(new File("target/test-output/filename/data-tuned-throughput-for-main-comparison-light.png").exists());

        // Check composite
        image = new File("target/test-output/filename/data-tuned-composite-for-main-comparison-light.svg");
        assertTrue(image.exists());
        assertTrue(new File("target/test-output/filename/data-tuned-composite-for-main-comparison-light.png").exists());
    }

    @Test
    @Launch({"tempfile.json", "target/test-output/filename"})
    public void testLaunchWithUnqualifiedFilename(LaunchResult result) {
        String output = result.getOutput();
        assertTrue(output.contains("tempfile.json"), output);

        File image = new File("target/test-output/filename/tempfile-tuned-throughput-for-all-light.svg");
        assertTrue(image.exists());
        assertTrue(new File("target/test-output/filename/tempfile-tuned-throughput-for-all-light.png").exists());
    }

    @Test
    @Launch({"../graphics-generator/src/test/resources/data.json", "target/test-output/filename"})
    public void testLaunchWithRelativeInputFilename(LaunchResult result) {
        String output = result.getOutput();
        assertTrue(output.contains("data.json"), output);

        File image = new File("target/test-output/filename/data-tuned-throughput-for-all-light.svg");
        assertTrue(image.exists());
        assertTrue(new File("target/test-output/filename/data-tuned-throughput-for-all-light.png").exists());
    }

    @Test
    @Launch({"tempfile.json", "../graphics/generator/target/test-output/filename"})
    public void testLaunchWithRelativeOutputPath(LaunchResult result) {
        String output = result.getOutput();
        assertTrue(output.contains("tempfile.json"), output);

        File image = new File("target/test-output/filename/data-tuned-throughput-for-all-light.svg");
        assertTrue(image.exists());
        assertTrue(new File("target/test-output/filename/data-tuned-throughput-for-all-light.png").exists());

        // Check parentheseses are stripped
        image = new File("target/test-output/filename/data-tuned-memory-rss-for-all-dark.svg");
        assertTrue(image.exists());
        assertTrue(new File("target/test-output/filename/data-tuned-memory-rss-for-all-dark.png").exists());
    }

    @Test
    @Launch({"src/test/resources", "target/test-output/directory-hyperfoil"})
    public void testLaunchWithDirectoryIgnoresHyperfoil(LaunchResult result) {
        String output = result.getOutput();
        assertFalse(output.contains("should-be-ignored.json"), "Hyperfoil JSON files should be ignored: " + output);
    }

    @Test
    @Launch({"src/test/resources", "target/test-output/directory"})
    public void testLaunchWithDirectory(LaunchResult result) {
        String output = result.getOutput();
        assertTrue(output.contains("data.json"), output);
        assertTrue(output.contains("eight-framework.json"), output);

        File dir = new File("target/test-output/directory/");
        File image1 = new File(dir, "data-tuned-throughput-for-all-light.svg");
        assertTrue(image1.exists());
        assertTrue(new File(dir, "data-tuned-throughput-for-all-light.png").exists());

        File image2 = new File(dir, "eight-framework-tuned-throughput-for-all-light.svg");
        assertTrue(image2.exists());
        assertTrue(new File(dir, "eight-framework-tuned-throughput-for-all-light.png").exists());


        File nestedDir = new File("target/test-output/directory/nested/more-nested");
        assertTrue(nestedDir.exists());

        File image3 = new File(nestedDir, "data3-ootb-throughput-for-all-light.svg");
        assertTrue(image3.exists());
        assertTrue(new File(nestedDir, "data3-ootb-throughput-for-all-light.png").exists());
    }

    @Test
    @Launch({"src/test/resources/data.json", "target/test-output/filename"})
    public void testDarkMode(LaunchResult result) {
        String output = result.getOutput();
        assertTrue(output.contains("data.json"), output);

        File image = new File("target/test-output/filename/data-tuned-throughput-for-all-dark.svg");
        assertTrue(image.exists());
        assertTrue(new File("target/test-output/filename/data-tuned-throughput-for-all-dark.png").exists());
    }

    @Test
    @Launch({"src/test/resources/data-unknown-framework.json", "target/test-output/unknown-framework"})
    public void testLaunchWithUnknownFramework(LaunchResult result) {
        String output = result.getOutput();
        assertTrue(output.contains("data-unknown-framework.json"), output);

        // Verify that images are generated successfully even with unknown frameworks
        File image = new File("target/test-output/unknown-framework/data-unknown-framework-throughput-for-all-light.svg");
        assertTrue(image.exists(), "Image should be generated for JSON with unknown framework");
        assertTrue(new File("target/test-output/unknown-framework/data-unknown-framework-throughput-for-all-light.png").exists(), "Image should be generated for JSON with unknown framework");

        // Verify dark mode image is also generated
        File darkImage = new File("target/test-output/unknown-framework/data-unknown-framework-throughput-for-all-dark.svg");
        assertTrue(darkImage.exists(), "Dark mode image should be generated for JSON with unknown framework");
        assertTrue(new File("target/test-output/unknown-framework/data-unknown-framework-throughput-for-all-dark.png").exists(), "Dark mode image should be generated for JSON with unknown framework");

        // Verify composite image is also generated
        File compositeImage = new File("target/test-output/unknown-framework/data-unknown-framework-composite-for-all-light.svg");
        assertTrue(compositeImage.exists(), "Composite image should be generated for JSON with unknown framework");
        assertTrue(new File("target/test-output/unknown-framework/data-unknown-framework-composite-for-all-light.png").exists(), "Composite image should be generated for JSON with unknown framework");
    }

    // The labelling logic is emergent, so some of it needs to be an integration test, not a unit test
    @Nested
    class LabellingTest {
        /**
         * Test that charts with both Spring 3 and Spring 4 frameworks show explicit version numbers.
         * This uses data.json which contains both spring3-* and spring4-* frameworks.
         */
        @Test
        @Launch({"src/test/resources/data.json", "target/test-output/spring-labeling"})
        public void testMultipleSpringVersionsShowExplicitVersions(LaunchResult result) throws IOException {
            String output = result.getOutput();
            assertTrue(output.contains("data.json"), output);

            // Check the "all" group which contains both Spring 3 and Spring 4
            File svgFile = new File("target/test-output/spring-labeling/data-tuned-throughput-for-all-light.svg");
            assertTrue(svgFile.exists(), "SVG file should exist");

            String svgContent = Files.readString(svgFile.toPath());

            // When both Spring 3 and Spring 4 are present, labels should include version numbers
            assertTrue(svgContent.contains("Spring Boot 3"),
                    "Chart with multiple Spring versions should show 'Spring Boot 3'");
            assertTrue(svgContent.contains("Spring Boot 4"),
                    "Chart with multiple Spring versions should show 'Spring Boot 4'");

            // Verify we don't have generic "Spring Boot" without version when both versions are present
            // This is a bit tricky because "Spring Boot 3" contains "Spring Boot", so we need to be careful
            // We'll check that the pattern doesn't appear in isolation
            assertFalse(svgContent.matches(".*Spring Boot\\s*\\n(?!\\s*[34]).*"),
                    "Should not have 'Spring Boot' without version number when multiple versions present");
        }

        /**
         * Test that charts with only Spring 4 frameworks show just "Spring" without version numbers.
         * This uses single-spring-version.json which only contains Spring 4 frameworks.
         */
        @Test
        @Launch({"src/test/resources/single-spring-version.json", "target/test-output/spring-labeling-single"})
        public void testSingleSpringVersionShowsGenericLabel(LaunchResult result) throws IOException {
            String output = result.getOutput();
            assertTrue(output.contains("single-spring-version.json"), output);

            // Check the "all" group which contains only Spring 4 frameworks
            File svgFile = new File("target/test-output/spring-labeling-single/single-spring-version-tuned-throughput-for-all-light.svg");
            assertTrue(svgFile.exists(), "SVG file should exist");

            String svgContent = Files.readString(svgFile.toPath());

            // When only one Spring version is present, labels should NOT include version numbers
            assertFalse(svgContent.contains("Spring Boot 3"),
                    "Chart with single Spring version should not show 'Spring Boot 3'");
            assertFalse(svgContent.contains("Spring Boot 4"),
                    "Chart with single Spring version should not show 'Spring Boot 4'");

            // Should have generic "Spring Boot" label instead
            assertTrue(svgContent.contains("Spring Boot"),
                    "Chart with single Spring version should show generic 'Spring Boot'");
        }

        /**
         * Test the main comparison group which only has Spring 4 (one version).
         * Since it only has one Spring version, it should show generic "Spring" labels.
         */
        @Test
        @Launch({"src/test/resources/data.json", "target/test-output/spring-labeling-main"})
        public void testMainComparisonGroupShowsGenericLabels(LaunchResult result) throws IOException {
            String output = result.getOutput();
            assertTrue(output.contains("data.json"), output);

            File svgFile = new File("target/test-output/spring-labeling-main/data-tuned-throughput-for-main-comparison-light.svg");
            assertTrue(svgFile.exists(), "SVG file should exist");

            String svgContent = Files.readString(svgFile.toPath());

            // Main comparison only has Spring 4, so should NOT show version numbers
            assertFalse(svgContent.contains("Spring Boot 3"),
                    "Main comparison chart should not show 'Spring Boot 3'");
            assertFalse(svgContent.contains("Spring Boot 4"),
                    "Main comparison chart should not show 'Spring Boot 4'");

            // Should have generic "Spring Boot" label instead
            assertTrue(svgContent.contains("Spring Boot"),
                    "Main comparison chart should show generic 'Spring Boot'");
        }

        /**
         * Test composite charts also follow the same labeling rules.
         */
        @Test
        @Launch({"src/test/resources/data.json", "target/test-output/spring-labeling-composite"})
        public void testCompositeChartsFollowLabelingRules(LaunchResult result) throws IOException {
            String output = result.getOutput();
            assertTrue(output.contains("data.json"), output);

            File svgFile = new File("target/test-output/spring-labeling-composite/data-tuned-composite-for-all-light.svg");
            assertTrue(svgFile.exists(), "SVG file should exist");

            String svgContent = Files.readString(svgFile.toPath());

            // Composite charts with multiple Spring versions should show explicit versions
            assertTrue(svgContent.contains("Spring Boot 3"),
                    "Composite chart with multiple Spring versions should show 'Spring Boot 3'");
            assertTrue(svgContent.contains("Spring Boot 4"),
                    "Composite chart with multiple Spring versions should show 'Spring Boot 4'");
        }

        /**
         * Test memory RSS charts follow the same labeling rules.
         */
        @Test
        @Launch({"src/test/resources/data.json", "target/test-output/spring-labeling-memory"})
        public void testMemoryChartsFollowLabelingRules(LaunchResult result) throws IOException {
            String output = result.getOutput();
            assertTrue(output.contains("data.json"), output);

            File svgFile = new File("target/test-output/spring-labeling-memory/data-tuned-memory-rss-for-all-light.svg");
            assertTrue(svgFile.exists(), "SVG file should exist");

            String svgContent = Files.readString(svgFile.toPath());

            // Memory charts with multiple Spring versions should show explicit versions
            assertTrue(svgContent.contains("Spring Boot 3"),
                    "Memory chart with multiple Spring versions should show 'Spring Boot 3'");
            assertTrue(svgContent.contains("Spring Boot 4"),
                    "Memory chart with multiple Spring versions should show 'Spring Boot 4'");
        }

        /**
         * Test that dark mode charts also follow the labeling rules.
         */
        @Test
        @Launch({"src/test/resources/data.json", "target/test-output/spring-labeling-dark"})
        public void testDarkModeChartsFollowLabelingRules(LaunchResult result) throws IOException {
            String output = result.getOutput();
            assertTrue(output.contains("data.json"), output);

            File svgFile = new File("target/test-output/spring-labeling-dark/data-tuned-throughput-for-all-dark.svg");
            assertTrue(svgFile.exists(), "SVG file should exist");

            String svgContent = Files.readString(svgFile.toPath());

            // Dark mode charts should follow the same rules
            assertTrue(svgContent.contains("Spring Boot 3"),
                    "Dark mode chart with multiple Spring versions should show 'Spring Boot 3'");
            assertTrue(svgContent.contains("Spring Boot 4"),
                    "Dark mode chart with multiple Spring versions should show 'Spring Boot 4'");
        }

        /**
         * Test single Spring version in dark mode shows generic label.
         */
        @Test
        @Launch({"src/test/resources/single-spring-version.json", "target/test-output/spring-labeling-single-dark"})
        public void testSingleSpringVersionInDarkModeShowsGenericLabel(LaunchResult result) throws IOException {
            String output = result.getOutput();
            assertTrue(output.contains("single-spring-version.json"), output);

            File svgFile = new File("target/test-output/spring-labeling-single-dark/single-spring-version-tuned-throughput-for-all-dark.svg");
            assertTrue(svgFile.exists(), "SVG file should exist");

            String svgContent = Files.readString(svgFile.toPath());

            // Dark mode with single Spring version should not show version numbers
            assertFalse(svgContent.contains("Spring Boot 3"),
                    "Dark mode chart with single Spring version should not show 'Spring Boot 3'");
            assertFalse(svgContent.contains("Spring Boot 4"),
                    "Dark mode chart with single Spring version should not show 'Spring Boot 4'");

            assertTrue(svgContent.contains("Spring Boot"),
                    "Dark mode chart with single Spring version should show generic 'Spring Boot'");
        }

        /**
         * Test the JAVA_AND_NATIVE_AND_LEYDEN_FRAMEWORKS group which excludes OLD category,
         * so it only has Spring 4 (not Spring 3). Should show generic "Spring" labels.
         * This corresponds to spring-quarkus-perf-comparison-latest-tuned-throughput-for-java-and-native-and-leyden-frameworks-light.png
         */
        @Test
        @Launch({"src/test/resources/data-leyden.json", "target/test-output/spring-labeling-leyden"})
        public void testJavaAndNativeAndLeydenFrameworksShowsGenericLabels(LaunchResult result) throws IOException {
            String output = result.getOutput();
            assertTrue(output.contains("data-leyden.json"), output);

            File svgFile = new File("target/test-output/spring-labeling-leyden/data-leyden-tuned-throughput-for-java-and-native-and-leyden-frameworks-light.svg");
            assertTrue(svgFile.exists(), "SVG file should exist");

            String svgContent = Files.readString(svgFile.toPath());

            // JAVA_AND_NATIVE_AND_LEYDEN_FRAMEWORKS group excludes OLD, so only Spring 4 is present
            // Should NOT show version numbers
            assertFalse(svgContent.contains("Spring Boot 3"),
                    "JAVA_AND_NATIVE_AND_LEYDEN_FRAMEWORKS chart should not show 'Spring Boot 3'");
            assertFalse(svgContent.contains("Spring Boot 4"),
                    "JAVA_AND_NATIVE_AND_LEYDEN_FRAMEWORKS chart should not show 'Spring Boot 4'");

            // Should have generic "Spring Boot" label instead
            assertTrue(svgContent.contains("Spring Boot"),
                    "JAVA_AND_NATIVE_AND_LEYDEN_FRAMEWORKS chart should show generic 'Spring Boot'");
        }
    }

}

