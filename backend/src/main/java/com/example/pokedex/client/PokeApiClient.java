package com.example.pokedex.client;

import com.example.pokedex.config.AppProperties;
import com.example.pokedex.model.PokemonDtos.*;
import com.example.pokedex.model.SpeciesDtos.UpstreamEvolutionChain;
import com.example.pokedex.model.SpeciesDtos.UpstreamSpecies;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.security.GeneralSecurityException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.client.JdkClientHttpRequestFactory;

@Component
public class PokeApiClient {
    private final RestClient http;

    public PokeApiClient(RestClient.Builder builder, AppProperties properties, Environment environment) {
        if (environment.acceptsProfiles(Profiles.of("local-insecure"))) {
            try {
                SSLContext ssl = SSLContext.getInstance("TLS");
                ssl.init(null, new TrustManager[]{new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {}
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {}
                }}, null);

                builder.requestFactory(new JdkClientHttpRequestFactory(HttpClient.newBuilder().sslContext(ssl).build()));
            } catch (GeneralSecurityException e) {
                throw new IllegalStateException("Cannot configure local PokeAPI client", e);
            }
        }
        this.http = builder.baseUrl(properties.pokeapi().baseUrl()).build();
    }

    @Cacheable(value = "pages", key = "#offset + ':' + #limit")
    public UpstreamPage page(int offset, int limit) {
        return http.get()
                .uri(uri -> uri.path("/pokemon")
                        .queryParam("offset", offset)
                        .queryParam("limit", limit).build())
                .retrieve().body(UpstreamPage.class);
    }

    @Cacheable("all")
    public UpstreamPage all() {
        int count = page(0, 1).count();
        return page(0, count);
    }

    @Cacheable(value = "types", key = "#type")
    public UpstreamType type(String type) {
        return http.get().uri("/type/{type}", type).retrieve()
                .onStatus(status -> status.value() == 404,
                        (request, response) -> {
                            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Unknown Pokemon type");
                        })
                .body(UpstreamType.class);
    }

    @Cacheable(value = "details", key = "#name")
    public UpstreamPokemon detail(String name) {
        return http.get().uri("/pokemon/{name}", name).retrieve()
                .onStatus(status -> status.value() == 404,
                        (request, response) -> {
                            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pokemon not found");
                        })
                .body(UpstreamPokemon.class);
    }

    @Cacheable(value = "species", key = "#name")
    public UpstreamSpecies species(String name) {
        return http.get()
                .uri("/pokemon-species/{name}", name)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        (request, response) -> {
                            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Pokemon species not found");
                        })
                .body(UpstreamSpecies.class);
    }

    @Cacheable(value = "evolutionChains", key = "#id")
    public UpstreamEvolutionChain evolutionChain(int id) {
        return http.get()
                .uri("/evolution-chain/{id}", id)
                .retrieve()
                .body(UpstreamEvolutionChain.class);
    }
}