package net.mudpot.starterkit.apiservice.controllers;

import io.micronaut.http.HttpStatus;
import io.micronaut.context.BeanProvider;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import net.mudpot.starterkit.apiservice.policy.RequirePolicy;
import net.mudpot.starterkit.apiservice.session.AnonymousSession;
import net.mudpot.starterkit.apiservice.session.AnonymousSessionContext;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloHistoryEntry;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldRequest;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldResult;
import net.mudpot.starterkit.orchestrationclients.model.WorkflowStartResponse;
import net.mudpot.starterkit.orchestrationclients.system.HelloWorldWorkflowClient;
import net.mudpot.starterkit.persistence.history.HelloHistoryQueryService;

import java.util.List;

@Controller("/api/workflows/hello-world")
@ExecuteOn(TaskExecutors.BLOCKING)
public class HelloWorkflowController {
    private final HelloWorldWorkflowClient helloWorldWorkflowClient;
    private final HelloHistoryQueryService helloHistoryQueryService;
    private final BeanProvider<AnonymousSessionContext> anonymousSessionContext;

    public HelloWorkflowController(
        final HelloWorldWorkflowClient helloWorldWorkflowClient,
        final HelloHistoryQueryService helloHistoryQueryService,
        final BeanProvider<AnonymousSessionContext> anonymousSessionContext
    ) {
        this.helloWorldWorkflowClient = helloWorldWorkflowClient;
        this.helloHistoryQueryService = helloHistoryQueryService;
        this.anonymousSessionContext = anonymousSessionContext;
    }

    @Post("/run")
    @RequirePolicy("workflow.hello_world.run")
    public HttpResponse<HelloWorldResult> run(@Body final HelloWorldRequest request) {
        final HelloWorldRequest normalized = normalizedRequest(request);
        final AnonymousSession session = anonymousSessionContext.get().session();
        return HttpResponse.ok(helloWorldWorkflowClient.run(normalized.name(), normalized.useCase(), session.actorKind(), session.sessionId()));
    }

    @Post("/start")
    @RequirePolicy("workflow.hello_world.start")
    public HttpResponse<WorkflowStartResponse> start(@Body final HelloWorldRequest request) {
        final HelloWorldRequest normalized = normalizedRequest(request);
        final AnonymousSession session = anonymousSessionContext.get().session();
        return HttpResponse.ok(helloWorldWorkflowClient.start(normalized.name(), normalized.useCase(), normalized.workflowId(), session.actorKind(), session.sessionId()));
    }

    @Get("/history")
    @RequirePolicy("workflow.hello_world.history")
    public HttpResponse<List<HelloHistoryEntry>> history(@QueryValue(defaultValue = "12") final int limit) {
        final int resolvedLimit = Math.max(1, Math.min(limit, 50));
        anonymousSessionContext.get().session();
        return HttpResponse.ok(helloHistoryQueryService.recent(resolvedLimit));
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
