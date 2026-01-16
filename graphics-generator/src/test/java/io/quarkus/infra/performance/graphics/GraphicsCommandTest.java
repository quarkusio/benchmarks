package io.quarkus.infra.performance.graphics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainLauncher;
import io.quarkus.test.junit.main.QuarkusMainTest;

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
            }
            catch (IOException e) {
              // Eat it and move on
            }
          });
        }
    }

    private static void deleteSvgFiles(Path targetDir) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir, "*-light.svg")) {
            for (Path file : stream) {
                Files.deleteIfExists(file);
            }
        }
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir, "*-dark.svg")) {
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
    @Launch({ "src/test/resources/data.json", "target/test-output/filename" })
    public void testLaunchWithFilename(LaunchResult result) {
        String output = result.getOutput();
        assertTrue(output.contains("data.json"), output);

        File image = new File("target/test-output/filename/data-tuned-throughput-light.svg");
        assertTrue(image.exists());
    }

    @Test
    @Launch({ "tempfile.json", "target/test-output/filename" })
    public void testLaunchWithUnqualifiedFilename(LaunchResult result) {
        String output = result.getOutput();
        assertTrue(output.contains("tempfile.json"), output);

        File image = new File("target/test-output/filename/tempfile-tuned-throughput-light.svg");
        assertTrue(image.exists());
    }

    @Test
    @Launch({ "../graphics-generator/src/test/resources/data.json", "target/test-output/filename" })
    public void testLaunchWithRelativeInputFilename(LaunchResult result) {
        String output = result.getOutput();
        assertTrue(output.contains("data.json"), output);

        File image = new File("target/test-output/filename/data-tuned-throughput-light.svg");
        assertTrue(image.exists());
    }

    @Test
    @Launch({ "tempfile.json", "../graphics/generator/target/test-output/filename" })
    public void testLaunchWithRelativeOutputPath(LaunchResult result) {
        String output = result.getOutput();
        assertTrue(output.contains("tempfile.json"), output);

        File image = new File("target/test-output/filename/data-tuned-throughput-light.svg");
        assertTrue(image.exists());

        // Check parentheseses are stripped
        image = new File("target/test-output/filename/data-tuned-memory-rss-dark.svg");
        assertTrue(image.exists());
    }

    @Test
    @Launch({ "src/test/resources", "target/test-output/directory" })
    public void testLaunchWithDirectory(LaunchResult result) {
        String output = result.getOutput();
        assertTrue(output.contains("data.json"), output);
        assertTrue(output.contains("eight-framework.json"), output);

        File dir = new File("target/test-output/directory/");
        File image1 = new File(dir, "data-tuned-throughput-light.svg");
        assertTrue(image1.exists());
        File image2 = new File(dir, "eight-framework-tuned-throughput-light.svg");
        assertTrue(image2.exists());

        File nestedDir = new File("target/test-output/directory/nested/more-nested");
        assertTrue(nestedDir.exists());
        File image3 = new File(nestedDir, "data3-ootb-throughput-light.svg");
        assertTrue(image3.exists());
    }

    @Test
    @Launch({ "src/test/resources/data.json", "target/test-output/filename" })
    public void testDarkMode(LaunchResult result) {
        String output = result.getOutput();
        assertTrue(output.contains("data.json"), output);

        File image = new File("target/test-output/filename/data-tuned-throughput-dark.svg");
        assertTrue(image.exists());
    }

}
