package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.acme.model.BenchmarkData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class ImageGeneratorTest {

    @Inject
    ImageGenerator imageGenerator;

    @BeforeEach
    public void setup() throws IOException {


        Path targetDir = Paths.get("target"); // adjust path if needed

        if (Files.exists(targetDir) && Files.isDirectory(targetDir)) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(targetDir, "*.svg")) {
                for (Path file : stream) {
                    Files.deleteIfExists(file);
                }
            }
        } else {
            throw new RuntimeException("How can this be? Target directory not found: " + targetDir.toAbsolutePath());
        }
    }


    @Test
    public void testGeneration() throws IOException {
        BenchmarkData data = new BenchmarkData();
        imageGenerator.generate(data, new File("target/images/test1.svg"));
        File image = new File("target/images/test1.svg");

        assertTrue(image.exists());
        assertTrue(image.length() > 100);
    }

}

