package net.mudpot.starterkit.apiservice.controllers;

import io.micronaut.context.BeanProvider;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.cookie.Cookie;
import net.mudpot.starterkit.apiservice.session.AnonymousSession;
import net.mudpot.starterkit.apiservice.session.AnonymousSessionContext;
import net.mudpot.starterkit.apiservice.session.AnonymousSessionResolver;
import net.mudpot.starterkit.apiservice.session.AnonymousSessionResponse;
import net.mudpot.starterkit.apiservice.session.AnonymousSessionService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionControllerTest {
    @Test
    void currentSessionReturnsResolvedSessionPayload() {
        final SessionController controller = new SessionController(
            new BeanProvider<>() {
                @Override
                public AnonymousSessionContext get() {
                    return new AnonymousSessionContext(null) {
                        @Override
                        public AnonymousSession session() {
                            return new AnonymousSession("anon-session-1", "anonymous", java.time.Instant.parse("2026-03-12T00:00:00Z"), false);
                        }
                    };
                }
            },
            new AnonymousSessionService(new net.mudpot.starterkit.apiservice.session.AnonymousSessionConfig() {
            })
        );

        final var response = controller.currentSession();
        final AnonymousSessionResponse body = response.body();

        assertNotNull(body);
        assertEquals("anon-session-1", body.sessionId());
        assertEquals("anonymous", body.actorKind());
        assertTrue(response.getCookies().isEmpty());
    }
}
