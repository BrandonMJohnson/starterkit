package net.mudpot.starterkit.commons.orchestration.policy.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationRequest;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationResult;

@ActivityInterface
public interface PolicyEvaluationActivities {
    @ActivityMethod
    PolicyEvaluationResult evaluatePolicy(PolicyEvaluationRequest request);
}
