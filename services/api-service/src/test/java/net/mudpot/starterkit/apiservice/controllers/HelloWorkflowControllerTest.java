package net.mudpot.starterkit.apiservice.controllers;

import net.mudpot.starterkit.apiservice.policy.AuthPolicy;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldRequest;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldResult;
import net.mudpot.starterkit.commons.session.Session;
import net.mudpot.starterkit.orchestrationclients.model.WorkflowStartResponse;
import net.mudpot.starterkit.orchestrationclients.system.HelloWorldWorkflowClient;
import net.mudpot.starterkit.persistence.history.HelloHistoryQueryService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class HelloWorkflowControllerTest {
    private static final Session TEST_SESSION = new Session("anon-session-1", "anonymous", Instant.parse("2026-03-12T00:00:00Z"), false);

    @Test
    void runUsesWorkflowClientAndSession() {
        final StubHelloWorldWorkflowClient client = new StubHelloWorldWorkflowClient();
        client.runResponse = new HelloWorldResult("wf-1", "starter_hello_v1", "Hello there.", "openai-compatible", "demo", Map.of(), Map.of(), Instant.parse("2026-03-12T00:00:00Z"));
        final HelloWorkflowController controller = new HelloWorkflowController(
            client,
            new StubHelloHistoryQueryService()
        );

        final HelloWorldResult response = controller.run(
            new HelloWorldRequest("Brandon", "Build a club operations platform.", ""),
            TEST_SESSION
        );

        assertEquals("Brandon", client.runRequest.name());
        assertEquals("Build a club operations platform.", client.runRequest.useCase());
        assertEquals("anonymous", client.runSession.actorKind());
        assertEquals("anon-session-1", client.runSession.sessionId());
        assertEquals("Hello there.", response.greeting());
    }

    @Test
    void methodsDeclareRecoveredPolicyAnnotations() throws Exception {
        assertPolicyAction("run", "api.hello_world.run", HelloWorldRequest.class, Session.class);
        assertPolicyAction("start", "api.hello_world.start", HelloWorldRequest.class, Session.class);
        assertPolicyAction("history", "api.hello_world.history", int.class, Session.class);
    }

    private static final class StubHelloWorldWorkflowClient extends HelloWorldWorkflowClient {
        private HelloWorldRequest runRequest;
        private Session runSession;
        private HelloWorldResult runResponse;

        private StubHelloWorldWorkflowClient() {
            super(null);
        }

        @Override
        public HelloWorldResult run(final HelloWorldRequest request, final Session session) {
            this.runRequest = request;
            this.runSession = session;
            return runResponse;
        }

        @Override
        public WorkflowStartResponse start(final HelloWorldRequest request, final Session session) {
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

    private static void assertPolicyAction(
        final String methodName,
        final String expectedAction,
        final Class<?>... parameterTypes
    ) throws NoSuchMethodException {
        final Method method = HelloWorkflowController.class.getMethod(methodName, parameterTypes);
        final AuthPolicy annotation = method.getAnnotation(AuthPolicy.class);
        assertNotNull(annotation);
        assertEquals(expectedAction, annotation.value());
    }
}
