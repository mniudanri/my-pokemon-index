package com.example.pokedex;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PokedexIntegrationTest {
    private static final AtomicInteger pageCalls = new AtomicInteger();
    private static final AtomicInteger llmCalls = new AtomicInteger();
    private static final HttpServer stub;

    static {
        try {
            stub = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            stub.createContext("/", PokedexIntegrationTest::respond);
            stub.start();
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    @DynamicPropertySource
    static void externalUrls(DynamicPropertyRegistry properties) {
        String base = "http://127.0.0.1:" + stub.getAddress().getPort();
        properties.add("app.pokeapi.base-url", () -> base + "/api/v2");
        properties.add("app.llm.url", () -> base);
    }

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private CacheManager cacheManager;

    private final HttpClient http = HttpClient.newHttpClient();

    @BeforeEach
    void reset() {
        cacheManager.getCacheNames().forEach(name ->
                cacheManager.getCache(name).clear());
        pageCalls.set(0);
        llmCalls.set(0);
    }

    @AfterAll
    static void stopStub() {
        stub.stop(0);
    }

    @Test
    void browseUsesCacheOnRepeatedRequests() throws Exception {
        var first = get("/api/pokemon?offset=0&limit=2");
        var second = get("/api/pokemon?offset=0&limit=2");

        assertEquals(200, first.statusCode());
        assertEquals(first.body(), second.body());
        assertTrue(first.body().contains("\"total\":2"));
        assertTrue(first.body().contains("\"name\":\"pikachu\""));
        assertEquals(1, pageCalls.get());
    }

    @Test
    void searchAndTypeFilterReturnMatchingPokemon() throws Exception {
        var response = get("/api/pokemon?q=pika&type=electric");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"total\":1"));
        assertTrue(response.body().contains("\"name\":\"pikachu\""));
        assertFalse(response.body().contains("\"name\":\"raichu\""));
    }

    @Test
    void detailAndUnknownPokemonHaveExpectedStatuses() throws Exception {
        var detail = get("/api/pokemon/pikachu");

        assertEquals(200, detail.statusCode());
        assertTrue(detail.body().contains("\"electric\""));
        assertTrue(detail.body().contains("\"attack\":55"));
        assertEquals(404, get("/api/pokemon/missing").statusCode());
    }

    @Test
    void aiQuestionPassesThroughBackendAndLocalLlm() throws Exception {
        var request = HttpRequest.newBuilder(api("/api/ai/ask"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "{\"pokemon\":\"pikachu\",\"question\":\"What type is it?\"}"))
                .build();

        var response = http.send(request,
                HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains(
                "\"answer\":\"Pikachu is Electric-type.\""));
        assertEquals(1, llmCalls.get());
    }

    private HttpResponse<String> get(String path) throws Exception {
        return http.send(HttpRequest.newBuilder(api(path)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
    }

    private URI api(String path) {
        return URI.create("http://127.0.0.1:" + port + path);
    }

    private static void respond(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String body;
        int status = 200;

        switch (path) {
            case "/api/v2/pokemon" -> {
                pageCalls.incrementAndGet();
                body = """
                        {"count":2,"results":[
                          {"name":"pikachu","url":"https://pokeapi.co/api/v2/pokemon/25/"},
                          {"name":"raichu","url":"https://pokeapi.co/api/v2/pokemon/26/"}
                        ]}
                        """;
            }
            case "/api/v2/type/electric" -> body = """
                    {"pokemon":[
                      {"pokemon":{"name":"pikachu","url":"https://pokeapi.co/api/v2/pokemon/25/"}},
                      {"pokemon":{"name":"raichu","url":"https://pokeapi.co/api/v2/pokemon/26/"}}
                    ]}
                    """;
            case "/api/v2/pokemon/pikachu" -> body = """
                    {"id":25,"name":"pikachu","height":4,"weight":60,
                     "types":[{"slot":1,"type":{"name":"electric","url":""}}],
                     "stats":[{"base_stat":55,"stat":{"name":"attack","url":""}}],
                     "sprites":{"front_default":"https://example.test/pikachu.png"}}
                    """;
            case "/api/generate" -> {
                llmCalls.incrementAndGet();
                exchange.getRequestBody().readAllBytes();
                body = "{\"response\":\"Pikachu is Electric-type.\"}";
            }
            default -> {
                status = 404;
                body = "{}";
            }
        }

        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (var output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}