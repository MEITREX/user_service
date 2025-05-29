package de.unistuttgart.iste.meitrex.user_service.persistence.mapper;

import de.unistuttgart.iste.meitrex.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.meitrex.generated.dto.GlobalUserRole;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RealmMapper {

    private final ModelMapper modelMapper;

    public RealmMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public List<GlobalUserRole> internalRolesToGraphQlRoles(Set<LoggedInUser.RealmRole> realmRoleSet) {
        return realmRoleSet.stream().map(role -> modelMapper.map(role, GlobalUserRole.class)).toList();
    }

    public List<GlobalUserRole> keycloakRolesToGraphQlRoles(List<String> keycloakRoles) {
        if (keycloakRoles == null) {
            return Collections.emptyList();
        }

        Set<LoggedInUser.RealmRole> internalRoles = LoggedInUser.RealmRole.getRolesFromKeycloakRoleList(keycloakRoles);
        return internalRolesToGraphQlRoles(internalRoles);
    }
}
