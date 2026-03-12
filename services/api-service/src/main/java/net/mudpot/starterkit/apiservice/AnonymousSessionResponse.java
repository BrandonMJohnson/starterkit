package net.mudpot.starterkit.apiservice;

public record AnonymousSessionResponse(
    String sessionId,
    String actorKind,
    String issuedAt
) {
}
