package com.example.pokedex.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Pokeapi pokeapi, Cache cache, Llm llm, String frontendOrigin) {
    public record Pokeapi(
        String baseUrl
    ) {}

    public record Cache(
        Duration ttl
    ) {}

    public record Llm(
        String chatUrl,
        String model,
        String apiKey
    ) {}
}