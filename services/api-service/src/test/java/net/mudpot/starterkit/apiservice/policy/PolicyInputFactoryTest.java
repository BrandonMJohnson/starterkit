package net.mudpot.starterkit.apiservice.policy;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.http.HttpRequest;
import net.mudpot.starterkit.apiservice.session.AnonymousSession;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldRequest;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PolicyInputFactoryTest {
    private final PolicyInputFactory policyInputFactory = new PolicyInputFactory(new ObjectMapper());

    @Test
    void buildsActorRequestAndSnakeCaseBody() {
        final HttpRequest<HelloWorldRequest> request = HttpRequest.POST(
            "/api/workflows/hello-world/run?limit=8",
            new HelloWorldRequest("Builder", "Understand the first durable slice.", "wf-1")
        );

        final Map<String, Object> input = policyInputFactory.build(
            request,
            new AnonymousSession("anon-session-1", "anonymous", Instant.parse("2026-03-12T00:00:00Z"), false)
        );

        final Map<?, ?> actor = assertInstanceOf(Map.class, input.get("actor"));
        final Map<?, ?> resource = assertInstanceOf(Map.class, input.get("resource"));
        final Map<?, ?> requestPayload = assertInstanceOf(Map.class, input.get("request"));
        final Map<?, ?> body = assertInstanceOf(Map.class, requestPayload.get("body"));
        final Map<?, ?> query = assertInstanceOf(Map.class, requestPayload.get("query"));

        assertEquals("anonymous", actor.get("kind"));
        assertEquals("anon-session-1", actor.get("session_id"));
        assertEquals("api", resource.get("type"));
        assertEquals("/api/workflows/hello-world/run", resource.get("path"));
        assertEquals("POST", requestPayload.get("method"));
        assertEquals("/api/workflows/hello-world/run", requestPayload.get("path"));
        assertEquals("Builder", body.get("name"));
        assertEquals("Understand the first durable slice.", body.get("use_case"));
        assertEquals("wf-1", body.get("workflow_id"));
        assertEquals("8", query.get("limit"));
    }
}
