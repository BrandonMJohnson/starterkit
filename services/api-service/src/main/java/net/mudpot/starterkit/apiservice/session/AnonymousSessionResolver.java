package net.mudpot.starterkit.apiservice.session;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.cookie.Cookie;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Singleton
public class AnonymousSessionResolver {
    private static final String DEFAULT_ACTOR_KIND = "anonymous";
    private static final String REQUEST_ATTRIBUTE = AnonymousSessionResolver.class.getName() + ".session";

    private final AnonymousSessionConfig config;

    public AnonymousSessionResolver(final AnonymousSessionConfig config) {
        this.config = config;
    }

    public AnonymousSession resolve(final HttpRequest<?> request) {
        final AnonymousSession existing = request.getAttribute(REQUEST_ATTRIBUTE, AnonymousSession.class).orElse(null);
        if (existing != null) {
            return existing;
        }

        final String rawToken = request.getCookies().findCookie(config.cookieName())
            .map(Cookie::getValue)
            .orElse("");
        final Map<String, Object> payload = AnonymousSessionCodec.verifyAndParse(rawToken, config.signingSecret());
        final String sessionId = stringValue(payload.get("session_id"));
        final Instant issuedAt = parseInstant(payload.get("issued_at"));
        final String actorKind = stringValue(payload.get("actor_kind"));
        final AnonymousSession session = !sessionId.isBlank() && issuedAt != null
            ? new AnonymousSession(sessionId, actorKind.isBlank() ? DEFAULT_ACTOR_KIND : actorKind, issuedAt, false)
            : new AnonymousSession(UUID.randomUUID().toString(), DEFAULT_ACTOR_KIND, Instant.now(), true);
        request.setAttribute(REQUEST_ATTRIBUTE, session);
        return session;
    }

    private static String stringValue(final Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private static Instant parseInstant(final Object value) {
        final String text = stringValue(value);
        if (text.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(text);
        } catch (final Exception ignored) {
            return null;
        }
    }
}
