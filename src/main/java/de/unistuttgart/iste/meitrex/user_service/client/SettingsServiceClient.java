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
     * Query settings for a single user.
     *
     * @param userId the user id
     * @return settings of the user (never null on success)
     * @throws UserServiceConnectionException if the response has GraphQL errors, the field is missing/null, or mapping fails
     */
    public Settings queryUserSettings(final UUID userId) throws UserServiceConnectionException {
        try {
            return graphQlClient.document(QueryDefinitions.FIND_USER_SETTINGS_QUERY)
                    .variable("userId", userId)
                    .execute()
                    .handle((ClientGraphQlResponse result, SynchronousSink<Settings> sink) -> {
                        if (!result.isValid()) {
                            sink.error(new UserServiceConnectionException(
                                    "Invalid response from user-service (findUserSettings).",
                                    result.getErrors()));
                            return;
                        }
                        try {
                            final Settings s = result
                                    .field(QueryDefinitions.FIND_USER_SETTINGS_QUERY_NAME)
                                    .toEntity(Settings.class);
                            if (s == null) {
                                sink.error(new UserServiceConnectionException(
                                        "Missing field 'findUserSettings' in user-service response."));
                                return;
                            }
                            sink.next(s);
                            sink.complete();
                        } catch (Exception mappingEx) {
                            sink.error(new UserServiceConnectionException(
                                    "Failed to map 'findUserSettings' from user-service response: "
                                            + mappingEx.getMessage()));
                        }
                    })
                    .retry(RETRY_COUNT)
                    .block();
        } catch (final RuntimeException e) {
            UserServiceConnectionException.unwrapAndThrow(e);
            return null;
        }
    }

    /**
     * Query settings for multiple users.
     *
     * @param userIds list of user ids
     * @return list of settings (never null; may be empty)
     * @throws UserServiceConnectionException if the response has GraphQL errors or mapping fails
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
                                    "Invalid response from user-service (findUsersSettings).",
                                    result.getErrors()));
                            return;
                        }
                        try {
                            final List<Settings> list = result
                                    .field(QueryDefinitions.FIND_USERS_SETTINGS_QUERY_NAME)
                                    .toEntityList(Settings.class);
                            sink.next(list != null ? list : List.of());
                            sink.complete();
                        } catch (Exception mappingEx) {
                            sink.error(new UserServiceConnectionException(
                                    "Failed to map 'findUsersSettings' from user-service response: "
                                            + mappingEx.getMessage()));
                        }
                    })
                    .retry(RETRY_COUNT)
                    .block();
        } catch (final RuntimeException e) {
            UserServiceConnectionException.unwrapAndThrow(e);
            return List.of(); // unreachable
        }
    }
}
