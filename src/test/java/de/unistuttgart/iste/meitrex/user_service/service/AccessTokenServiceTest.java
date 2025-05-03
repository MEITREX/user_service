package de.unistuttgart.iste.meitrex.user_service.service;

import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.generated.dto.AccessToken;
import de.unistuttgart.iste.meitrex.generated.dto.ExternalServiceProviderDto;
import de.unistuttgart.iste.meitrex.generated.dto.GenerateAccessTokenInput;
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

        UUID userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
        when(loggedInUser.getId()).thenReturn(userId);

        UserInfo userInfo = mock(UserInfo.class);
        when(userInfo.getId()).thenReturn(userId);

        when(userService.findUserInfo(userId)).thenReturn(Optional.of(userInfo));
        when(userService.findUserInfoInHeader(loggedInUser)).thenReturn(userInfo);
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
            accessTokenService.getAccessToken(loggedInUser.getId(), providerDto);
        });
    }

    @Test
    void testGetAccessToken_ValidNonExpiringToken() {
        validAccessToken.setAccessToken("valid_access_token");
        validAccessToken.setAccessTokenExpiresAt(null);
        when(accessTokenRepository.findByUserIdAndProvider(any(), any())).thenReturn(Optional.of(validAccessToken));

        AccessToken result = accessTokenService.getAccessToken(loggedInUser.getId(), providerDto);

        assertThat(result.getAccessToken(), equalTo("valid_access_token"));
    }

    @Test
    void testGetAccessToken_ValidExpiringToken() {
        validAccessToken.setAccessToken("valid_access_token");
        validAccessToken.setAccessTokenExpiresAt(OffsetDateTime.now().plusMinutes(5));
        when(accessTokenRepository.findByUserIdAndProvider(any(), any())).thenReturn(Optional.of(validAccessToken));

        AccessToken result = accessTokenService.getAccessToken(loggedInUser.getId(), providerDto);

        assertThat(result.getAccessToken(), equalTo("valid_access_token"));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetAccessToken_ExpiredToken_ValidRefreshToken() throws Exception {
        validAccessToken.setAccessToken("expired_access_token");
        validAccessToken.setAccessTokenExpiresAt(OffsetDateTime.now().minusMinutes(5));
        validAccessToken.setRefreshToken("refresh_token");
        validAccessToken.setRefreshTokenExpiresAt(OffsetDateTime.now().plusMinutes(5));

        when(accessTokenRepository.findByUserIdAndProvider(any(), any())).thenReturn(Optional.of(validAccessToken));

        when(modelMapper.map(ExternalServiceProviderDto.GITHUB, ExternalServiceProvider.class))
                .thenReturn(ExternalServiceProvider.GITHUB);

        ExternalServiceProviderInfo providerInfo = mock(ExternalServiceProviderInfo.class);
        when(providerInfo.getClientId()).thenReturn("clientId");
        when(providerInfo.getClientSecret()).thenReturn("clientSecret");
        when(providerInfo.getTokenRequestUrl()).thenReturn("https://token.url");

        Map<ExternalServiceProvider, ExternalServiceProviderInfo> providersMap =
                Map.of(ExternalServiceProvider.GITHUB, providerInfo);
        when(providersConfig.getProviders()).thenReturn(providersMap);

        String responseBody = """
        {
            "access_token": "new_valid_access_token",
            "expires_in": 28800,
            "refresh_token": "refresh_token_value",
            "refresh_token_expires_in": 15897600
        }
        """;
        HttpResponse<String> mockHttpResponse = mock(HttpResponse.class);
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(responseBody);

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString()))).thenReturn(mockHttpResponse);

        accessTokenService = new AccessTokenService(userService, accessTokenRepository, providersConfig, modelMapper, mockHttpClient);

        AccessToken result = accessTokenService.getAccessToken(loggedInUser.getId(), providerDto);

        assertThat(result.getAccessToken(), equalTo("new_valid_access_token"));
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
            accessTokenService.getAccessToken(loggedInUser.getId(), providerDto);
        });
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGenerateAccessToken_Success() throws Exception {
        GenerateAccessTokenInput input = new GenerateAccessTokenInput();
        input.setAuthorizationCode("mockCode");
        input.setProvider(ExternalServiceProviderDto.GITHUB);

        when(modelMapper.map(ExternalServiceProviderDto.GITHUB, ExternalServiceProvider.class))
                .thenReturn(ExternalServiceProvider.GITHUB);

        ExternalServiceProviderInfo providerInfo = mock(ExternalServiceProviderInfo.class);
        when(providerInfo.getClientId()).thenReturn("clientId");
        when(providerInfo.getClientSecret()).thenReturn("clientSecret");
        when(providerInfo.getTokenRequestUrl()).thenReturn("https://mock.token.url");

        Map<ExternalServiceProvider, ExternalServiceProviderInfo> providersMap =
                Map.of(ExternalServiceProvider.GITHUB, providerInfo);
        when(providersConfig.getProviders()).thenReturn(providersMap);

        String responseBody = """
        {
            "access_token": "generated_access_token",
            "expires_in": 28800,
            "refresh_token": "refresh_token",
            "refresh_token_expires_in": 15897600
        }
        """;
        HttpResponse<String> mockHttpResponse = mock(HttpResponse.class);
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(responseBody);

        HttpClient mockHttpClient = mock(HttpClient.class);
        when(mockHttpClient.send(any(HttpRequest.class), eq(HttpResponse.BodyHandlers.ofString())))
                .thenAnswer(invocation -> {
                    HttpRequest request = invocation.getArgument(0);
                    if (request.uri().toString().equals("https://api.github.com/user")) {
                        HttpResponse<String> userInfoResponse = mock(HttpResponse.class);
                        when(userInfoResponse.statusCode()).thenReturn(200);
                        when(userInfoResponse.body()).thenReturn("""
                {
                    "login": "mocked-username"
                }
            """);
                        return userInfoResponse;
                    } else {
                        return mockHttpResponse; // your token response
                    }
                });


        accessTokenService = new AccessTokenService(
                userService, accessTokenRepository, providersConfig, modelMapper, mockHttpClient);

        boolean result = accessTokenService.generateAccessToken(loggedInUser, input);
        assertTrue(result);

        ArgumentCaptor<AccessTokenEntity> captor = ArgumentCaptor.forClass(AccessTokenEntity.class);
        verify(accessTokenRepository, times(1)).save(captor.capture());

        AccessTokenEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getUserId(), is(loggedInUser.getId()));
        assertThat(savedEntity.getProvider(), is(ExternalServiceProvider.GITHUB));
        assertThat(savedEntity.getAccessToken(), is("generated_access_token"));
        assertThat(savedEntity.getRefreshToken(), is("refresh_token"));
    }
}
