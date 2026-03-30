package net.mudpot.starterkit.apiservice.session;

import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.SameSite;
import jakarta.inject.Singleton;
import net.mudpot.starterkit.commons.session.Session;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@Singleton
public class SessionService {
    private final SessionConfig config;

    public SessionService(final SessionConfig config) {
        this.config = config;
    }

    public SessionResponse toResponse(final Session session) {
        return new SessionResponse(session.sessionId(), session.actorKind(), session.issuedAt().toString());
    }

    public Cookie sessionCookie(final Session session) {
        final Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("session_id", session.sessionId());
        payload.put("actor_kind", session.actorKind());
        payload.put("issued_at", session.issuedAt().toString());
        final Cookie cookie = Cookie.of(config.cookieName(), SessionCodec.sign(payload, config.signingSecret()))
            .httpOnly(true)
            .path("/")
            .sameSite(SameSite.Lax)
            .secure(config.cookieSecure())
            .maxAge(Duration.ofDays(config.maxAgeDays()));
        if (!config.cookieDomain().isBlank()) {
            cookie.domain(config.cookieDomain());
        }
        return cookie;
    }
}
