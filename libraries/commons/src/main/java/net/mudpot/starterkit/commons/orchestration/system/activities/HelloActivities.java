package net.mudpot.starterkit.commons.orchestration.system.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import net.mudpot.starterkit.commons.ai.model.LlmResponse;
import net.mudpot.starterkit.commons.ai.model.PromptBundle;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldResult;

@ActivityInterface
public interface HelloActivities {
    @ActivityMethod
    HelloWorldResult completeHello(String workflowId, String name, String useCase, PromptBundle prompt, LlmResponse llmResponse);
}
