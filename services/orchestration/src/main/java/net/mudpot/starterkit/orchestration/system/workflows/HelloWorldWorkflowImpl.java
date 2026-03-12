package net.mudpot.starterkit.orchestration.system.workflows;

import io.micronaut.context.annotation.Prototype;
import io.temporal.workflow.Workflow;
import jakarta.inject.Named;
import net.mudpot.starterkit.commons.ai.model.LlmResponse;
import net.mudpot.starterkit.commons.ai.model.PromptBundle;
import net.mudpot.starterkit.commons.orchestration.ai.activities.LlmActivities;
import net.mudpot.starterkit.commons.orchestration.ai.activities.PromptActivities;
import net.mudpot.starterkit.commons.orchestration.system.activities.HelloActivities;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldResult;
import net.mudpot.starterkit.commons.orchestration.system.workflows.HelloWorldWorkflow;

import java.util.Map;

@Prototype
public class HelloWorldWorkflowImpl implements HelloWorldWorkflow {
    private final HelloActivities helloActivities;
    private final PromptActivities promptActivities;
    private final LlmActivities llmActivities;

    public HelloWorldWorkflowImpl(
        @Named("helloActivitiesStub") final HelloActivities helloActivities,
        @Named("promptActivitiesStub") final PromptActivities promptActivities,
        @Named("llmActivitiesStub") final LlmActivities llmActivities
    ) {
        this.helloActivities = helloActivities;
        this.promptActivities = promptActivities;
        this.llmActivities = llmActivities;
    }

    @Override
    public HelloWorldResult run(final String name, final String useCase) {
        final String normalizedName = name == null || name.isBlank() ? "World" : name.trim();
        final String normalizedUseCase = useCase == null || useCase.isBlank()
            ? "Demonstrate the StarterKit platform baseline."
            : useCase.trim();
        final PromptBundle prompt = promptActivities.renderPrompt("starter_hello_v1", Map.of(
            "name", normalizedName,
            "use_case", normalizedUseCase
        ));
        final LlmResponse llmResponse = llmActivities.callLlm(
            prompt.getUserPrompt(),
            null,
            null,
            prompt.getSystemPrompt(),
            0.3,
            500,
            "hello:" + normalizedName.toLowerCase() + ":" + Integer.toHexString(normalizedUseCase.hashCode()),
            300,
            false
        );
        return helloActivities.completeHello(
            Workflow.getInfo().getWorkflowId(),
            normalizedName,
            normalizedUseCase,
            prompt,
            llmResponse
        );
    }
}
