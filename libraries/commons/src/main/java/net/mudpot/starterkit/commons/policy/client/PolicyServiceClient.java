package net.mudpot.starterkit.commons.policy.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationRequest;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationResult;
import net.mudpot.starterkit.commons.policy.PolicyEvaluator;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class PolicyServiceClient implements PolicyEvaluator {
    private static final int MAX_ATTEMPTS = 3;

    private final HttpClient httpClient = HttpClient.newBuilder().build();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String policyServiceUrl;
    private final boolean enforce;

    public PolicyServiceClient(final String policyServiceUrl, final boolean enforce) {
        this.policyServiceUrl = policyServiceUrl;
        this.enforce = enforce;
    }

    @Override
    public PolicyEvaluationResult evaluate(final PolicyEvaluationRequest request) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                final HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(policyServiceUrl + "/v1/policy/evaluate"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(request)))
                    .build();
                final HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return objectMapper.readValue(response.body(), PolicyEvaluationResult.class);
                }
                if (response.statusCode() < 500 || attempt == MAX_ATTEMPTS) {
                    return enforcementAwareFailure("policy-service-http-" + response.statusCode());
                }
            } catch (final Exception exception) {
                if (attempt == MAX_ATTEMPTS) {
                    return enforcementAwareFailure("policy-service-error");
                }
            }
        }
        return enforcementAwareFailure("policy-service-error");
    }

    private PolicyEvaluationResult enforcementAwareFailure(final String reason) {
        if (!enforce) {
            return new PolicyEvaluationResult(true, reason + "-fail-open", "policy-service");
        }
        return new PolicyEvaluationResult(false, reason, "policy-service");
    }
}
