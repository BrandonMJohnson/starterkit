package net.mudpot.starterkit.commons.session;

import java.time.Instant;

public record Session(
    String sessionId,
    String actorKind,
    Instant issuedAt,
    boolean fresh
) {
    public boolean isAnonymous() {
        return actorKind != null && actorKind.equalsIgnoreCase("anonymous");
    }
}
