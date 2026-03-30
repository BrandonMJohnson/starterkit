package net.mudpot.starterkit.orchestrationclients.system;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import net.mudpot.starterkit.commons.orchestration.TaskQueues;
import net.mudpot.starterkit.commons.orchestration.WorkflowNames;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldRequest;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldResult;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldWorkflowInput;
import net.mudpot.starterkit.commons.orchestration.system.workflows.HelloWorldWorkflow;
import net.mudpot.starterkit.commons.session.Session;
import net.mudpot.starterkit.orchestrationclients.model.WorkflowStartResponse;

import java.util.UUID;

public class HelloWorldWorkflowClient {
    private final WorkflowClient workflowClient;

    public HelloWorldWorkflowClient(final WorkflowClient workflowClient) {
        this.workflowClient = workflowClient;
    }

    public WorkflowStartResponse start(final HelloWorldRequest request, final Session session) {
        final String resolvedId = normalizedWorkflowId(request == null ? null : request.workflowId());
        final HelloWorldWorkflow workflow = workflowStub(resolvedId);
        final WorkflowExecution execution = WorkflowClient.start(workflow::run, workflowInput(request, session));
        return new WorkflowStartResponse(
            WorkflowNames.HELLO_WORLD,
            execution.getWorkflowId(),
            TaskQueues.HELLO_WORLD,
            execution.getRunId()
        );
    }

    public HelloWorldResult run(final HelloWorldRequest request, final Session session) {
        return workflowStub("hello-world-" + UUID.randomUUID()).run(workflowInput(request, session));
    }

    private HelloWorldWorkflow workflowStub(final String workflowId) {
        return workflowClient.newWorkflowStub(
            HelloWorldWorkflow.class,
            WorkflowOptions.newBuilder()
                .setTaskQueue(TaskQueues.HELLO_WORLD)
                .setWorkflowId(workflowId)
                .build()
        );
    }

    private static String normalizedName(final String name) {
        final String value = name == null ? "" : name.trim();
        return value.isBlank() ? "World" : value;
    }

    private static String normalizedUseCase(final String useCase) {
        final String value = useCase == null ? "" : useCase.trim();
        return value.isBlank() ? "Demonstrate the StarterKit platform baseline." : value;
    }

    private static String normalizedWorkflowId(final String workflowId) {
        final String value = workflowId == null ? "" : workflowId.trim();
        return value.isBlank() ? "hello-world-" + UUID.randomUUID() : value;
    }

    private static HelloWorldWorkflowInput workflowInput(final HelloWorldRequest request, final Session session) {
        final String name = request == null ? null : request.name();
        final String useCase = request == null ? null : request.useCase();
        final String actorKind = session == null ? "" : session.actorKind();
        final String sessionId = session == null ? "" : session.sessionId();
        return new HelloWorldWorkflowInput(
            normalizedName(name),
            normalizedUseCase(useCase),
            actorKind == null ? "" : actorKind.trim(),
            sessionId == null ? "" : sessionId.trim()
        );
    }
}
