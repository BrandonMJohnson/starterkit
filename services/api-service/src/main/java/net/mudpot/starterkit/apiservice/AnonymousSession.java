package net.mudpot.starterkit.apiservice;

import java.time.Instant;

public record AnonymousSession(
    String sessionId,
    String actorKind,
    Instant issuedAt,
    boolean fresh
) {
}
