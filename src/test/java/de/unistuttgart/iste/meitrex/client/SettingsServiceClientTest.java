package de.unistuttgart.iste.meitrex.client;

import de.unistuttgart.iste.meitrex.generated.dto.Gamification;
import de.unistuttgart.iste.meitrex.generated.dto.Settings;
import de.unistuttgart.iste.meitrex.user_service.client.SettingsServiceClient;
import de.unistuttgart.iste.meitrex.user_service.exception.SettingServiceConnectionException;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.ResponseError;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Tests for SettingsServiceClient
 */
class SettingsServiceClientTest {

    @Test
    void queryUserSettings_success() throws Exception {
        String json = """
            {
              "data": {
                "findUserSettings": {
                  "gamification": "GAMIFICATION_ENABLED",
                  "notification": { "gamification": true, "lecture": false }
                }
              }
            }
        """;

        SettingsServiceClient client = new SettingsServiceClient(mockGraphQl(json));

        UUID userId = UUID.randomUUID();
        Settings s = client.queryUserSettings(userId);

        assertNotNull(s);
        assertEquals(Gamification.GAMIFICATION_ENABLED, s.getGamification());
        assertNotNull(s.getNotification());
        assertEquals(Boolean.TRUE, s.getNotification().getGamification());
        assertEquals(Boolean.FALSE, s.getNotification().getLecture());
    }

    @Test
    void queryUsersSettings_success() throws Exception {
        String json = """
            {
              "data": {
                "findUsersSettings": [
                  {
                    "gamification": "GAMIFICATION_ENABLED",
                    "notification": { "gamification": true, "lecture": false }
                  },
                  {
                    "gamification": "ALL_GAMIFICATION_DISABLED",
                    "notification": { "gamification": false, "lecture": true }
                  }
                ]
              }
            }
        """;

        SettingsServiceClient client = new SettingsServiceClient(mockGraphQl(json));

        var u1 = UUID.randomUUID();
        var u2 = UUID.randomUUID();
        List<Settings> list = client.queryUsersSettings(List.of(u1, u2));

        assertNotNull(list);
        assertEquals(2, list.size());

        assertEquals(Gamification.GAMIFICATION_ENABLED, list.get(0).getGamification());
        assertEquals(Boolean.TRUE, list.get(0).getNotification().getGamification());
        assertEquals(Boolean.FALSE, list.get(0).getNotification().getLecture());

        assertEquals(Gamification.ALL_GAMIFICATION_DISABLED, list.get(1).getGamification());
        assertEquals(Boolean.FALSE, list.get(1).getNotification().getGamification());
        assertEquals(Boolean.TRUE, list.get(1).getNotification().getLecture());
    }

    @Test
    void queryUserSettings_graphQlErrors_throws() {
        String json = """
        {
          "data": { "findUserSettings": null },
          "errors": [ { "message": "boom", "path": ["findUserSettings"] } ]
        }
    """;

        ExchangeFunction fx = req -> {
            ClientResponse resp = ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(json)
                    .build();
            return Mono.just(resp);
        };
        WebClient webClient = WebClient.builder().exchangeFunction(fx).build();
        GraphQlClient gql = HttpGraphQlClient.builder(webClient).build();

        SettingsServiceClient client = new SettingsServiceClient(gql);

        assertThrows(SettingServiceConnectionException.class,
                () -> client.queryUserSettings(UUID.randomUUID()));
    }

    @Test
    void queryUserSettings_fieldMissing_throws() {
        String json = "{ \"data\": { } }";
        SettingsServiceClient client = new SettingsServiceClient(gqlWithJson(json));

        assertThrows(SettingServiceConnectionException.class,
                () -> client.queryUserSettings(UUID.randomUUID()));
    }


    @Test
    void queryUserSettings_retriesThenSuccess() throws Exception {
        String err = "{ \"data\": { \"findUserSettings\": null }, \"errors\": [ { \"message\": \"e\" } ] }";
        String ok = """
        { "data": { "findUserSettings": {
            "gamification": "ALL_GAMIFICATION_DISABLED",
            "notification": { "gamification": false, "lecture": true }
        } } }
    """;
        SettingsServiceClient client = new SettingsServiceClient(gqlWithSequence(err, err, ok));

        Settings s = client.queryUserSettings(UUID.randomUUID());

        assertNotNull(s);
        assertEquals(Gamification.ALL_GAMIFICATION_DISABLED, s.getGamification());
        assertEquals(Boolean.FALSE, s.getNotification().getGamification());
        assertEquals(Boolean.TRUE, s.getNotification().getLecture());
    }

    @Test
    void queryUsersSettings_emptyInput_noHttpCall() {
        AtomicInteger calls = new AtomicInteger(0);
        SettingsServiceClient client = new SettingsServiceClient(gqlCounting(calls));

        try {
            List<Settings> list = client.queryUsersSettings(List.of());
            assertNotNull(list);
            assertTrue(list.isEmpty());
            assertEquals(0, calls.get(), "No HTTP call should be made for empty input");
        } catch (SettingServiceConnectionException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }



    @Test
    void queryUsersSettings_mappingError_throws() {
        String json = "{ \"data\": { \"findUsersSettings\": { \"not\": \"a list\" } } }";
        SettingsServiceClient client = new SettingsServiceClient(gqlWithJson(json));

        assertThrows(SettingServiceConnectionException.class,
                () -> client.queryUsersSettings(List.of(UUID.randomUUID())));
    }


    /**
     * Creates a GraphQlClient backed by a WebClient that always returns the given JSON.
     * No server, no extra deps.
     */
    private static GraphQlClient mockGraphQl(String jsonResponse) {
        ExchangeFunction fx = request -> {
            ClientResponse resp = ClientResponse
                    .create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(jsonResponse)
                    .build();
            return Mono.just(resp);
        };
        WebClient webClient = WebClient.builder()
                .exchangeFunction(fx)
                .build();
        return HttpGraphQlClient.builder(webClient).build();
    }

    private static GraphQlClient gqlWithJson(String json) {
        ExchangeFunction fx = req -> Mono.just(
                ClientResponse.create(HttpStatus.OK)
                        .header("Content-Type", "application/json")
                        .body(json)
                        .build()
        );
        WebClient webClient = WebClient.builder().exchangeFunction(fx).build();
        return HttpGraphQlClient.builder(webClient).build();
    }

    private static GraphQlClient gqlWithSequence(String... jsonResponses) {
        AtomicInteger idx = new AtomicInteger(0);
        ExchangeFunction fx = req -> {
            String body = jsonResponses[Math.min(idx.getAndIncrement(), jsonResponses.length - 1)];
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(body)
                    .build());
        };
        WebClient webClient = WebClient.builder().exchangeFunction(fx).build();
        return HttpGraphQlClient.builder(webClient).build();
    }

    private static GraphQlClient gqlCounting(AtomicInteger counter) {
        ExchangeFunction fx = req -> {
            counter.incrementAndGet();
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("{ \"data\": { \"findUsersSettings\": [] } }")
                    .build());
        };
        WebClient webClient = WebClient.builder().exchangeFunction(fx).build();
        return HttpGraphQlClient.builder(webClient).build();
    }

    private static GraphQlClient gqlErrorWrapped(String message) {
        ExchangeFunction fx = req -> {
            return Mono.error(new RuntimeException(
                    new SettingServiceConnectionException(message)
            ));
        };
        WebClient webClient = WebClient.builder().exchangeFunction(fx).build();
        return HttpGraphQlClient.builder(webClient).build();
    }

    @Test
    void queryUserSettings_runtimeWrapped_unwrapsToUserServiceConnectionException() {
        SettingsServiceClient client = new SettingsServiceClient(gqlErrorWrapped("wrapped-error"));
        assertThrows(SettingServiceConnectionException.class,
                () -> client.queryUserSettings(UUID.randomUUID()));
    }

    @Test
    void queryUsersSettings_runtimeWrapped_unwrapsToUserServiceConnectionException() {
        SettingsServiceClient client = new SettingsServiceClient(gqlErrorWrapped("wrapped-error"));
        assertThrows(SettingServiceConnectionException.class,
                () -> client.queryUsersSettings(List.of(UUID.randomUUID())));
    }

    @Test
    void queryUsersSettings_nullList_normalizedToEmpty() throws SettingServiceConnectionException {
        String json = """
        { "data": { "findUsersSettings": null } }
    """;
        SettingsServiceClient client = new SettingsServiceClient(gqlWithJson(json));

        List<Settings> list = client.queryUsersSettings(List.of(UUID.randomUUID()));
        org.junit.jupiter.api.Assertions.assertNotNull(list);
        org.junit.jupiter.api.Assertions.assertTrue(list.isEmpty());
    }

    private static ResponseError responseError(String message) {
        return (ResponseError) Proxy.newProxyInstance(
                ResponseError.class.getClassLoader(),
                new Class[]{ResponseError.class},
                (proxy, method, args) -> {
                    String name = method.getName();
                    if ("getMessage".equals(name)) {
                        return message;
                    }
                    if ("toSpecification".equals(name)) {
                        return Map.of("message", message);
                    }
                    return null;
                }
        );
    }


    @Test
    void constructor_withMessage_keepsMessage() {
        var ex = new SettingServiceConnectionException("plain");
        assertEquals("plain", ex.getMessage());
    }

    @Test
    void constructor_withErrors_formatsMessage() {
        var base = "Invalid response";
        var ex = new SettingServiceConnectionException(base, java.util.List.of(
                responseError("boom1"),
                responseError("boom2")
        ));

        String msg = ex.getMessage();
        assertTrue(msg.contains(base));
        assertTrue(msg.contains("boom1"));
        assertTrue(msg.contains("boom2"));
    }

    @Test
    void constructor_withNullErrors_fallsBackToBaseMessage() {
        var ex = new SettingServiceConnectionException("only-base", null);
        assertEquals("only-base", ex.getMessage());
    }

    @Test
    void unwrapAndThrow_directWrapped_throwsInnerUserEx() {
        var inner = new SettingServiceConnectionException("inner");
        var outer = new RuntimeException(inner);

        var thrown = assertThrows(SettingServiceConnectionException.class,
                () -> SettingServiceConnectionException.unwrapAndThrow(outer));
        assertEquals("inner", thrown.getMessage());
    }

    @Test
    void unwrapAndThrow_deeplyNested_throwsInnerUserEx() {
        var inner = new SettingServiceConnectionException("deep");
        var outer = new RuntimeException(new IllegalStateException(inner));

        var thrown = assertThrows(SettingServiceConnectionException.class,
                () -> SettingServiceConnectionException.unwrapAndThrow(outer));
        assertEquals("deep", thrown.getMessage());
    }

    @Test
    void unwrapAndThrow_noUserEx_rethrowsOriginalRuntime() {
        var outer = new RuntimeException(new IllegalArgumentException("no-user-ex"));
        var rethrown = assertThrows(RuntimeException.class,
                () -> SettingServiceConnectionException.unwrapAndThrow(outer));
        assertSame(outer, rethrown);
    }
}
