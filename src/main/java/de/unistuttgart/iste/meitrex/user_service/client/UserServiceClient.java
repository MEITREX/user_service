package de.unistuttgart.iste.meitrex.user_service.client;


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

        List<UserInfo> retrievedUserInfos = null;
        try {
            retrievedUserInfos = result.field(queryName).toEntityList(UserInfo.class);
        } catch (FieldAccessException e) {
            sink.error(new UserServiceConnectionException(e.toString()));
        }

        if (retrievedUserInfos == null) {
            sink.error(new UserServiceConnectionException("Error fetching userInfo from UserService: Missing field in response."));
            return;
        }
        if (retrievedUserInfos.isEmpty()) {
            sink.error(new UserServiceConnectionException("Error fetching userInfo from UserService: Field in response is empty."));
        }

        sink.next(retrievedUserInfos);
    }

    private static void unwrapUserServiceConnectionException(final RuntimeException e) throws UserServiceConnectionException {
        // block wraps exceptions in a RuntimeException, so we need to unwrap them
        if (e.getCause() instanceof final UserServiceConnectionException userServiceConnectionException) {
            throw userServiceConnectionException;
        }
        // if the exception is not a ContentServiceConnectionException, we don't know how to handle it
        throw e;
    }

}
