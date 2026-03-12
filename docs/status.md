# StarterKit Status

Last updated: 2026-03-12

## Current Focus

- Land the reusable platform baseline as a standalone, buildable, deployable repo.
- Keep the demo intentionally small: one LLM-backed `hello-world` workflow plus persisted history.
- Preserve the full core spine: API, orchestration, PostgreSQL, Valkey, tracing, policy/OPA, and UI shell.
- Keep the next architectural seam ready for a future graph database without forcing one into the first slice prematurely.

## In Progress

- Simplified Gradle multi-project layout is replacing Aviation's included build-logic pattern.
- A compose-first deployment path is being wired for local deployment of the full platform baseline.
- The `hello-world` workflow is being upgraded to use prompt rendering, real LLM calls, Postgres-backed history, and OPA-gated API access.
- Repo guidance is being rewritten so Codex starts with a short business-domain interview when the domain is not yet defined.

## Next 3 Tasks

1. Finish the deployable compose stack and verify the services start together.
2. Verify the UI shell can trigger the workflow end to end and render history.
3. Document the graph-store extension seam once the first buildable baseline is green.

## Risks

- Dockerfiles currently depend on built artifacts from the Gradle and frontend build steps.
- The LLM path requires reachable provider configuration; the repo can boot without a valid provider, but the demo workflow will fail until LLM env vars are set correctly.
- The graph database boundary is intentional but not implemented yet; a later slice still needs a concrete technology choice and usage pattern.
