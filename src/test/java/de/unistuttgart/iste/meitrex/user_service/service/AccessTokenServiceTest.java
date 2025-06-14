package de.unistuttgart.iste.meitrex.user_service.service;

import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.generated.dto.*;
import de.unistuttgart.iste.meitrex.user_service.config.access_token.AccessTokenResponse;
import de.unistuttgart.iste.meitrex.user_service.config.access_token.ExternalServiceProviderInfo;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.AccessTokenEntity;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.ExternalServiceProvider;
import de.unistuttgart.iste.meitrex.user_service.persistence.repository.AccessTokenRepository;
import de.unistuttgart.iste.meitrex.user_service.config.access_token.ExternalServiceProviderConfiguration;
import de.unistuttgart.iste.meitrex.user_service.service.oauth.ExternalOAuthClient;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.modelmapper.ModelMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.List;
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

    @Mock
    private ExternalOAuthClient externalOAuthClient;

    @InjectMocks
    private AccessTokenService accessTokenService;

    private final LoggedInUser loggedInUser = mock(LoggedInUser.class);
    private final ExternalServiceProviderDto providerDto = ExternalServiceProviderDto.GITHUB;
    private final AccessTokenEntity validAccessToken = new AccessTokenEntity();

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

        UUID userId = loggedInUser.getId();

        assertThrows(EntityNotFoundException.class, () -> {
            accessTokenService.getAccessToken(userId, providerDto);
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

    @Test
    void testGetAccessToken_ExpiredToken_ExpiredRefreshToken() {
        validAccessToken.setAccessToken("valid_access_token");
        validAccessToken.setAccessTokenExpiresAt(OffsetDateTime.now().minusMinutes(5));
        validAccessToken.setRefreshToken("refresh_token");
        validAccessToken.setRefreshTokenExpiresAt(OffsetDateTime.now().minusMinutes(5));
        when(accessTokenRepository.findByUserIdAndProvider(any(), any())).thenReturn(Optional.of(validAccessToken));

        UUID userId = loggedInUser.getId();

        assertThrows(EntityNotFoundException.class, () -> {
            accessTokenService.getAccessToken(userId, providerDto);
        });
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

        AccessTokenResponse tokenResponse = new AccessTokenResponse("new_valid_access_token", 28800, "refresh_token", 15897600);
        when(externalOAuthClient.refreshAccessToken(any(), any()))
                .thenReturn(tokenResponse);

        accessTokenService = new AccessTokenService(
                userService,
                accessTokenRepository,
                modelMapper,
                externalOAuthClient
        );

        AccessToken result = accessTokenService.getAccessToken(loggedInUser.getId(), providerDto);

        assertThat(result.getAccessToken(), equalTo("new_valid_access_token"));
        verify(accessTokenRepository, times(1)).save(any(AccessTokenEntity.class));
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
        when(providerInfo.getExternalUserIdUrl()).thenReturn("https://api.github.com/user");

        Map<ExternalServiceProvider, ExternalServiceProviderInfo> providersMap =
                Map.of(ExternalServiceProvider.GITHUB, providerInfo);
        when(providersConfig.getProviders()).thenReturn(providersMap);

        AccessTokenResponse tokenResponse = new AccessTokenResponse("generated_access_token", 28800, "refresh_token", 15897600);
        when(externalOAuthClient.exchangeCodeForAccessToken(any(), any()))
                .thenReturn(tokenResponse);

        when(externalOAuthClient.fetchExternalUserId(any(), any()))
                .thenReturn("external_user_id");

        accessTokenService = new AccessTokenService(
                userService,
                accessTokenRepository,
                modelMapper,
                externalOAuthClient
        );

        boolean result = accessTokenService.generateAccessToken(loggedInUser, input);
        assertTrue(result);

        ArgumentCaptor<AccessTokenEntity> captor = ArgumentCaptor.forClass(AccessTokenEntity.class);
        verify(accessTokenRepository, times(1)).save(captor.capture());

        AccessTokenEntity savedEntity = captor.getValue();
        assertThat(savedEntity.getUserId(), is(loggedInUser.getId()));
        assertThat(savedEntity.getProvider(), is(ExternalServiceProvider.GITHUB));
        assertThat(savedEntity.getAccessToken(), is("generated_access_token"));
        assertThat(savedEntity.getRefreshToken(), is("refresh_token"));
        assertThat(savedEntity.getExternalUserId(), is("external_user_id"));
    }

    @Test
    void testGetExternalUserIds_someUsersHaveTokens() {
        // Given
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID noTokenUserId = UUID.randomUUID();

        AccessTokenEntity token1 = new AccessTokenEntity();
        token1.setUserId(userId1);
        token1.setExternalUserId("user1_ext");

        AccessTokenEntity token2 = new AccessTokenEntity();
        token2.setUserId(userId2);
        token2.setExternalUserId("user2_ext");

        when(modelMapper.map(ExternalServiceProviderDto.GITHUB, ExternalServiceProvider.class))
                .thenReturn(ExternalServiceProvider.GITHUB);

        when(accessTokenRepository.findByUserIdAndProvider(userId1, ExternalServiceProvider.GITHUB))
                .thenReturn(Optional.of(token1));
        when(accessTokenRepository.findByUserIdAndProvider(userId2, ExternalServiceProvider.GITHUB))
                .thenReturn(Optional.of(token2));
        when(accessTokenRepository.findByUserIdAndProvider(noTokenUserId, ExternalServiceProvider.GITHUB))
                .thenReturn(Optional.empty());

        List<ExternalUserIdWithUser> result = accessTokenService.getExternalUserIds(
                ExternalServiceProviderDto.GITHUB,
                List.of(userId1, userId2, noTokenUserId)
        );

        assertThat(result, hasSize(2));
        assertThat(result, containsInAnyOrder(
                new ExternalUserIdWithUser(userId1, "user1_ext"),
                new ExternalUserIdWithUser(userId2, "user2_ext")
        ));
    }

    @Test
    void testGetExternalUserIds_noUsersHaveTokens() {
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();

        when(modelMapper.map(ExternalServiceProviderDto.GITHUB, ExternalServiceProvider.class))
                .thenReturn(ExternalServiceProvider.GITHUB);

        when(accessTokenRepository.findByUserIdAndProvider(any(), eq(ExternalServiceProvider.GITHUB)))
                .thenReturn(Optional.empty());

        List<ExternalUserIdWithUser> result = accessTokenService.getExternalUserIds(
                ExternalServiceProviderDto.GITHUB,
                List.of(userId1, userId2)
        );

        assertThat(result, is(empty()));
    }

    @Test
    void testGetExternalUserIds_emptyInputList() {
        List<ExternalUserIdWithUser> result = accessTokenService.getExternalUserIds(
                ExternalServiceProviderDto.GITHUB,
                List.of()
        );

        assertThat(result, is(empty()));
        verifyNoInteractions(accessTokenRepository);
    }

}
