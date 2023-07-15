package de.unistuttgart.iste.gits.user_service.controller;

import de.unistuttgart.iste.gits.generated.dto.CourseMembership;
import de.unistuttgart.iste.gits.generated.dto.CourseMembershipInput;
import de.unistuttgart.iste.gits.user_service.service.MembershipService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MembershipController {

    private final MembershipService membershipService;

    @QueryMapping
    public List<CourseMembership> courseMemberships(@Argument(name="id") UUID userId) {

        return membershipService.getAllMembershipsByUser(userId);
    }

    @QueryMapping
    public List<List<CourseMembership>> courseMembershipsBatched(@Argument(name="ids") List<UUID> userIds) {
        return membershipService.getMembershipsByUserBatched(userIds);
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
