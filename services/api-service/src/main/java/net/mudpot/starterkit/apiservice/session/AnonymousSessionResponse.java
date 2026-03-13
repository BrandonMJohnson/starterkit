package net.mudpot.starterkit.apiservice.session;

public record AnonymousSessionResponse(
    String sessionId,
    String actorKind,
    String issuedAt
) {
}
