package org.acme;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.model.BenchmarkData;

import java.io.File;
import java.io.IOException;

@ApplicationScoped
public class DataIngester {

    @Inject
    ObjectMapper mapper;

    public BenchmarkData ingest(File file) {
        System.out.printf("Ingesting: %s\n", file);
        try {
            BenchmarkData obj = mapper.readValue(file, BenchmarkData.class);
            return obj;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
