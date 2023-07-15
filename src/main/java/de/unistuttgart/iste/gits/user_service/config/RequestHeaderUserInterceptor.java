package de.unistuttgart.iste.gits.user_service.config;

import de.unistuttgart.iste.gits.common.user_handling.RequestHeaderUserProcessor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Configuration;
import org.springframework.graphql.server.WebGraphQlInterceptor;
import org.springframework.graphql.server.WebGraphQlRequest;
import org.springframework.graphql.server.WebGraphQlResponse;
import reactor.core.publisher.Mono;

@Configuration
public class RequestHeaderUserInterceptor implements WebGraphQlInterceptor {
    @Override
    @SneakyThrows
    public Mono<WebGraphQlResponse> intercept(WebGraphQlRequest request, Chain chain) {
        RequestHeaderUserProcessor.process(request);
        return chain.next(request);
    }
}