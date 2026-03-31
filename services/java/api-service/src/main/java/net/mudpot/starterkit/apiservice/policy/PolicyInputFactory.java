package net.mudpot.starterkit.apiservice.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpRequest;
import jakarta.inject.Singleton;
import net.mudpot.starterkit.commons.session.Session;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Singleton
public class PolicyInputFactory {
    private final ObjectMapper objectMapper;

    public PolicyInputFactory(final ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> build(final HttpRequest<?> request, final Session session) {
        final Map<String, Object> input = new LinkedHashMap<>();
        input.put("actor", Map.of(
            "kind", session.actorKind(),
            "session_id", session.sessionId()
        ));
        input.put("resource", Map.of(
            "type", "api",
            "path", request.getPath()
        ));
        input.put("request", Map.of(
            "method", request.getMethodName(),
            "path", request.getPath(),
            "query", queryPayload(request),
            "body", bodyPayload(request)
        ));
        return input;
    }

    private static Map<String, Object> queryPayload(final HttpRequest<?> request) {
        final Map<String, Object> query = new LinkedHashMap<>();
        request.getParameters().forEach((name, values) -> {
            if (values == null || values.isEmpty()) {
                return;
            }
            query.put(name, values.size() == 1 ? values.getFirst() : List.copyOf(values));
        });
        return query;
    }

    private Object bodyPayload(final HttpRequest<?> request) {
        return request.getBody()
            .map(this::toPolicyValue)
            .orElse(Map.of());
    }

    private Object toPolicyValue(final Object value) {
        if (value == null || value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value;
        }
        if (value instanceof Map<?, ?> mapValue) {
            final Map<String, Object> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                out.put(snakeCase(String.valueOf(entry.getKey())), toPolicyValue(entry.getValue()));
            }
            return out;
        }
        if (value instanceof Iterable<?> iterable) {
            final java.util.ArrayList<Object> out = new java.util.ArrayList<>();
            for (Object item : iterable) {
                out.add(toPolicyValue(item));
            }
            return out;
        }
        if (value.getClass().isArray()) {
            final int length = java.lang.reflect.Array.getLength(value);
            final java.util.ArrayList<Object> out = new java.util.ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                out.add(toPolicyValue(java.lang.reflect.Array.get(value, i)));
            }
            return out;
        }
        return toPolicyValue(objectMapper.convertValue(value, Map.class));
    }

    private static String snakeCase(final String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        final String normalized = value
            .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
            .replace('-', '_')
            .replace(' ', '_');
        return normalized.toLowerCase(Locale.ROOT);
    }
}
