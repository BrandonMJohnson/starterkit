package net.mudpot.starterkit.commons.orchestration.system.model;

public record HelloWorldRequest(
    String name,
    String useCase,
    String workflowId
) {
}
