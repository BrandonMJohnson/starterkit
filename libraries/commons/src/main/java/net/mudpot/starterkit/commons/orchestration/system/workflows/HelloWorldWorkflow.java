package net.mudpot.starterkit.commons.orchestration.system.workflows;

import net.mudpot.starterkit.commons.orchestration.system.model.HelloWorldResult;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface HelloWorldWorkflow {
    @WorkflowMethod(name = "HelloWorldWorkflow")
    HelloWorldResult run(String name, String useCase);
}
