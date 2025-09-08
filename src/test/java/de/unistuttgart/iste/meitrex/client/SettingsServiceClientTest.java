package de.unistuttgart.iste.meitrex.client;

import de.unistuttgart.iste.meitrex.generated.dto.Gamification;
import de.unistuttgart.iste.meitrex.generated.dto.Settings;
import de.unistuttgart.iste.meitrex.user_service.client.SettingsServiceClient;
import de.unistuttgart.iste.meitrex.user_service.exception.UserServiceConnectionException;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.client.FieldAccessException;
import org.springframework.graphql.client.GraphQlClient;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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

        assertThrows(UserServiceConnectionException.class,
                () -> client.queryUserSettings(UUID.randomUUID()));
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
}
