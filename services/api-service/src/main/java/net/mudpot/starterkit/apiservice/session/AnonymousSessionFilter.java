package net.mudpot.starterkit.apiservice.session;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.order.Ordered;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import org.reactivestreams.Publisher;

@Filter("/api/**")
public class AnonymousSessionFilter implements HttpServerFilter, Ordered {
    private final AnonymousSessionResolver anonymousSessionResolver;
    private final AnonymousSessionService anonymousSessionService;

    public AnonymousSessionFilter(
        final AnonymousSessionResolver anonymousSessionResolver,
        final AnonymousSessionService anonymousSessionService
    ) {
        this.anonymousSessionResolver = anonymousSessionResolver;
        this.anonymousSessionService = anonymousSessionService;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(final HttpRequest<?> request, final ServerFilterChain chain) {
        final AnonymousSession session = anonymousSessionResolver.resolve(request);
        return Publishers.map(chain.proceed(request), response -> {
            if (session.fresh()) {
                response.cookie(anonymousSessionService.sessionCookie(session));
            }
            return response;
        });
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.SESSION.order();
    }
}
