package net.mudpot.starterkit.policyservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationRequest;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationResult;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpaSidecarClientTest {
    @Test
    void returnsStructuredDecisionFromPolicyPackage() {
        final OpaSidecarHttpClient httpClient = request -> Map.of(
            "result", Map.of(
                "allow", true,
                "reason", "action-allowed",
                "policy_version", "starterkit.v1"
            )
        );
        final OpaSidecarClient client = new OpaSidecarClient(new ObjectMapper(), httpClient);

        final PolicyEvaluationResult result = client.evaluate(new PolicyEvaluationRequest(
            "workflow.hello_world.run",
            Map.of("name", "Builder")
        ));

        assertTrue(result.allowed());
        assertEquals("action-allowed", result.reason());
        assertEquals("starterkit.v1", result.policyVersion());
    }
}
