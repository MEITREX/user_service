package de.unistuttgart.iste.meitrex.user_service.persistence.validation;


import de.unistuttgart.iste.meitrex.generated.dto.ExternalServiceProviderDto;
import de.unistuttgart.iste.meitrex.generated.dto.GenerateAccessTokenInput;
import de.unistuttgart.iste.meitrex.user_service.persistence.entity.ExternalServiceProvider;
import jakarta.validation.ValidationException;
import org.springframework.stereotype.Component;

/**
 * Validates access token related input.
 */
@Component
public class AccessTokenValidator {

    public void validateProviderDto(ExternalServiceProviderDto provider) {
        if (provider == null) {
            throw new ValidationException("Provider must be specified");
        }

        try {
            // Check if the providerDto corresponds to a valid provider
            ExternalServiceProvider.valueOf(provider.name());
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Invalid provider: " + provider.name());
        }
    }

    public void validateGenerateAccessTokenInput(GenerateAccessTokenInput input) {
        if (input.getAuthorizationCode() == null) {
            throw new ValidationException("Authorization code must be specified");
        }

        if (input.getRedirectUri() == null) {
            throw new ValidationException("Redirect URI must be specified");
        }

        validateProviderDto(input.getProvider());
    }
}
