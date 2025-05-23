package de.unistuttgart.iste.meitrex.user_service.controller;
import de.unistuttgart.iste.meitrex.generated.dto.*;

import de.unistuttgart.iste.meitrex.user_service.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class SettingsController {
    private final SettingsService settingsService;

    @MutationMapping
    public Settings saveSettings(@Argument UUID userId, @Argument SettingsInput input) {
        return settingsService.save(userId,input);
    }

}
