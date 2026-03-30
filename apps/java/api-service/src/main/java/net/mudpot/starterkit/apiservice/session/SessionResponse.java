package net.mudpot.starterkit.apiservice.session;

public record SessionResponse(
    String sessionId,
    String actorKind,
    String issuedAt
) {
}
