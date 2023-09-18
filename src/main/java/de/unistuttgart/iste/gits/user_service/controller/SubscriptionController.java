package de.unistuttgart.iste.gits.user_service.controller;

import de.unistuttgart.iste.gits.common.event.CourseChangeEvent;
import de.unistuttgart.iste.gits.common.exception.IncompleteEventMessageException;
import de.unistuttgart.iste.gits.user_service.service.MembershipService;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
@RequiredArgsConstructor
public class SubscriptionController {

    private final MembershipService membershipService;

    @Topic(name = "course-changes", pubsubName = "gits")
    @PostMapping(path = "/user-service/course-changes-pubsub")
    public Mono<Void> updateAssociation(@RequestBody CloudEvent<CourseChangeEvent> cloudEvent) {
        log.info("Received course change event: {}", cloudEvent.getData());
        return Mono.fromRunnable(
                () -> {
                    try {
                        membershipService.removeCourse(cloudEvent.getData());
                    } catch (IncompleteEventMessageException e) {
                        log.error("Error while processing course-changes event. {}", e.getMessage());
                    }
                });
    }
}
