package net.mudpot.starterkit.orchestration.system.activities;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.ContentBlock;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.Usage;
import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.completions.CompletionUsage;
import jakarta.inject.Singleton;
import net.mudpot.starterkit.commons.orchestration.ai.activities.LlmActivities;
import net.mudpot.starterkit.commons.ai.model.LlmResponse;
import net.mudpot.starterkit.orchestration.config.TemporalWorkerConfig;
import redis.clients.jedis.JedisPooled;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class LlmActivitiesImpl implements LlmActivities {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String CACHE_NAMESPACE = "llm";
    private static final String DEFAULT_PROVIDER = "openai-compatible";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(45);
    private static final Map<String, CachedValue> MEMORY_CACHE = new ConcurrentHashMap<>();

    private final TemporalWorkerConfig config;
    private final JedisPooled redis;
    private final OpenAIClient openAiClient;
    private final AnthropicClient anthropicClient;

    public LlmActivitiesImpl(final TemporalWorkerConfig config) {
        this.config = config;
        this.redis = createRedisClient(config);
        this.openAiClient = OpenAIOkHttpClient.builder()
            .apiKey(sanitize(config.openAiApiKey()).isBlank() ? "ollama" : sanitize(config.openAiApiKey()))
            .baseUrl(normalizeOpenAiBaseUrl(config.openAiBaseUrl()))
            .timeout(DEFAULT_TIMEOUT)
            .maxRetries(0)
            .build();
        this.anthropicClient = AnthropicOkHttpClient.builder()
            .apiKey(sanitize(config.anthropicApiKey()).isBlank() ? "unset" : sanitize(config.anthropicApiKey()))
            .baseUrl(normalizeAnthropicBaseUrl(config.anthropicBaseUrl()))
            .timeout(DEFAULT_TIMEOUT)
            .maxRetries(0)
            .build();
    }

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
        final String prompt = sanitize(userPrompt);
        if (prompt.isBlank()) {
            throw new RuntimeException("LLM user prompt is required");
        }
        final String chosenProvider = normalizeProvider(provider);
        if (!isOpenAiCompatibleProvider(chosenProvider) && !isAnthropicProvider(chosenProvider)) {
            throw new RuntimeException("Unsupported LLM provider: " + chosenProvider);
        }

        final String chosenModel = sanitize(model).isBlank()
            ? defaultModelForProvider(chosenProvider)
            : sanitize(model);
        if (chosenModel.isBlank()) {
            throw new RuntimeException("LLM model is required");
        }

        final int resolvedMaxTokens = Math.max(1, maxTokens <= 0 ? 800 : maxTokens);
        final String normalizedSystemPrompt = sanitize(systemPrompt);
        final String resolvedCacheKey = resolveCacheKey(
            cacheKey,
            cacheByPromptHash,
            chosenProvider,
            chosenModel,
            normalizedSystemPrompt,
            prompt,
            temperature,
            resolvedMaxTokens
        );
        final int ttlSeconds = Math.max(0, cacheTtlSeconds);
        final boolean useCache = !resolvedCacheKey.isBlank() && ttlSeconds > 0;
        final long now = Instant.now().getEpochSecond();
        if (useCache) {
            final LlmResponse cached = getCached(resolvedCacheKey, now);
            if (cached != null) {
                return cached;
            }
        }

        final ProviderResult providerResult = isAnthropicProvider(chosenProvider)
            ? callAnthropic(prompt, chosenModel, normalizedSystemPrompt, temperature, resolvedMaxTokens)
            : callOpenAiCompatible(prompt, chosenModel, normalizedSystemPrompt, temperature, resolvedMaxTokens);
        final String text = providerResult.text();
        if (text.isBlank()) {
            throw new RuntimeException("LLM returned empty response");
        }

        final Map<String, Object> usage = providerResult.usage();
        final Map<String, Object> cost = new LinkedHashMap<>();
        cost.put("estimated_total_usd", null);
        cost.put("currency", "USD");
        cost.put("pricing_source", "unconfigured");
        final Map<String, Object> missCache = new LinkedHashMap<>();
        missCache.put("hit", false);
        missCache.put("key", useCache ? resolvedCacheKey : null);
        missCache.put("expires_at_epoch", null);

        final LlmResponse response = new LlmResponse(
            chosenProvider,
            chosenModel,
            text,
            usage,
            cost,
            missCache
        );

        if (useCache) {
            final long expiresAt = now + ttlSeconds;
            setCached(resolvedCacheKey, ttlSeconds, expiresAt, response);
            response.getCache().put("expires_at_epoch", expiresAt);
        }
        return response;
    }

    private ProviderResult callOpenAiCompatible(
        final String prompt,
        final String model,
        final String systemPrompt,
        final double temperature,
        final int maxTokens
    ) {
        final ChatCompletionCreateParams.Builder paramsBuilder = ChatCompletionCreateParams.builder()
            .model(model)
            .temperature(temperature)
            .maxTokens((long) maxTokens);
        if (!systemPrompt.isBlank()) {
            paramsBuilder.addSystemMessage(systemPrompt);
        }
        paramsBuilder.addUserMessage(prompt);

        final ChatCompletion completion = openAiClient.chat().completions().create(paramsBuilder.build());
        final String text = completion.choices().isEmpty()
            ? ""
            : completion.choices().get(0).message().content().orElse("").trim();
        return new ProviderResult(text, extractUsage(completion.usage().orElse(null)));
    }

    private ProviderResult callAnthropic(
        final String prompt,
        final String model,
        final String systemPrompt,
        final double temperature,
        final int maxTokens
    ) {
        if (sanitize(config.anthropicApiKey()).isBlank()) {
            throw new RuntimeException("AI_ANTHROPIC_API_KEY is not set");
        }

        try {
            final MessageCreateParams.Builder builder = MessageCreateParams.builder()
                .model(model)
                .maxTokens((long) maxTokens)
                .temperature(temperature)
                .addUserMessage(prompt);
            if (!systemPrompt.isBlank()) {
                builder.system(systemPrompt);
            }
            final Message message = anthropicClient.messages().create(builder.build());
            final StringBuilder text = new StringBuilder();
            for (final ContentBlock block : message.content()) {
                if (!block.isText()) {
                    continue;
                }
                final String blockText = block.asText().text().trim();
                if (blockText.isBlank()) {
                    continue;
                }
                if (text.length() > 0) {
                    text.append('\n');
                }
                text.append(blockText);
            }
            final Usage usage = message.usage();
            final Map<String, Object> usageMap = new LinkedHashMap<>();
            usageMap.put("input_tokens", usage.inputTokens());
            usageMap.put("output_tokens", usage.outputTokens());
            usageMap.put("cache_creation_input_tokens", usage.cacheCreationInputTokens().orElse(0L));
            usageMap.put("cache_read_input_tokens", usage.cacheReadInputTokens().orElse(0L));
            return new ProviderResult(text.toString().trim(), usageMap);
        } catch (final Exception ex) {
            throw new RuntimeException("Anthropic request failed", ex);
        }
    }

    private LlmResponse getCached(final String cacheKey, final long nowEpoch) {
        final String key = cacheStorageKey(cacheKey);
        final Map<String, Object> payload = redis != null ? redisGet(key) : memoryGet(key, nowEpoch);
        if (payload == null) {
            return null;
        }

        final Number expiresAt = asNumber(payload.get("expires_at_epoch"));
        if (expiresAt == null || expiresAt.longValue() <= nowEpoch) {
            return null;
        }
        final Object rawResponse = payload.get("response");
        if (!(rawResponse instanceof Map<?, ?> responseMap)) {
            return null;
        }
        final LlmResponse base = OBJECT_MAPPER.convertValue(responseMap, LlmResponse.class);
        final LlmResponse cached = new LlmResponse(
            base.getProvider(),
            base.getModel(),
            base.getText(),
            zeroUsage(base.getUsage()),
            zeroCost(base.getCost()),
            new LinkedHashMap<>()
        );
        cached.getCache().put("hit", true);
        cached.getCache().put("key", cacheKey);
        cached.getCache().put("expires_at_epoch", expiresAt.longValue());
        return cached;
    }

    private void setCached(final String cacheKey, final int ttlSeconds, final long expiresAt, final LlmResponse response) {
        final String key = cacheStorageKey(cacheKey);
        final Map<String, Object> toStore = new LinkedHashMap<>();
        toStore.put("expires_at_epoch", expiresAt);
        toStore.put("response", OBJECT_MAPPER.convertValue(response, new TypeReference<Map<String, Object>>() { }));
        if (redis != null) {
            redisSet(key, toStore, ttlSeconds);
            return;
        }
        MEMORY_CACHE.put(key, new CachedValue(toStore, expiresAt));
    }

    private Map<String, Object> extractUsage(final CompletionUsage usage) {
        if (usage == null) {
            return Map.of();
        }
        final Map<String, Object> out = new LinkedHashMap<>();
        out.put("input_tokens", usage.promptTokens());
        out.put("output_tokens", usage.completionTokens());
        out.put("total_tokens", usage.totalTokens());
        return out;
    }

    private static Map<String, Object> zeroUsage(final Map<String, Object> usage) {
        if (usage == null || usage.isEmpty()) {
            return Map.of();
        }
        final Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : usage.entrySet()) {
            if (entry.getValue() instanceof Number) {
                out.put(entry.getKey(), 0);
            }
        }
        return out;
    }

    private static Map<String, Object> zeroCost(final Map<String, Object> cost) {
        final Map<String, Object> out = new LinkedHashMap<>();
        out.put("estimated_total_usd", 0.0);
        out.put("currency", cost == null ? "USD" : cost.getOrDefault("currency", "USD"));
        out.put("pricing_source", "cached");
        out.put("components_usd", Map.of(
            "input", 0.0,
            "output", 0.0,
            "cache_read", 0.0,
            "cache_write", 0.0
        ));
        return out;
    }

    private static String resolveCacheKey(
        final String cacheKey,
        final boolean cacheByPromptHash,
        final String provider,
        final String model,
        final String systemPrompt,
        final String userPrompt,
        final double temperature,
        final int maxTokens
    ) {
        final String explicit = sanitize(cacheKey);
        if (!explicit.isBlank()) {
            return explicit;
        }
        if (!cacheByPromptHash) {
            return "";
        }
        final String canonical = provider + "|" + model + "|" + systemPrompt + "|" + userPrompt + "|" + temperature + "|" + maxTokens;
        return "prompt-hash:" + sha256Hex(canonical);
    }

    private static String cacheStorageKey(final String cacheKey) {
        return CACHE_NAMESPACE + ":" + sha256Hex(cacheKey);
    }

    private static String sha256Hex(final String text) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to hash cache key", ex);
        }
    }

    private Map<String, Object> redisGet(final String key) {
        try {
            final String raw = redis.get(key);
            if (raw == null || raw.isBlank()) {
                return null;
            }
            return OBJECT_MAPPER.readValue(raw, new TypeReference<>() { });
        } catch (Exception ignored) {
            return null;
        }
    }

    private void redisSet(final String key, final Map<String, Object> payload, final int ttlSeconds) {
        try {
            redis.setex(key, Math.max(1, ttlSeconds), OBJECT_MAPPER.writeValueAsString(payload));
        } catch (Exception ignored) {
            // Best effort only.
        }
    }

    private Map<String, Object> memoryGet(final String key, final long nowEpoch) {
        final CachedValue cached = MEMORY_CACHE.get(key);
        if (cached == null) {
            return null;
        }
        if (cached.expiresAtEpochSeconds() <= nowEpoch) {
            MEMORY_CACHE.remove(key);
            return null;
        }
        return new LinkedHashMap<>(cached.payload());
    }

    private static Number asNumber(final Object value) {
        return value instanceof Number number ? number : null;
    }

    private String normalizeProvider(final String provider) {
        final String normalized = sanitize(provider).toLowerCase();
        if (!normalized.isBlank()) {
            return normalized;
        }
        final String configured = sanitize(config.aiDefaultProvider()).toLowerCase();
        return configured.isBlank() ? DEFAULT_PROVIDER : configured;
    }

    private static boolean isOpenAiCompatibleProvider(final String provider) {
        return "openai-compatible".equals(provider)
            || "openai_compatible".equals(provider)
            || "openai".equals(provider);
    }

    private static boolean isAnthropicProvider(final String provider) {
        return "anthropic".equals(provider);
    }

    private String defaultModelForProvider(final String provider) {
        if (isAnthropicProvider(provider)) {
            return sanitize(config.anthropicDefaultModel());
        }
        return sanitize(config.openAiDefaultModel());
    }

    private static JedisPooled createRedisClient(final TemporalWorkerConfig config) {
        if (!config.valkeyEnabled()) {
            return null;
        }
        try {
            return new JedisPooled(config.valkeyHost(), config.valkeyPort());
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String normalizeOpenAiBaseUrl(final String baseUrl) {
        String value = sanitize(baseUrl);
        if (value.startsWith("//")) {
            value = "http:" + value;
        }
        if (value.isBlank() || !value.contains("://")) {
            value = "http://127.0.0.1:1234/v1";
        }
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private static String normalizeAnthropicBaseUrl(final String baseUrl) {
        String value = sanitize(baseUrl);
        if (value.startsWith("//")) {
            value = "https:" + value;
        }
        if (value.isBlank() || !value.contains("://")) {
            value = "https://api.anthropic.com/v1/messages";
        }
        if (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        if (value.endsWith("/messages")) {
            return value.substring(0, value.length() - "/messages".length());
        }
        return value;
    }

    private static String sanitize(final String value) {
        return value == null ? "" : value.trim();
    }

    private record ProviderResult(String text, Map<String, Object> usage) { }

    private record CachedValue(Map<String, Object> payload, long expiresAtEpochSeconds) { }
}
