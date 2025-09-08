package de.unistuttgart.iste.meitrex.user_service.client;

import lombok.NoArgsConstructor;

/**
 * GraphQL documents used by SettingsServiceClient.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class QueryDefinitions {

    public static final String FIND_USER_SETTINGS_QUERY_NAME = "findUserSettings";
    public static final String FIND_USERS_SETTINGS_QUERY_NAME = "findUsersSettings";

    public static final String FIND_USER_SETTINGS_QUERY = """
            query($userId: UUID!) {
              findUserSettings(userId: $userId) {
                gamification
                notification { gamification lecture }
              }
            }
            """;

    public static final String FIND_USERS_SETTINGS_QUERY = """
            query($usersIds: [UUID!]!) {
              findUsersSettings(usersIds: $usersIds) {
                gamification
                notification { gamification lecture }
              }
            }
            """;
}
