package net.mudpot.starterkit.apiservice.session;

import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.order.Ordered;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.filter.ServerFilterPhase;
import net.mudpot.starterkit.commons.session.Session;
import org.reactivestreams.Publisher;

@Filter("/api/**")
public class SessionFilter implements HttpServerFilter, Ordered {
    private final SessionResolver sessionResolver;
    private final SessionService sessionService;

    public SessionFilter(final SessionResolver sessionResolver, final SessionService sessionService) {
        this.sessionResolver = sessionResolver;
        this.sessionService = sessionService;
    }

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(final HttpRequest<?> request, final ServerFilterChain chain) {
        final Session session = sessionResolver.resolve(request);
        return Publishers.map(chain.proceed(request), response -> {
            if (session.fresh()) {
                response.cookie(sessionService.sessionCookie(session));
            }
            return response;
        });
    }

    @Override
    public int getOrder() {
        return ServerFilterPhase.SESSION.order();
    }
}
