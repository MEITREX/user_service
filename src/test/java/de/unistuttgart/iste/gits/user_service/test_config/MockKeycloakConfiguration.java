package de.unistuttgart.iste.gits.user_service.test_config;

import de.unistuttgart.iste.gits.user_service.config.KeycloakWrapper;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.UUID;

@TestConfiguration
public class MockKeycloakConfiguration {

    public static UUID firstUserId = UUID.fromString("ed59730a-e956-42c2-9ac6-815f5523ac48");
    public static UUID secondUserId = UUID.fromString("52945048-4a4d-48aa-8ba5-52013df593d6");
    public static UUID thirdUserId = UUID.fromString("1de8dc82-f2a0-4c80-a4d8-a122c88fb55a");

    @Primary
    @Bean
    public KeycloakWrapper keycloak() {
        final RealmResource realm = Mockito.mock(RealmResource.class, Mockito.RETURNS_DEEP_STUBS);

        UserRepresentation firstUser = new UserRepresentation();
        firstUser.setId(firstUserId.toString());
        firstUser.setFirstName("First");
        firstUser.setLastName("User");
        firstUser.setUsername("firstuser");

        UserRepresentation secondUser = new UserRepresentation();
        secondUser.setId(secondUserId.toString());
        secondUser.setFirstName("Second");
        secondUser.setLastName("User");
        secondUser.setUsername("seconduser");

        UserRepresentation thirdUser = new UserRepresentation();
        thirdUser.setId(thirdUserId.toString());
        thirdUser.setFirstName("Third");
        thirdUser.setLastName("User");
        thirdUser.setUsername("thirduser");

        Mockito.when(realm.users().get(firstUserId.toString()).toRepresentation()).thenReturn(firstUser);
        Mockito.when(realm.users().get(secondUserId.toString()).toRepresentation()).thenReturn(secondUser);
        Mockito.when(realm.users().get(thirdUserId.toString()).toRepresentation()).thenReturn(thirdUser);

        return new KeycloakWrapper(realm);
    }
}
