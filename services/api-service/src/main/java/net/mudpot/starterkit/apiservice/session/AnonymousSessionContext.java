package net.mudpot.starterkit.apiservice.session;

import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.runtime.http.scope.RequestScope;

@RequestScope
public class AnonymousSessionContext {
    private final AnonymousSessionResolver anonymousSessionResolver;

    public AnonymousSessionContext(final AnonymousSessionResolver anonymousSessionResolver) {
        this.anonymousSessionResolver = anonymousSessionResolver;
    }

    public AnonymousSession session() {
        final io.micronaut.http.HttpRequest<?> request = ServerRequestContext.currentRequest()
            .orElseThrow(() -> new IllegalStateException("Anonymous session requested outside request context."));
        return anonymousSessionResolver.resolve(request);
    }
}
