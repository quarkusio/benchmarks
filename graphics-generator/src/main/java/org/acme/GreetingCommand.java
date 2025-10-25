package org.acme;

import jakarta.inject.Inject;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;

@Command(name = "greeting", mixinStandardHelpOptions = true)
public class GreetingCommand implements Runnable {

    @Parameters(paramLabel = "<filename>", defaultValue = "latest.json",
            description = "A filename of json-formatted data")
    String filename;

    @Inject
    DataIngester ingester;

    @Override
    public void run() {
        ingester.ingest(new File(filename));
    }

}
