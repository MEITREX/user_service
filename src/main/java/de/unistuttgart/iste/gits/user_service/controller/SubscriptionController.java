package de.unistuttgart.iste.gits.user_service.controller;

import de.unistuttgart.iste.gits.common.event.CourseChangeEvent;
import de.unistuttgart.iste.gits.user_service.service.MembershipService;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

@Controller
@Slf4j
public class SubscriptionController {

    MembershipService membershipService;

    @Topic(name = "course-changes", pubsubName = "gits")
    @PostMapping(path = "/user-service/course-changes-pubsub")
    public Mono<Void> updateAssociation(@RequestBody CloudEvent<CourseChangeEvent> cloudEvent) {

        return Mono.fromRunnable(
                () -> {
                    try {
                        membershipService.removeCourse(cloudEvent.getData());
                    } catch (NullPointerException e) {
                        log.error(e.getMessage());
                    }
                });
    }
}
