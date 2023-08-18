package de.unistuttgart.iste.gits.user_service.controller;

import de.unistuttgart.iste.gits.generated.dto.*;
import de.unistuttgart.iste.gits.user_service.service.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @SchemaMapping
    public List<CourseMembership> courseMemberships(UserInfo user) {
        return membershipService.getAllMembershipsByUser(user.getId());
    }

    @MutationMapping
    public CourseMembership createMembership(@Argument(name = "input")CourseMembershipInput inputDto){
        return membershipService.createMembership(inputDto);
    }

    @MutationMapping
    public CourseMembership updateMembership(@Argument(name = "input")CourseMembershipInput inputDto){
        return membershipService.updateMembershipRole(inputDto);
    }

    @MutationMapping
    public CourseMembership deleteMembership(@Argument(name = "input")CourseMembershipInput inputDto){
        return membershipService.deleteMembership(inputDto);
    }
}
