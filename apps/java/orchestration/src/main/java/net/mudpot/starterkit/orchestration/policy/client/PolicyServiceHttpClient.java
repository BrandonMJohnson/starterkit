package net.mudpot.starterkit.orchestration.policy.client;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationRequest;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationResult;

@Client("${policy.service-url}")
public interface PolicyServiceHttpClient {
    @Post("/v1/policy/evaluate")
    PolicyEvaluationResult evaluate(@Body PolicyEvaluationRequest request);
}
