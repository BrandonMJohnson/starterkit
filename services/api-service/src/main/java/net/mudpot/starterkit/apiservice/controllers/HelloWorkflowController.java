package net.mudpot.starterkit.apiservice.controllers;

import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import net.mudpot.starterkit.apiservice.policy.AuthPolicy;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloHistoryEntry;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldRequest;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldResult;
import net.mudpot.starterkit.commons.session.Session;
import net.mudpot.starterkit.orchestrationclients.model.WorkflowStartResponse;
import net.mudpot.starterkit.orchestrationclients.system.HelloWorldWorkflowClient;
import net.mudpot.starterkit.persistence.history.HelloHistoryQueryService;

import java.util.List;

@Controller("/api/workflows/hello-world")
@ExecuteOn(TaskExecutors.BLOCKING)
public class HelloWorkflowController {
    private final HelloWorldWorkflowClient helloWorldWorkflowClient;
    private final HelloHistoryQueryService helloHistoryQueryService;

    public HelloWorkflowController(
        final HelloWorldWorkflowClient helloWorldWorkflowClient,
        final HelloHistoryQueryService helloHistoryQueryService
    ) {
        this.helloWorldWorkflowClient = helloWorldWorkflowClient;
        this.helloHistoryQueryService = helloHistoryQueryService;
    }

    @Post("/run")
    @AuthPolicy("api.hello_world.run")
    public HelloWorldResult run(@Body final HelloWorldRequest request, final Session session) {
        final HelloWorldRequest normalized = normalizedRequest(request);
        return helloWorldWorkflowClient.run(normalized, session);
    }

    @Post("/start")
    @AuthPolicy("api.hello_world.start")
    public WorkflowStartResponse start(@Body final HelloWorldRequest request, final Session session) {
        final HelloWorldRequest normalized = normalizedRequest(request);
        return helloWorldWorkflowClient.start(normalized, session);
    }

    @Get("/history")
    @AuthPolicy("api.hello_world.history")
    public List<HelloHistoryEntry> history(@QueryValue(defaultValue = "12") final int limit, final Session session) {
        final int resolvedLimit = Math.max(1, Math.min(limit, 50));
        return helloHistoryQueryService.recent(resolvedLimit);
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
