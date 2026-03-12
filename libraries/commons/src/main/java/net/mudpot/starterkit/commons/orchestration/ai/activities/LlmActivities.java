package net.mudpot.starterkit.commons.orchestration.ai.activities;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import net.mudpot.starterkit.commons.ai.model.LlmResponse;

@ActivityInterface
public interface LlmActivities {
    @ActivityMethod
    LlmResponse callLlm(
        String userPrompt,
        String provider,
        String model,
        String systemPrompt,
        double temperature,
        int maxTokens,
        String cacheKey,
        int cacheTtlSeconds,
        boolean cacheByPromptHash
    );
}
