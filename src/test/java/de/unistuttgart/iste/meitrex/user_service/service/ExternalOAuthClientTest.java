package de.unistuttgart.iste.meitrex.user_service.service;

import de.unistuttgart.iste.meitrex.user_service.config.access_token.AccessTokenResponse;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.ExternalServiceProvider;
import de.unistuttgart.iste.meitrex.user_service.service.oauth.ExternalOAuthClient;
import de.unistuttgart.iste.meitrex.user_service.service.oauth.ExternalOAuthStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExternalOAuthClientTest {

    private ExternalOAuthStrategy githubStrategy;
    private ExternalOAuthClient oAuthClient;

    @BeforeEach
    void setUp() {
        githubStrategy = mock(ExternalOAuthStrategy.class);
        when(githubStrategy.supports(ExternalServiceProvider.GITHUB)).thenReturn(true);

        oAuthClient = new ExternalOAuthClient(List.of(githubStrategy));
    }

    @Test
    void testExchangeCodeForAccessToken_delegatesToCorrectStrategy() throws IOException, InterruptedException {
        AccessTokenResponse expectedResponse = new AccessTokenResponse("accessToken", 3600, "refreshToken", 7200);
        when(githubStrategy.exchangeCodeForAccessToken("authCode")).thenReturn(expectedResponse);

        AccessTokenResponse result = oAuthClient.exchangeCodeForAccessToken("authCode", ExternalServiceProvider.GITHUB);

        assertEquals(expectedResponse, result);
        verify(githubStrategy).exchangeCodeForAccessToken("authCode");
    }

    @Test
    void testRefreshAccessToken_delegatesToCorrectStrategy() throws IOException, InterruptedException {
        AccessTokenResponse expectedResponse = new AccessTokenResponse("newAccessToken", 3600, "newRefreshToken", 7200);
        when(githubStrategy.refreshAccessToken("oldRefreshToken")).thenReturn(expectedResponse);

        AccessTokenResponse result = oAuthClient.refreshAccessToken("oldRefreshToken", ExternalServiceProvider.GITHUB);

        assertEquals(expectedResponse, result);
        verify(githubStrategy).refreshAccessToken("oldRefreshToken");
    }

    @Test
    void testFetchExternalUserId_delegatesToCorrectStrategy() throws IOException, InterruptedException {
        when(githubStrategy.fetchExternalUserId("accessToken")).thenReturn("github_username");

        String result = oAuthClient.fetchExternalUserId("accessToken", ExternalServiceProvider.GITHUB);

        assertEquals("github_username", result);
        verify(githubStrategy).fetchExternalUserId("accessToken");
    }

    @Test
    void testUnsupportedProvider_throwsException() {
        ExternalOAuthClient clientWithNoSupport = new ExternalOAuthClient(List.of());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                clientWithNoSupport.exchangeCodeForAccessToken("code", ExternalServiceProvider.GITHUB)
        );

        assertTrue(exception.getMessage().contains("No OAuth strategy found"));
    }
}
