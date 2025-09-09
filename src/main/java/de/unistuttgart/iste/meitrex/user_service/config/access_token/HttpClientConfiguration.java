package de.unistuttgart.iste.meitrex.user_service.config.access_token;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
@RequiredArgsConstructor
public class HttpClientConfiguration {
    @Bean
    public HttpClient getHttpClient() {
        return HttpClient.newHttpClient();
    }
}