package net.mudpot.starterkit.commons.policy;

public interface PolicyEvaluator {
    PolicyEvaluationResult evaluate(PolicyEvaluationRequest request);
}
