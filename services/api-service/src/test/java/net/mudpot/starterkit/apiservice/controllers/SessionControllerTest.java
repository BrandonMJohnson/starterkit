package net.mudpot.starterkit.apiservice.controllers;

import net.mudpot.starterkit.apiservice.session.SessionResponse;
import net.mudpot.starterkit.apiservice.session.SessionService;
import net.mudpot.starterkit.commons.session.Session;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class SessionControllerTest {
    @Test
    void currentSessionReturnsSessionPayload() {
        final SessionController controller = new SessionController(new SessionService(new net.mudpot.starterkit.apiservice.session.SessionConfig() {
        }));

        final SessionResponse body = controller.currentSession(
            new Session("anon-session-1", "anonymous", java.time.Instant.parse("2026-03-12T00:00:00Z"), false)
        );

        assertNotNull(body);
        assertEquals("anon-session-1", body.sessionId());
        assertEquals("anonymous", body.actorKind());
    }
}
