package org.acme;

import jakarta.inject.Inject;
import org.acme.model.BenchmarkData;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;

@Command(name = "greeting", mixinStandardHelpOptions = true)
public class GreetingCommand implements Runnable {

    @Parameters(paramLabel = "<filename>", defaultValue = "latest.json",
            description = "A filename of json-formatted data")
    String filename;

    @Parameters(paramLabel = "<outputDir>", defaultValue = ".",
            description = "The directory for generated images")
    String outputDirectory;

    @Inject
    DataIngester ingester;

    @Inject
    ImageGenerator generator;

    @Override
    public void run() {
        File file = new File(filename);
        BenchmarkData data = ingester.ingest(file);
        File outFile = new File(outputDirectory, file.getName().replace(".json", ".svg"));
        try {
            generator.generate(data, outFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
