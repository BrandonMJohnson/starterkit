# StarterKit

StarterKit is a reusable full-stack baseline for building domain-specific products on top of:

- a Micronaut API boundary
- Temporal-backed orchestration
- a standalone Node.js Temporal worker
- PostgreSQL persistence
- Valkey caching
- OpenTelemetry tracing with Jaeger
- OPA-backed policy evaluation
- a React/Vite UI shell served by `nginx`

The shipped demo is intentionally small: a single `hello-world` workflow that renders a prompt, calls the same LLM activity path used by Aviation's WX agent, persists run history, and exposes that history in the UI.

The local deployment stack uses one durable PostgreSQL instance with separate logical databases for:

- the app (`starterkit`)
- Temporal default persistence (`temporal`)
- Temporal visibility (`temporal_visibility`)

The baseline also includes a signed session cookie with an anonymous actor by default. There is still no real login flow, but the API now issues a stable per-browser session at `/api/session` so policy evaluation and future UI personalization can hang off a real session boundary instead of a hard-coded actor stub.

## What This Repo Optimizes For

- Something you can clone, build, and deploy locally without aviation-specific baggage.
- A codebase Codex can enter quickly.
- Clear seams for adding a real domain, starting with discovery rather than premature implementation.
- Documented patterns for workflows, policy, persistence, tracing, caching, and frontend data flow.

## First Codex Prompt

Point Codex at this repo and start with the business problem, not the implementation details. `AGENTS.md` instructs Codex to begin with a short interview when the domain is not yet clearly defined.

## Repository Layout

```text
StarterKit/
  apps/
    java/
    node/
  libs/
    java/
    node/
  contracts/
  generated/
  java-build/
  docs/
    status.md
    patterns.md
    interviews/
  shared/
    policies/opa/
```

## Quick Start

1. Copy `.env.example` to `.env` and set the LLM values that match your local setup.
2. Enable the repo-managed Node toolchain and install workspace dependencies:

```bash
corepack enable
pnpm install
```

3. Start the local stack:

```bash
docker compose up --build
```

This path now uses a root multi-stage Docker build, so Compose compiles the full Gradle project and the UI assets before assembling the runtime images.

The default local `ui-service` path also mounts `apps/java/ui-service/build/frontend-static` as an overlay when that directory contains a host build, so frontend watch output can take over without replacing the clean-checkout fallback baked into the image.

4. Open:

- UI shell: [http://localhost:18090](http://localhost:18090)
- API health: [http://localhost:18080/api/healthz](http://localhost:18080/api/healthz)
- Temporal UI: [http://localhost:18233](http://localhost:18233)
- Jaeger: [http://localhost:18686](http://localhost:18686)

The compose host ports are configurable through `.env` if those defaults still collide with other local services.

## Local Iteration

- Java services:

```bash
./java-build/gradlew -p java-build :apps:java:api-service:run
./java-build/gradlew -p java-build :apps:java:orchestration:run
./java-build/gradlew -p java-build :apps:java:policy-service:run
```

- Root workspace tasks:

```bash
pnpm build
pnpm test
pnpm lint
```

- Frontend dev server:

```bash
pnpm --filter @starterkit/starterkit-ui dev
```

The Vite dev server proxies `/api` to `http://localhost:8080`.

- Node Temporal worker:

```bash
pnpm --filter @starterkit/node-temporal-worker worker
pnpm --filter @starterkit/node-temporal-worker start-workflow
```

The standalone worker listens on `hello-from-nodejs-task-queue` by default and starts the `helloFromNodejsWorkflow`.

- Frontend validation through the compose-managed `nginx` container:

```bash
docker compose up --build ui-service api-service orchestration policy-service
```

In this mode, `ui-service` serves baked image assets until a host frontend build is present, then automatically switches to the mounted overlay for fast browser refreshes.

- Fast UI iteration with the default compose overlay:

```bash
./java-build/gradlew -p java-build :apps:java:ui-service:buildFrontendAssets
docker compose up --build ui-service api-service orchestration policy-service

pnpm --filter @starterkit/starterkit-ui build:watch
```

This keeps the containerized `nginx` path, but the mounted `apps/java/ui-service/build/frontend-static` directory takes over as soon as it contains a frontend build so browser refreshes pick up rebuilds without rebuilding the image.

## Deployment Model

The first deployable path is Docker Compose. The service boundaries stay container-friendly so you can later replace compose with Kubernetes, Nomad, ECS, or another deployment layer without rewriting the application seams.

The Compose baseline intentionally keeps a single persistent Postgres server while splitting the application, Temporal, and Temporal visibility data into separate databases. The named Docker volume `postgres-data` keeps those databases across container restarts and recreations unless you explicitly remove the volume.

## Graph DB Follow-On

This baseline does not hard-wire a graph database yet, but it does reserve the seam:

- `graph.*` config exists in API and orchestration
- `docs/patterns.md` documents where a future `libs/java/graph-store` boundary should sit
- domain discovery should decide whether the graph belongs on the query path, orchestration side effects, or both before code is added

## Docs

- [status.md](/Users/brandonjohnson/SourceCode/StarterKit/docs/status.md)
- [patterns.md](/Users/brandonjohnson/SourceCode/StarterKit/docs/patterns.md)
- [interviews/README.md](/Users/brandonjohnson/SourceCode/StarterKit/docs/interviews/README.md)
