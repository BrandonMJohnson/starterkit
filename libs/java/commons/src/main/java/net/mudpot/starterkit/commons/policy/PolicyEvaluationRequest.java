package net.mudpot.starterkit.commons.policy;

public record PolicyEvaluationRequest(
    String action,
    Object input
) {
}
