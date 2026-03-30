package net.mudpot.starterkit.policyservice;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationRequest;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationResult;
import net.mudpot.starterkit.commons.policy.PolicyEvaluator;

@Controller("/v1/policy")
@ExecuteOn(TaskExecutors.BLOCKING)
public class PolicyController {
    private final PolicyEvaluator policyEvaluator;

    public PolicyController(final PolicyEvaluator policyEvaluator) {
        this.policyEvaluator = policyEvaluator;
    }

    @Post("/evaluate")
    public PolicyEvaluationResult evaluate(@Body final PolicyEvaluationRequest request) {
        return policyEvaluator.evaluate(request);
    }
}
