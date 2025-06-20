package de.unistuttgart.iste.meitrex.user_service.service;

import de.unistuttgart.iste.meitrex.user_service.config.access_token.AccessTokenResponse;
import de.unistuttgart.iste.meitrex.user_service.config.access_token.ExternalServiceProviderConfiguration;
import de.unistuttgart.iste.meitrex.user_service.config.access_token.ExternalServiceProviderInfo;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.ExternalServiceProvider;
import de.unistuttgart.iste.meitrex.user_service.service.oauth.GitHubOAuthStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GitHubOAuthStrategyTest {

    private GitHubOAuthStrategy strategy;
    private HttpClient mockClient;
    private HttpResponse<String> mockResponse;

    @BeforeEach
    void setUp() {
        mockClient = mock(HttpClient.class);
        ExternalServiceProviderConfiguration mockConfig = mock(ExternalServiceProviderConfiguration.class);
        mockResponse = mock(HttpResponse.class);

        ExternalServiceProviderInfo githubInfo = ExternalServiceProviderInfo.builder()
                .clientId("clientId")
                .clientSecret("clientSecret")
                .tokenRequestUrl("https://github.com/login/oauth/access_token")
                .externalUserIdUrl("https://api.github.com/user")
                .build();

        when(mockConfig.getProviders()).thenReturn(Map.of(ExternalServiceProvider.GITHUB, githubInfo));

        strategy = new GitHubOAuthStrategy(mockClient, mockConfig);
    }

    @Test
    void supports_shouldReturnTrueForGitHub() {
        assertTrue(strategy.supports(ExternalServiceProvider.GITHUB));
    }

    @Test
    void exchangeCodeForAccessToken_shouldReturnAccessTokenResponseOnSuccess() throws IOException, InterruptedException {
        String json = """
                {
                  "access_token": "abc123",
                  "expires_in": 3600,
                  "refresh_token": "ref456",
                  "refresh_token_expires_in": 7200
                }
                """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(json);
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        AccessTokenResponse result = strategy.exchangeCodeForAccessToken("dummyCode");

        assertNotNull(result);
        assertEquals("abc123", result.getAccessToken());
        assertEquals(3600, result.getExpiresIn());
        assertEquals("ref456", result.getRefreshToken());
        assertEquals(7200, result.getRefreshTokenExpiresIn());
    }

    @Test
    void fetchExternalUserId_shouldReturnUsernameOnSuccess() throws IOException, InterruptedException {
        String json = """
                {
                  "login": "bohdan"
                }
                """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(json);
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        String userId = strategy.fetchExternalUserId("valid_token");
        assertEquals("bohdan", userId);
    }

    @Test
    void refreshAccessToken_shouldReturnAccessTokenResponseOnSuccess() throws IOException, InterruptedException {
        String json = """
            {
              "access_token": "new_access_token",
              "expires_in": 3600,
              "refresh_token": "new_refresh_token",
              "refresh_token_expires_in": 86400
            }
            """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(json);
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        AccessTokenResponse result = strategy.refreshAccessToken("old_refresh_token");

        assertNotNull(result);
        assertEquals("new_access_token", result.getAccessToken());
        assertEquals(3600, result.getExpiresIn());
        assertEquals("new_refresh_token", result.getRefreshToken());
        assertEquals(86400, result.getRefreshTokenExpiresIn());
    }

    @Test
    void exchangeCodeForAccessToken_shouldReturnNullOnFailure() throws IOException, InterruptedException {
        when(mockResponse.statusCode()).thenReturn(401);
        when(mockResponse.body()).thenReturn("Unauthorized");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        AccessTokenResponse result = strategy.exchangeCodeForAccessToken("invalid_code");

        assertNull(result);
    }

    @Test
    void refreshAccessToken_shouldReturnNullOnFailure() throws IOException, InterruptedException {
        when(mockResponse.statusCode()).thenReturn(400);
        when(mockResponse.body()).thenReturn("Bad Request");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        AccessTokenResponse result = strategy.refreshAccessToken("invalid_refresh_token");

        assertNull(result);
    }

    @Test
    void fetchExternalUserId_shouldReturnNullOnFailure() throws IOException, InterruptedException {
        when(mockResponse.statusCode()).thenReturn(403);
        when(mockResponse.body()).thenReturn("Forbidden");
        when(mockClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        String userId = strategy.fetchExternalUserId("invalid_token");

        assertNull(userId);
    }
}
