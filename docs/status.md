# StarterKit Status

Last updated: 2026-03-30

## Current Focus

- Land the reusable platform baseline as a standalone, buildable, deployable repo.
- Keep the demo intentionally small: one LLM-backed `hello-world` workflow plus persisted history.
- Keep the repo operating as a polyglot monorepo with Nx at the root, Gradle isolated under `java-build/`, and pnpm managing the Node workspace.
- Preserve the full core spine: API, orchestration, PostgreSQL, Valkey, tracing, policy/OPA, and UI shell.
- Add a separate Node.js Temporal worker slice without coupling Java and Node source trees directly.
- Keep the no-login baseline, but route request identity through a real session boundary with an anonymous actor by default.
- Keep the next architectural seam ready for a future graph database without forcing one into the first slice prematurely.

## In Progress

- Repo layout is moving from `services/` and `libraries/` into `apps/` and `libs/` so the root clearly reads as an Nx workspace.
- Simplified Gradle multi-project layout is replacing Aviation's included build-logic pattern while staying isolated under `java-build/`.
- A compose-first deployment path is being wired for local deployment of the full platform baseline, including one durable Postgres instance split into separate app, Temporal, and Temporal visibility databases.
- Compose image builds now run from `infra/` through shared Docker entrypoints so `docker compose -f infra/local/docker-compose.yml up --build` can compile the full Gradle project and package the resulting runtime images directly from the workspace root.
- OPA policy bundles now live with the policy service resources instead of under a generic top-level `shared/` directory.
- The `hello-world` workflow is being upgraded to use prompt rendering, real LLM calls, Postgres-backed history, and OPA-gated API access.
- A separate Node.js Temporal worker now owns a standalone `helloFromNodejsWorkflow` on its own task queue.
- The `hello-world` workflow now also carries session context into orchestration and evaluates workflow-side policy through a dedicated policy activity so business rules can live in Rego instead of only at API ingress.
- API requests now bootstrap through a signed session cookie and `/api/session`, and policy input uses session context instead of a fixed builder actor.
- Recovered API policy enforcement is moving back toward a reusable Micronaut-native boundary with controller annotations, an HTTP filter, and typed session parameter injection.
- Recovered frontend structure now lives in the pnpm workspace under `apps/node/starterkit-ui` while `apps/platform/ui-service` remains the static host boundary.
- The default local UI path now combines baked frontend assets with a host-mounted overlay fallback so clean checkouts still boot while `build:watch` can take over immediately once frontend assets exist on disk.
- Repo guidance is being rewritten so Codex starts with a short business-domain interview when the domain is not yet defined.

## Next 3 Tasks

1. Build the updated Docker images, including the new Node.js Temporal worker image, through `docker compose -f infra/local/docker-compose.yml up --build`.
2. Decide whether the Node.js worker should stay standalone or gain an API/UI trigger path through a contract boundary.
3. Document the graph-store extension seam once the polyglot baseline is green in compose.

## Risks

- The LLM path requires reachable provider configuration; the repo can boot without a valid provider, but the demo workflow will fail until LLM env vars are set correctly.
- The root workspace now depends on `pnpm` via Corepack; local environments that skip `corepack enable` will miss Node workspace commands.
- Temporal SQL bootstrap now assumes the single Postgres server can create and retain the `temporal` and `temporal_visibility` databases via the checked-in init script on first volume initialization.
- Session signing currently defaults to a local development secret unless `STARTERKIT_SESSION_SIGNING_SECRET` is overridden for deployed environments.
- The graph database boundary is intentional but not implemented yet; a later slice still needs a concrete technology choice and usage pattern.
