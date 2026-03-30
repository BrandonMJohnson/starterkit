package net.mudpot.starterkit.apiservice.session;

import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import net.mudpot.starterkit.commons.session.Session;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionArgumentBinderTest {
    @Test
    void bindsSessionFromRequest() {
        final Session expected = new Session("session-1", "anonymous", Instant.parse("2026-03-12T00:00:00Z"), false);
        final SessionArgumentBinder binder = new SessionArgumentBinder(new SessionResolver(new SessionConfig() {
        }) {
            @Override
            public Session resolve(final HttpRequest<?> request) {
                return expected;
            }
        });

        final var result = binder.bind(ConversionContext.of(Argument.of(Session.class)), HttpRequest.GET("/api/session"));

        assertTrue(result.isPresentAndSatisfied());
        assertEquals(expected, result.get());
    }
}
