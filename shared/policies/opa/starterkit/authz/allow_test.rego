package starterkit.authz

test_run_allowed if {
  allow with input as {"action": "workflow.hello_world.run"}
}

test_unknown_action_denied if {
  not allow with input as {"action": "workflow.unknown"}
}
