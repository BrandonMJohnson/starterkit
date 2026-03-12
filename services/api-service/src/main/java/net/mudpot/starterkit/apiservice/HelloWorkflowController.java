package net.mudpot.starterkit.apiservice;

import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloHistoryEntry;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldRequest;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldResult;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationRequest;
import net.mudpot.starterkit.commons.policy.PolicyEvaluationResult;
import net.mudpot.starterkit.commons.policy.PolicyEvaluator;
import net.mudpot.starterkit.orchestrationclients.model.WorkflowStartResponse;
import net.mudpot.starterkit.orchestrationclients.system.HelloWorldWorkflowClient;

import java.util.List;
import java.util.Map;

@Controller("/api/workflows/hello-world")
@ExecuteOn(TaskExecutors.BLOCKING)
public class HelloWorkflowController {
    private final HelloWorldWorkflowClient helloWorldWorkflowClient;
    private final HelloHistoryQueryService helloHistoryQueryService;
    private final PolicyEvaluator policyEvaluator;

    public HelloWorkflowController(
        final HelloWorldWorkflowClient helloWorldWorkflowClient,
        final HelloHistoryQueryService helloHistoryQueryService,
        final PolicyEvaluator policyEvaluator
    ) {
        this.helloWorldWorkflowClient = helloWorldWorkflowClient;
        this.helloHistoryQueryService = helloHistoryQueryService;
        this.policyEvaluator = policyEvaluator;
    }

    @Post("/run")
    public HelloWorldResult run(@Body final HelloWorldRequest request) {
        final HelloWorldRequest normalized = normalizedRequest(request);
        requirePolicy("workflow.hello_world.run", normalized);
        return helloWorldWorkflowClient.run(normalized.name(), normalized.useCase());
    }

    @Post("/start")
    public WorkflowStartResponse start(@Body final HelloWorldRequest request) {
        final HelloWorldRequest normalized = normalizedRequest(request);
        requirePolicy("workflow.hello_world.start", normalized);
        return helloWorldWorkflowClient.start(normalized.name(), normalized.useCase(), normalized.workflowId());
    }

    @Get("/history")
    public List<HelloHistoryEntry> history(@QueryValue(defaultValue = "12") final int limit) {
        final int resolvedLimit = Math.max(1, Math.min(limit, 50));
        requirePolicy("workflow.hello_world.history", null);
        return helloHistoryQueryService.recent(resolvedLimit);
    }

    private void requirePolicy(final String action, final HelloWorldRequest request) {
        final Map<String, Object> input = request == null
            ? Map.of(
                "actor", Map.of("role", "builder"),
                "resource", Map.of("type", "hello-world", "scope", "history")
            )
            : Map.of(
                "actor", Map.of("role", "builder"),
                "resource", Map.of("type", "hello-world", "scope", "workflow"),
                "name", request.name(),
                "use_case", request.useCase()
            );
        final PolicyEvaluationResult result = policyEvaluator.evaluate(new PolicyEvaluationRequest(action, input));
        if (!result.allowed()) {
            throw new HttpStatusException(HttpStatus.FORBIDDEN, "Policy denied: " + result.reason());
        }
    }

    private static HelloWorldRequest normalizedRequest(final HelloWorldRequest request) {
        if (request == null) {
            return new HelloWorldRequest("World", "Demonstrate the StarterKit platform baseline.", "");
        }
        final String name = request.name() == null || request.name().isBlank() ? "World" : request.name().trim();
        final String useCase = request.useCase() == null || request.useCase().isBlank()
            ? "Demonstrate the StarterKit platform baseline."
            : request.useCase().trim();
        final String workflowId = request.workflowId() == null ? "" : request.workflowId().trim();
        return new HelloWorldRequest(name, useCase, workflowId);
    }
}
