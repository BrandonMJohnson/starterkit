package net.mudpot.starterkit.apiservice.controllers;

import io.micronaut.http.HttpRequest;
import io.micronaut.context.BeanProvider;
import net.mudpot.starterkit.apiservice.policy.RequirePolicy;
import net.mudpot.starterkit.apiservice.session.AnonymousSession;
import net.mudpot.starterkit.apiservice.session.AnonymousSessionContext;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldRequest;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldResult;
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
    @Test
    void runUsesWorkflowClientAndAnonymousSession() {
        final StubHelloWorldWorkflowClient client = new StubHelloWorldWorkflowClient();
        client.runResponse = new HelloWorldResult("wf-1", "starter_hello_v1", "Hello there.", "openai-compatible", "demo", Map.of(), Map.of(), Instant.parse("2026-03-12T00:00:00Z"));
        final HelloWorkflowController controller = new HelloWorkflowController(
            client,
            new StubHelloHistoryQueryService(),
            anonymousSessionContext()
        );

        final HelloWorldResult response = controller.run(
            new HelloWorldRequest("Brandon", "Build a club operations platform.", "")
        ).body();

        assertEquals("Brandon", client.runName);
        assertEquals("Build a club operations platform.", client.runUseCase);
        assertEquals("anonymous", client.runActorKind);
        assertEquals("anon-session-1", client.runSessionId);
        assertEquals("Hello there.", response.greeting());
    }

    @Test
    void methodsDeclareRecoveredPolicyAnnotations() throws Exception {
        assertPolicyAction("run", "workflow.hello_world.run", HelloWorldRequest.class);
        assertPolicyAction("start", "workflow.hello_world.start", HelloWorldRequest.class);
        assertPolicyAction("history", "workflow.hello_world.history", int.class);
    }

    private static final class StubHelloWorldWorkflowClient extends HelloWorldWorkflowClient {
        private String runName;
        private String runUseCase;
        private String runActorKind;
        private String runSessionId;
        private HelloWorldResult runResponse;

        private StubHelloWorldWorkflowClient() {
            super(null);
        }

        @Override
        public HelloWorldResult run(final String name, final String useCase, final String actorKind, final String sessionId) {
            this.runName = name;
            this.runUseCase = useCase;
            this.runActorKind = actorKind;
            this.runSessionId = sessionId;
            return runResponse;
        }

        @Override
        public WorkflowStartResponse start(
            final String name,
            final String useCase,
            final String workflowId,
            final String actorKind,
            final String sessionId
        ) {
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

    private static BeanProvider<AnonymousSessionContext> anonymousSessionContext() {
        return new BeanProvider<>() {
            @Override
            public AnonymousSessionContext get() {
                return new AnonymousSessionContext(
                    new AnonymousSession("anon-session-1", "anonymous", Instant.parse("2026-03-12T00:00:00Z"), false)
                );
            }
        };
    }

    private static void assertPolicyAction(
        final String methodName,
        final String expectedAction,
        final Class<?>... parameterTypes
    ) throws NoSuchMethodException {
        final Method method = HelloWorkflowController.class.getMethod(methodName, parameterTypes);
        final RequirePolicy annotation = method.getAnnotation(RequirePolicy.class);
        assertNotNull(annotation);
        assertEquals(expectedAction, annotation.value());
    }
}
