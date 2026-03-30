package net.mudpot.starterkit.commons.policy;

public record PolicyEvaluationResult(
    boolean allowed,
    String reason,
    String policyVersion
) {
}
