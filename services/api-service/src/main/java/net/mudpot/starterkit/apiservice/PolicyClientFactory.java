package net.mudpot.starterkit.apiservice;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import jakarta.inject.Singleton;
import net.mudpot.starterkit.commons.policy.PolicyEvaluator;
import net.mudpot.starterkit.commons.policy.client.PolicyServiceClient;

@Factory
public class PolicyClientFactory {
    @Singleton
    public PolicyEvaluator policyEvaluator(
        @Value("${policy.service-url}") final String policyServiceUrl,
        @Value("${policy.service-enforce:true}") final boolean enforce
    ) {
        return new PolicyServiceClient(policyServiceUrl, enforce);
    }
}
