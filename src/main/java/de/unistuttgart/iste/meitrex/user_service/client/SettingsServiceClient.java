package de.unistuttgart.iste.meitrex.user_service.client;

import de.unistuttgart.iste.meitrex.generated.dto.Settings;
import de.unistuttgart.iste.meitrex.user_service.exception.UserServiceConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.client.ClientGraphQlResponse;
import org.springframework.graphql.client.GraphQlClient;
import reactor.core.publisher.SynchronousSink;

import java.util.*;
import java.util.UUID;

/**
 * Client for the user-service to query user settings over GraphQL.
 */
@Slf4j
public class SettingsServiceClient {

    private static final long RETRY_COUNT = 3;
    private final GraphQlClient graphQlClient;

    public SettingsServiceClient(final GraphQlClient graphQlClient) {
        this.graphQlClient = graphQlClient;
    }

    /**
     * Queries user-service for the settings of a single user.
     *
     * @param userId the user id
     * @return settings of the user
     * @throws UserServiceConnectionException if the request fails or the response is invalid
     */
    public Settings queryUserSettings(final UUID userId) throws UserServiceConnectionException {
        try {
            return graphQlClient.document(QueryDefinitions.FIND_USER_SETTINGS_QUERY)
                    .variable("userId", userId)
                    .execute()
                    .handle((ClientGraphQlResponse result, SynchronousSink<Settings> sink) -> {
                        if (!result.isValid()) {
                            sink.error(new UserServiceConnectionException(
                                    "Invalid response from user-service.",
                                    result.getErrors()));
                            return;
                        }
                        final Settings s = result.field(QueryDefinitions.FIND_USER_SETTINGS_QUERY_NAME)
                                .toEntity(Settings.class);
                        sink.next(s);
                        sink.complete();
                    })
                    .retry(RETRY_COUNT)
                    .block();
        } catch (final RuntimeException e) {
            UserServiceConnectionException.unwrapAndThrow(e);
            return null; // unreachable
        }
    }

    /**
     * Queries user-service for the settings of multiple users.
     *
     * @param userIds list of user ids
     * @return list of settings
     * @throws UserServiceConnectionException if the request fails or the response is invalid
     */
    public List<Settings> queryUsersSettings(final List<UUID> userIds) throws UserServiceConnectionException {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        try {
            return graphQlClient.document(QueryDefinitions.FIND_USERS_SETTINGS_QUERY)
                    .variable("usersIds", userIds)
                    .execute()
                    .handle((ClientGraphQlResponse result, SynchronousSink<List<Settings>> sink) -> {
                        if (!result.isValid()) {
                            sink.error(new UserServiceConnectionException(
                                    "Invalid response from user-service.",
                                    result.getErrors()));
                            return;
                        }
                        final List<Settings> list = result
                                .field(QueryDefinitions.FIND_USERS_SETTINGS_QUERY_NAME)
                                .toEntityList(Settings.class);
                        sink.next(list != null ? list : List.of());
                        sink.complete();
                    })
                    .retry(RETRY_COUNT)
                    .block();
        } catch (final RuntimeException e) {
            UserServiceConnectionException.unwrapAndThrow(e);
            return List.of(); // unreachable
        }
    }
}
