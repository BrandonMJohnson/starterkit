package net.mudpot.starterkit.orchestration.system.workflows;

import io.temporal.client.WorkflowOptions;
import io.temporal.testing.TestWorkflowEnvironment;
import net.mudpot.starterkit.commons.ai.model.LlmResponse;
import net.mudpot.starterkit.commons.ai.model.PromptBundle;
import net.mudpot.starterkit.commons.orchestration.TaskQueues;
import net.mudpot.starterkit.commons.orchestration.system.activities.HelloActivities;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldResult;
import net.mudpot.starterkit.commons.orchestration.system.workflows.HelloWorldWorkflow;
import net.mudpot.starterkit.commons.orchestration.ai.activities.LlmActivities;
import net.mudpot.starterkit.commons.orchestration.ai.activities.PromptActivities;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HelloWorldWorkflowImplTest {
    @Test
    void runUsesPromptAndLlmActivities() {
        try (TestWorkflowEnvironment environment = TestWorkflowEnvironment.newInstance()) {
            environment.newWorker(TaskQueues.HELLO_WORLD)
                .registerWorkflowImplementationFactory(
                    HelloWorldWorkflow.class,
                    () -> new HelloWorldWorkflowImpl(
                        new StubHelloActivities(),
                        new StubPromptActivities(),
                        new StubLlmActivities()
                    )
                );
            environment.start();

            final HelloWorldWorkflow workflow = environment.getWorkflowClient().newWorkflowStub(
                HelloWorldWorkflow.class,
                WorkflowOptions.newBuilder()
                    .setTaskQueue(TaskQueues.HELLO_WORLD)
                    .setWorkflowId("wf-hello")
                    .build()
            );

            final HelloWorldResult result = workflow.run("Brandon", "Build a flight-club platform.");

            assertEquals("wf-hello", result.workflowId());
            assertEquals("Hello from the LLM.", result.greeting());
            assertEquals("starter_hello_v1", result.promptTemplate());
        }
    }

    private static final class StubHelloActivities implements HelloActivities {
        @Override
        public HelloWorldResult completeHello(
            final String workflowId,
            final String name,
            final String useCase,
            final PromptBundle prompt,
            final LlmResponse llmResponse
        ) {
            return new HelloWorldResult(
                workflowId,
                prompt.getTemplate(),
                llmResponse.getText(),
                llmResponse.getProvider(),
                llmResponse.getModel(),
                llmResponse.getUsage(),
                llmResponse.getCache(),
                Instant.parse("2026-03-12T00:00:00Z")
            );
        }
    }

    private static final class StubPromptActivities implements PromptActivities {
        @Override
        public PromptBundle renderPrompt(final String templateName, final Map<String, Object> variables) {
            return new PromptBundle(templateName, "system", "user", variables);
        }
    }

    private static final class StubLlmActivities implements LlmActivities {
        @Override
        public LlmResponse callLlm(
            final String userPrompt,
            final String provider,
            final String model,
            final String systemPrompt,
            final double temperature,
            final int maxTokens,
            final String cacheKey,
            final int cacheTtlSeconds,
            final boolean cacheByPromptHash
        ) {
            return new LlmResponse("openai-compatible", "demo", "Hello from the LLM.", Map.of(), Map.of(), Map.of("hit", false));
        }
    }
}
