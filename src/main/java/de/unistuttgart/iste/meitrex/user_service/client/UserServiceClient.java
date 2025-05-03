package de.unistuttgart.iste.meitrex.user_service.client;


import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.generated.dto.AccessToken;
import de.unistuttgart.iste.meitrex.generated.dto.ExternalServiceProviderDto;
import de.unistuttgart.iste.meitrex.generated.dto.ExternalUserIdWithUser;
import de.unistuttgart.iste.meitrex.generated.dto.UserInfo;
import de.unistuttgart.iste.meitrex.user_service.exception.UserServiceConnectionException;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.FieldAccessException;
import org.springframework.graphql.client.GraphQlClient;
import reactor.core.publisher.SynchronousSink;

import java.util.List;
import java.util.UUID;

/*
Client allowing to query user info.
 */

public class UserServiceClient {

    private static final long RETRY_COUNT = 3;
    private final GraphQlClient graphQlClient;

    public UserServiceClient(GraphQlClient graphQlClient) {
        this.graphQlClient = graphQlClient;
    }

    public List<UserInfo> queryUserInfos(final List<UUID> userIds) throws UserServiceConnectionException {
        final String query = """
                query($userIds: [UUID!]!) {
                    findUserInfos(ids: $userIds) {
                        id,
                        userName,
                        firstName,
                        lastName
                    }
                }
                """;
        String queryName = "findUserInfos";

        List<UserInfo> meitrexStudentInfoList = null;

        try {
            meitrexStudentInfoList = graphQlClient.document(query)
                    .variable("userIds", userIds)
                    .execute()
                    .handle((ClientGraphQlResponse result, SynchronousSink<List<UserInfo>> sink)
                            -> handleGraphQlResponse(result, sink, queryName))
                    .retry(RETRY_COUNT)
                    .block();
        } catch (RuntimeException e) {
            unwrapUserServiceConnectionException(e);
        }

        if (meitrexStudentInfoList == null) {
            throw new UserServiceConnectionException("Error fetching userInfo from UserService");
        }

        return meitrexStudentInfoList;
    }

    private void handleGraphQlResponse(final ClientGraphQlResponse result, final SynchronousSink<List<UserInfo>> sink, final String queryName) {
        if (!result.isValid()) {
            sink.error(new UserServiceConnectionException(result.getErrors().toString()));
            return;
        }

        List<UserInfo> retrievedUserInfos;
        try {
            retrievedUserInfos = result.field(queryName).toEntityList(UserInfo.class);
        } catch (FieldAccessException e) {
            sink.error(new UserServiceConnectionException(e.toString()));
            return;
        }

        // retrievedUserInfos == null is always false, therefore no check
        if (retrievedUserInfos.isEmpty()) {
            sink.error(new UserServiceConnectionException("Error fetching userInfo from UserService: UserInfo List is empty."));
            return;
        }

        sink.next(retrievedUserInfos);
    }

    public AccessToken queryAccessToken(LoggedInUser currentUser, ExternalServiceProviderDto provider) throws UserServiceConnectionException {
        final String query = """
            query($currentUserId: UUID!, $provider: ExternalServiceProviderDto!) {
                _internal_getAccessToken(currentUserId: $currentUserId, provider: $provider) {
                    accessToken
                    externalUserId
                }
            }
        """;
        final String queryName = "_internal_getAccessToken";

        try {
            return graphQlClient.document(query)
                    .variable("provider", provider)
                    .variable("currentUserId", currentUser.getId())
                    .execute()
                    .handle((ClientGraphQlResponse result, SynchronousSink<AccessToken> sink) -> {
                        if (!result.isValid()) {
                            sink.error(new UserServiceConnectionException(result.getErrors().toString()));
                            return;
                        }

                        try {
                            AccessToken accessToken = result.field(queryName).toEntity(AccessToken.class);
                            if (accessToken == null || accessToken.getAccessToken().isBlank()) {
                                sink.error(new UserServiceConnectionException("Access token is empty or null."));
                            } else {
                                sink.next(accessToken);
                            }
                        } catch (FieldAccessException e) {
                            sink.error(new UserServiceConnectionException("Failed to extract access token: " + e.getMessage()));
                        }
                    })
                    .retry(RETRY_COUNT)
                    .block();
        } catch (RuntimeException e) {
            unwrapUserServiceConnectionException(e);
            return null;
        }
    }

    private static void unwrapUserServiceConnectionException(final RuntimeException e) throws UserServiceConnectionException {
        // block wraps exceptions in a RuntimeException, so we need to unwrap them
        if (e.getCause() instanceof final UserServiceConnectionException userServiceConnectionException) {
            throw userServiceConnectionException;
        }
        // if the exception is not a UserServiceConnectionException, we don't know how to handle it
        throw e;
    }

    public List<ExternalUserIdWithUser> queryExternalUserIds(ExternalServiceProviderDto provider, List<UUID> userIds) throws UserServiceConnectionException {
        final String query = """
        query($provider: ExternalServiceProviderDto!, $userIds: [UUID!]!) {
            _internal_getExternalUserIds(provider: $provider, userIds: $userIds) {
                userId
                externalUserId
            }
        }
    """;
        final String queryName = "_internal_getExternalUserIds";

        try {
            return graphQlClient.document(query)
                    .variable("provider", provider)
                    .variable("userIds", userIds)
                    .execute()
                    .handle((ClientGraphQlResponse result, SynchronousSink<List<ExternalUserIdWithUser>> sink) -> {
                        if (!result.isValid()) {
                            sink.error(new UserServiceConnectionException(result.getErrors().toString()));
                            return;
                        }

                        try {
                            List<ExternalUserIdWithUser> list = result.field(queryName).toEntityList(ExternalUserIdWithUser.class);
                            if (list == null || list.isEmpty()) {
                                sink.error(new UserServiceConnectionException("ExternalUserIdWithUser list is empty or null."));
                            } else {
                                sink.next(list);
                            }
                        } catch (FieldAccessException e) {
                            sink.error(new UserServiceConnectionException("Failed to extract userId-externalId map: " + e.getMessage()));
                        }
                    })
                    .retry(RETRY_COUNT)
                    .block();
        } catch (RuntimeException e) {
            unwrapUserServiceConnectionException(e);
            return null;
        }
    }


}
