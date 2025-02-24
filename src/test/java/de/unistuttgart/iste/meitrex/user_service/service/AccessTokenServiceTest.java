package de.unistuttgart.iste.meitrex.user_service.service;

import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.generated.dto.ExternalServiceProviderDto;
import de.unistuttgart.iste.meitrex.generated.dto.UserInfo;
import de.unistuttgart.iste.meitrex.user_service.config.access_token.ExternalServiceProviderInfo;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.AccessTokenEntity;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.ExternalServiceProvider;
import de.unistuttgart.iste.meitrex.user_service.persistence.repository.AccessTokenRepository;
import de.unistuttgart.iste.meitrex.user_service.config.access_token.ExternalServiceProviderConfiguration;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class AccessTokenServiceTest {

    @Mock
    private AccessTokenRepository accessTokenRepository;

    @Mock
    private UserService userService;

    @Mock
    private ExternalServiceProviderConfiguration providersConfig;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private AccessTokenService accessTokenService;

    private final LoggedInUser loggedInUser = mock(LoggedInUser.class);
    private final ExternalServiceProviderDto providerDto = ExternalServiceProviderDto.GITHUB;
    private final AccessTokenEntity validAccessToken = new AccessTokenEntity();;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        UserInfo userInfo = mock(UserInfo.class);
        when(userService.findUserInfoInHeader(loggedInUser)).thenReturn(userInfo);
        when(userInfo.getId()).thenReturn(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    }

    @Test
    void testIsAccessTokenAvailable_NoTokenFound() {
        when(accessTokenRepository.findByUserIdAndProvider(any(), any())).thenReturn(Optional.empty());

        Boolean result = accessTokenService.isAccessTokenAvailable(loggedInUser, providerDto);

        assertThat(result, is(false));
    }

    @Test
    void testIsAccessTokenAvailable_ValidNonExpiringToken() {
        validAccessToken.setAccessToken("valid_access_token");
        validAccessToken.setAccessTokenExpiresAt(null);
        when(accessTokenRepository.findByUserIdAndProvider(any(), any())).thenReturn(Optional.of(validAccessToken));

        Boolean result = accessTokenService.isAccessTokenAvailable(loggedInUser, providerDto);

        assertThat(result, is(true));
    }

    @Test
    void testIsAccessTokenAvailable_ValidExpiringToken() {
        validAccessToken.setAccessToken("valid_access_token");
        validAccessToken.setAccessTokenExpiresAt(OffsetDateTime.now().plusMinutes(5));
        when(accessTokenRepository.findByUserIdAndProvider(any(), any())).thenReturn(Optional.of(validAccessToken));

        Boolean result = accessTokenService.isAccessTokenAvailable(loggedInUser, providerDto);

        assertThat(result, is(true));
    }

    @Test
    void testIsAccessTokenAvailable_ExpiredToken_ValidRefreshToken() {
        validAccessToken.setAccessToken("valid_access_token");
        validAccessToken.setAccessTokenExpiresAt(OffsetDateTime.now().minusMinutes(5));
        validAccessToken.setRefreshToken("refresh_token");
        validAccessToken.setRefreshTokenExpiresAt(OffsetDateTime.now().plusMinutes(5));
        when(accessTokenRepository.findByUserIdAndProvider(any(), any())).thenReturn(Optional.of(validAccessToken));

        Boolean result = accessTokenService.isAccessTokenAvailable(loggedInUser, providerDto);

        assertThat(result, is(true));
    }

    @Test
    void testIsAccessTokenAvailable_ExpiredToken_ExpiredRefreshToken() {
        validAccessToken.setAccessToken("valid_access_token");
        validAccessToken.setAccessTokenExpiresAt(OffsetDateTime.now().minusMinutes(5));
        validAccessToken.setRefreshToken("refresh_token");
        validAccessToken.setRefreshTokenExpiresAt(OffsetDateTime.now().minusMinutes(5));
        when(accessTokenRepository.findByUserIdAndProvider(any(), any())).thenReturn(Optional.of(validAccessToken));

        Boolean result = accessTokenService.isAccessTokenAvailable(loggedInUser, providerDto);

        assertThat(result, is(false));
    }

    @Test
    void testGetAccessToken_NoTokenFound() {
        when(accessTokenRepository.findByUserIdAndProvider(any(), any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> {
            accessTokenService.getAccessToken(loggedInUser, providerDto);
        });
    }

    @Test
    void testGetAccessToken_ValidNonExpiringToken() {
        validAccessToken.setAccessToken("valid_access_token");
        validAccessToken.setAccessTokenExpiresAt(null);
        when(accessTokenRepository.findByUserIdAndProvider(any(), any())).thenReturn(Optional.of(validAccessToken));

        String result = accessTokenService.getAccessToken(loggedInUser, providerDto);

        assertThat(result, equalTo("valid_access_token"));
    }

    @Test
    void testGetAccessToken_ValidExpiringToken() {
        validAccessToken.setAccessToken("valid_access_token");
        validAccessToken.setAccessTokenExpiresAt(OffsetDateTime.now().plusMinutes(5));
        when(accessTokenRepository.findByUserIdAndProvider(any(), any())).thenReturn(Optional.of(validAccessToken));

        String result = accessTokenService.getAccessToken(loggedInUser, providerDto);

        assertThat(result, equalTo("valid_access_token"));
    }

    @Test
    void testGetAccessToken_ExpiredToken_ValidRefreshToken() throws Exception {
        validAccessToken.setAccessToken("expired_access_token");
        validAccessToken.setAccessTokenExpiresAt(OffsetDateTime.now().minusMinutes(5));
        validAccessToken.setRefreshToken("refresh_token");
        validAccessToken.setRefreshTokenExpiresAt(OffsetDateTime.now().plusMinutes(5));

        when(accessTokenRepository.findByUserIdAndProvider(any(), any())).thenReturn(Optional.of(validAccessToken));

        ExternalServiceProviderInfo mockProviderInfo = mock(ExternalServiceProviderInfo.class);
        when(mockProviderInfo.getClientId()).thenReturn("mockClientId");
        when(mockProviderInfo.getClientSecret()).thenReturn("mockClientSecret");
        when(mockProviderInfo.getTokenRequestUrl()).thenReturn("https://mock.token.url");

        Map<ExternalServiceProvider, ExternalServiceProviderInfo> mockProvidersMap =
                Map.of(ExternalServiceProvider.GITHUB, mockProviderInfo);
        when(providersConfig.getProviders()).thenReturn(mockProvidersMap);

        String mockResponseBody = "{\"access_token\":\"new_valid_access_token\", \"expires_in\":3600, \"refresh_token\":\"new_refresh_token\", \"refresh_token_expires_in\":7200}";
        HttpResponse<String> mockHttpResponse = mock(HttpResponse.class);
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(mockResponseBody);

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(mockHttpResponse);

        accessTokenService = new AccessTokenService(userService, accessTokenRepository, providersConfig, modelMapper, mockHttpClient);

        String result = accessTokenService.getAccessToken(loggedInUser, providerDto);

        assertThat(result, equalTo("new_valid_access_token"));

        verify(accessTokenRepository, times(1)).save(any(AccessTokenEntity.class));
    }

    @Test
    void testGetAccessToken_ExpiredToken_ExpiredRefreshToken() {
        validAccessToken.setAccessToken("valid_access_token");
        validAccessToken.setAccessTokenExpiresAt(OffsetDateTime.now().minusMinutes(5));
        validAccessToken.setRefreshToken("refresh_token");
        validAccessToken.setRefreshTokenExpiresAt(OffsetDateTime.now().minusMinutes(5));
        when(accessTokenRepository.findByUserIdAndProvider(any(), any())).thenReturn(Optional.of(validAccessToken));

        assertThrows(EntityNotFoundException.class, () -> {
            accessTokenService.getAccessToken(loggedInUser, providerDto);
        });
    }

    // Test for generateAccessToken
    @Test
    void testGenerateAccessToken_Success() {
        assertNull("");
    }
}
