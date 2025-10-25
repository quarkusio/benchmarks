package org.acme.model;

public record Result(
        Build build,
        Startup startup,
        Rss rss,
        Load load) {
}
