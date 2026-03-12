package net.mudpot.starterkit.apiservice;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.exceptions.HttpStatusException;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldRequest;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldResult;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationRequest;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationResult;
import net.mudpot.starterkit.commons.policy.PolicyEvaluator;
import net.mudpot.starterkit.orchestrationclients.model.WorkflowStartResponse;
import net.mudpot.starterkit.orchestrationclients.system.HelloWorldWorkflowClient;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HelloWorkflowControllerTest {
    @Test
    void runUsesWorkflowClientWhenPolicyAllows() {
        final StubHelloWorldWorkflowClient client = new StubHelloWorldWorkflowClient();
        client.runResponse = new HelloWorldResult("wf-1", "starter_hello_v1", "Hello there.", "openai-compatible", "demo", Map.of(), Map.of(), Instant.parse("2026-03-12T00:00:00Z"));
        final HelloWorkflowController controller = new HelloWorkflowController(client, new StubHelloHistoryQueryService(), allowAll());

        final HelloWorldResult response = controller.run(new HelloWorldRequest("Brandon", "Build a club operations platform.", ""));

        assertEquals("Brandon", client.runName);
        assertEquals("Build a club operations platform.", client.runUseCase);
        assertEquals("Hello there.", response.greeting());
    }

    @Test
    void historyRejectsDeniedPolicy() {
        final HelloWorkflowController controller = new HelloWorkflowController(
            new StubHelloWorldWorkflowClient(),
            new StubHelloHistoryQueryService(),
            request -> new PolicyEvaluationResult(false, "denied", "starterkit.v1")
        );

        final HttpStatusException exception = assertThrows(HttpStatusException.class, () -> controller.history(10));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    private static PolicyEvaluator allowAll() {
        return request -> new PolicyEvaluationResult(true, "allowed", "starterkit.v1");
    }

    private static final class StubHelloWorldWorkflowClient extends HelloWorldWorkflowClient {
        private String runName;
        private String runUseCase;
        private HelloWorldResult runResponse;

        private StubHelloWorldWorkflowClient() {
            super(null);
        }

        @Override
        public HelloWorldResult run(final String name, final String useCase) {
            this.runName = name;
            this.runUseCase = useCase;
            return runResponse;
        }

        @Override
        public WorkflowStartResponse start(final String name, final String useCase, final String workflowId) {
            return new WorkflowStartResponse("HelloWorldWorkflow", "wf-1", "hello-world-task-queue", "run-1");
        }
    }

    private static final class StubHelloHistoryQueryService extends HelloHistoryQueryService {
        private StubHelloHistoryQueryService() {
            super(null);
        }

        @Override
        public List<net.mudpot.starterkit.commons.orchestration.system.model.HelloHistoryEntry> recent(final int limit) {
            return List.of();
        }
    }
}
