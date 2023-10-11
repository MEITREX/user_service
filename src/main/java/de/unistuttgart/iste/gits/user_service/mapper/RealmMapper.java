package de.unistuttgart.iste.gits.user_service.mapper;

import de.unistuttgart.iste.gits.common.user_handling.LoggedInUser;
import de.unistuttgart.iste.gits.generated.dto.GlobalUserRole;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Component
public class RealmMapper {

    private final ModelMapper modelMapper;

    public RealmMapper(ModelMapper modelMapper){
        this.modelMapper = modelMapper;
    }

    public List<GlobalUserRole> internalRolesToGraphQlRoles (Set<LoggedInUser.RealmRole> realmRoleSet){
        return realmRoleSet.stream().map(role -> modelMapper.map(role, GlobalUserRole.class)).toList();
    }

    public List<GlobalUserRole> keycloakRolesToGraphQlRoles(List<String> keycloakRoles){
        if (keycloakRoles == null){
            return Collections.emptyList();
        }

        Set<LoggedInUser.RealmRole> internalRoles = LoggedInUser.RealmRole.getRolesFromKeycloakRoleList(keycloakRoles);
        return internalRolesToGraphQlRoles(internalRoles);
    }
}
