# StarterKit

StarterKit is a reusable full-stack baseline for building domain-specific products on top of:

- a Micronaut API boundary
- Temporal-backed orchestration
- PostgreSQL persistence
- Valkey caching
- OpenTelemetry tracing with Jaeger
- OPA-backed policy evaluation
- a React/Vite UI shell served by `nginx`

The shipped demo is intentionally small: a single `hello-world` workflow that renders a prompt, calls the same LLM activity path used by Aviation's WX agent, persists run history, and exposes that history in the UI.

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
  docs/
    status.md
    patterns.md
    interviews/
  libraries/
    commons/
    orchestration-clients/
    persistence/
  services/
    api-service/
    orchestration/
    policy-service/
    ui-service/
  shared/
    policies/opa/
```

## Quick Start

1. Copy `.env.example` to `.env` and set the LLM values that match your local setup.
2. Build the repo:

```bash
./gradlew build
```

3. Start the local stack:

```bash
docker compose up --build
```

4. Open:

- UI shell: [http://localhost:8090](http://localhost:8090)
- API health: [http://localhost:8080/api/healthz](http://localhost:8080/api/healthz)
- Temporal UI: [http://localhost:8233](http://localhost:8233)
- Jaeger: [http://localhost:16686](http://localhost:16686)

## Local Iteration

- Java services:

```bash
./gradlew :services:api-service:run
./gradlew :services:orchestration:run
./gradlew :services:policy-service:run
```

- Frontend dev server:

```bash
cd services/ui-service/frontend
npm install
npm run dev
```

The Vite dev server proxies `/api` to `http://localhost:8080`.

## Deployment Model

The first deployable path is Docker Compose. The service boundaries stay container-friendly so you can later replace compose with Kubernetes, Nomad, ECS, or another deployment layer without rewriting the application seams.

## Graph DB Follow-On

This baseline does not hard-wire a graph database yet, but it does reserve the seam:

- `graph.*` config exists in API and orchestration
- `docs/patterns.md` documents where a future `libraries/graph-store` boundary should sit
- domain discovery should decide whether the graph belongs on the query path, orchestration side effects, or both before code is added

## Docs

- [status.md](/Users/brandonjohnson/SourceCode/StarterKit/docs/status.md)
- [patterns.md](/Users/brandonjohnson/SourceCode/StarterKit/docs/patterns.md)
- [interviews/README.md](/Users/brandonjohnson/SourceCode/StarterKit/docs/interviews/README.md)
