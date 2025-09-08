package de.unistuttgart.iste.meitrex.user_service.exception;

import org.springframework.graphql.ResponseError;

import java.util.List;

/**
 * Exception thrown when the connection to the user-service fails or returns an invalid response.
 */
public class UserServiceConnectionException extends Exception {

    private final String message;

    public UserServiceConnectionException(final String message) {
        super(message);
        this.message = message;
    }

    public UserServiceConnectionException(final String message, final List<ResponseError> errors) {
        super(withErrors(message, errors));
        this.message = withErrors(message, errors);
    }

    private static String withErrors(final String base, final List<ResponseError> errors) {
        if (errors == null || errors.isEmpty()) return base;
        final StringBuilder sb = new StringBuilder(base).append('\n')
                .append("GraphQL Response Errors:\n");
        for (final ResponseError err : errors) {
            sb.append(err.getMessage()).append(" at path ").append(err.getPath()).append('\n');
        }
        return sb.toString();
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    /**
     * Unwraps a RuntimeException thrown by reactive pipelines and rethrows as UserServiceConnectionException when possible.
     */
    public static void unwrapAndThrow(final RuntimeException e) throws UserServiceConnectionException {
        Throwable t = e;
        while (t != null) {
            if (t instanceof UserServiceConnectionException uce) {
                throw uce;
            }
            t = t.getCause();
        }
        throw e;
    }
}
