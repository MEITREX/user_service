package de.unistuttgart.iste.meitrex.user_service.controller;
import de.unistuttgart.iste.meitrex.generated.dto.*;

import de.unistuttgart.iste.meitrex.user_service.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class SettingsController {
    private final SettingsService settingsService;

    @MutationMapping
    public Settings updateSettings(@Argument UUID userId, @Argument SettingsInput input) {
        return settingsService.update(userId,input);
    }

    @QueryMapping
    public Settings findUserSettings(@Argument UUID userId) {
        return settingsService.find(userId);
    }

    // Set default settings
    @MutationMapping
    public Settings defaultSettings(@Argument UUID userId){
        return settingsService.setDefault(userId);
    }

}
