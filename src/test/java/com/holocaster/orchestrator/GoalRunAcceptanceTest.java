package com.holocaster.orchestrator;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GoalRunAcceptanceTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    private static final AtomicReference<String> providerRequest = new AtomicReference<>();
    private static final AtomicReference<CountDownLatch> providerCalled = new AtomicReference<>();
    private static final AtomicReference<CountDownLatch> providerResponseGate = new AtomicReference<>();
    private static HttpServer provider;

    @LocalServerPort
    private int port;

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();

    @BeforeAll
    static void startProvider() throws IOException {
        provider = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        provider.createContext("/v1/chat/completions", GoalRunAcceptanceTest::finishGoal);
        provider.start();
    }

    @AfterAll
    static void stopProvider() {
        provider.stop(0);
    }

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry properties) {
        properties.add("spring.datasource.url", postgres::getJdbcUrl);
        properties.add("spring.datasource.username", postgres::getUsername);
        properties.add("spring.datasource.password", postgres::getPassword);
        properties.add("spring.ai.openai.base-url", () -> "http://localhost:" + provider.getAddress().getPort() + "/v1");
        properties.add("spring.ai.openai.api-key", () -> "test-key");
        properties.add("spring.ai.openai.chat.model", () -> "test-orchestrator");
    }

    @Test
    void completesAGoalRunThroughThePublicApi() throws Exception {
        HttpResponse<String> created = send("POST", "/api/goal-runs", "{\"goal\":\"Explain the MVP in one sentence\"}");

        assertThat(created.statusCode()).isEqualTo(201);
        JsonNode createdRun = json.readTree(created.body());
        assertThat(createdRun.get("goal").asText()).isEqualTo("Explain the MVP in one sentence");
        assertThat(createdRun.get("state").asText()).isEqualTo("RUNNING");

        JsonNode completedRun = awaitCompleted(createdRun.get("id").asText());
        assertThat(completedRun.get("state").asText()).isEqualTo("COMPLETED");
        assertThat(completedRun.get("outcome").asText()).isEqualTo("ACHIEVED");
        assertThat(completedRun.get("finalAnswer").asText()).isEqualTo("The MVP proves one bounded orchestration path.");
        assertThat(completedRun.toString()).doesNotContain("test-key", "Authorization");

        JsonNode sentToProvider = json.readTree(providerRequest.get());
        assertThat(sentToProvider.at("/response_format/type").asText()).isEqualTo("json_schema");
        assertThat(sentToProvider.at("/response_format/json_schema/strict").asBoolean()).isTrue();
        assertThat(sentToProvider.at("/model").asText()).isEqualTo("test-orchestrator");
    }

    @Test
    void rejectsAnotherGoalRunWhileOneIsRunning() throws Exception {
        CountDownLatch called = new CountDownLatch(1);
        CountDownLatch responseGate = new CountDownLatch(1);
        providerCalled.set(called);
        providerResponseGate.set(responseGate);

        String firstRunId;
        try {
            HttpResponse<String> first = send("POST", "/api/goal-runs", "{\"goal\":\"Keep this run active\"}");
            assertThat(first.statusCode()).isEqualTo(201);
            firstRunId = json.readTree(first.body()).get("id").asText();
            assertThat(called.await(5, TimeUnit.SECONDS)).isTrue();

            HttpResponse<String> conflicting = send("POST", "/api/goal-runs", "{\"goal\":\"Do not start this run\"}");
            assertThat(conflicting.statusCode()).isEqualTo(409);
            assertThat(json.readTree(conflicting.body()).get("message").asText())
                    .isEqualTo("Another Goal Run is already running.");
        } finally {
            responseGate.countDown();
            providerCalled.set(null);
            providerResponseGate.set(null);
        }

        assertThat(awaitCompleted(firstRunId).get("state").asText()).isEqualTo("COMPLETED");
    }

    private JsonNode awaitCompleted(String id) throws Exception {
        long deadline = System.nanoTime() + Duration.ofSeconds(10).toNanos();
        JsonNode run;
        do {
            Thread.sleep(50);
            HttpResponse<String> response = send("GET", "/api/goal-runs/" + id, null);
            assertThat(response.statusCode()).isEqualTo(200);
            run = json.readTree(response.body());
        } while (run.get("state").asText().equals("RUNNING") && System.nanoTime() < deadline);
        return run;
    }

    private HttpResponse<String> send(String method, String path, String body) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
                .header("Content-Type", "application/json");
        request.method(method, body == null
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body));
        return http.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static void finishGoal(HttpExchange exchange) throws IOException {
        providerRequest.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        CountDownLatch called = providerCalled.get();
        if (called != null) {
            called.countDown();
        }
        CountDownLatch responseGate = providerResponseGate.get();
        if (responseGate != null) {
            try {
                responseGate.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while delaying the fake provider response.", exception);
            }
        }
        byte[] response = """
                {
                  "id": "generation-test-1",
                  "object": "chat.completion",
                  "created": 1760000000,
                  "model": "test-orchestrator",
                  "choices": [{
                    "index": 0,
                    "message": {
                      "role": "assistant",
                      "content": "{\\"action\\":\\"FINISH\\",\\"outcome\\":\\"ACHIEVED\\",\\"answer\\":\\"The MVP proves one bounded orchestration path.\\"}"
                    },
                    "finish_reason": "stop"
                  }],
                  "usage": {
                    "prompt_tokens": 20,
                    "completion_tokens": 15,
                    "total_tokens": 35
                  }
                }
                """.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        exchange.getResponseBody().write(response);
        exchange.close();
    }
}
