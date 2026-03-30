package starterkit.authz

default allow := false
allowed_actions := {
  "api.hello_world.run",
  "api.hello_world.start",
  "api.hello_world.history",
}

normalized_action := lower(trim_space(object.get(input, "action", "")))

workflow_execute_supported if normalized_action == "workflow.hello_world.execute"

workflow_execute_allowed if {
  workflow_execute_supported
  request := object.get(input, "request", {})
  count(trim_space(object.get(request, "use_case", ""))) >= 15
}

allow if {
  allowed_actions[normalized_action]
}

allow if workflow_execute_allowed

reason := "action-allowed" if allowed_actions[normalized_action]
reason := "hello_world_execute_allowed" if workflow_execute_allowed
reason := "hello_world_use_case_too_short" if {
  workflow_execute_supported
  not workflow_execute_allowed
}
reason := "action-not-supported" if {
  not allowed_actions[normalized_action]
  not workflow_execute_supported
}

policy_version := "starterkit.v1"
