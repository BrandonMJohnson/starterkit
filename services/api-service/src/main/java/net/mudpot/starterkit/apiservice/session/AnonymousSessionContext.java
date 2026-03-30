package net.mudpot.starterkit.apiservice.session;

import io.micronaut.http.HttpRequest;
import io.micronaut.runtime.http.scope.RequestScope;

@RequestScope
public class AnonymousSessionContext {
    private final AnonymousSession session;

    public AnonymousSessionContext(final HttpRequest<?> request, final AnonymousSessionResolver anonymousSessionResolver) {
        this(anonymousSessionResolver.resolve(request));
    }

    public AnonymousSessionContext(final AnonymousSession session) {
        this.session = session;
    }

    public AnonymousSession session() {
        return session;
    }
}
