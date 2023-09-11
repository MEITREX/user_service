package de.unistuttgart.iste.gits.user_service.config;

import de.unistuttgart.iste.gits.common.user_handling.RequestHeaderUserProcessor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.*;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

@Configuration
public class RequestHeaderUserInterceptor implements WebGraphQlInterceptor {
    @Override
    @SneakyThrows
    @NonNull
    public Mono<WebGraphQlResponse> intercept(@NonNull WebGraphQlRequest request, Chain chain) {
        RequestHeaderUserProcessor.process(request);
        return chain.next(request);
    }
}