package io.quarkus.infra.performance.graphics;

import java.io.IOException;
import java.nio.file.Path;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.infra.performance.graphics.model.BenchmarkData;

@ApplicationScoped
public class DataIngester {
    @Inject
    ObjectMapper mapper;

    public BenchmarkData ingest(Path file) {
        System.out.printf("Ingesting: %s\n", file);
        try {
            return mapper.readValue(file.toFile(), BenchmarkData.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
