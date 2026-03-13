package net.mudpot.starterkit.apiservice.config;

import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Value;
import io.opentelemetry.api.OpenTelemetry;
import jakarta.inject.Singleton;
import net.mudpot.starterkit.commons.policy.PolicyEvaluator;
import net.mudpot.starterkit.commons.policy.client.PolicyServiceClient;

@Factory
public class PolicyClientFactory {
    @Singleton
    public PolicyEvaluator policyEvaluator(
        final OpenTelemetry openTelemetry,
        @Value("${policy.service-url}") final String policyServiceUrl,
        @Value("${policy.service-enforce:true}") final boolean enforce
    ) {
        return new PolicyServiceClient(openTelemetry, policyServiceUrl, enforce);
    }
}
