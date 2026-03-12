package starterkit.authz

default allow := false

allowed_actions := {
  "workflow.hello_world.run",
  "workflow.hello_world.start",
  "workflow.hello_world.history",
}

allow if {
  action := lower(trim(input.action))
  allowed_actions[action]
}

reason := "action-allowed" if allow
reason := "action-not-supported" if not allow

policy_version := "starterkit.v1"
