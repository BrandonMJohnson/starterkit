# StarterKit Patterns

## Service Boundaries

- `api-service`
  - external HTTP boundary
  - synchronous read models
  - workflow start/run adapters
  - policy checks before side effects
- `orchestration`
  - Temporal workflows and activities
  - durable retries/timeouts
  - LLM, cache, and persistence side effects
- `policy-service`
  - small adapter over OPA
  - central place for policy decision translation
- `ui-service`
  - static SPA host
  - same-origin proxy for `/api` and tracing

## Workflow Pattern

1. API accepts typed input.
2. API checks policy.
3. API calls a typed workflow client.
4. Workflow can call policy evaluation activities for workflow-side auth and business rules.
5. Workflow renders a prompt.
6. Workflow calls the shared LLM activity.
7. Activity persists durable results.
8. API and UI read the resulting state back through normal service boundaries.

## Caching Pattern

- LLM caching lives inside the shared `LlmActivitiesImpl` path.
- Valkey is optional in local code paths and can fail over to in-memory cache when disabled.
- Cache keys should be explicit and domain-aware.

## Persistence Pattern

- Shared persistence entities and repositories live in `libraries/persistence`.
- Schema changes land as Flyway migrations in that same library so both API and worker see the same migration set.
- The first demo persists workflow history, not domain complexity.

## Policy Pattern

- Services do not embed authorization logic directly in controllers when a reusable decision can live in OPA.
- API controller methods declare policy requirements through a reusable Micronaut annotation/interceptor seam.
- Request/session context should be resolved at the Micronaut HTTP boundary and consumed from request context, not rebuilt ad hoc inside individual controllers.
- API calls `policy-service`.
- `policy-service` delegates to OPA.
- Workflows can call `policy-service` through a dedicated Temporal activity when policy-evaluable business rules belong inside orchestration rather than only at API ingress.
- Rego policies live under `shared/policies/opa/`.

## Session Pattern

- StarterKit ships with anonymous sessions even though it does not ship a login system.
- `api-service` owns the session cookie and exposes `/api/session` for bootstrap.
- Anonymous session resolution belongs at the Micronaut HTTP boundary via filter/request context, not as repeated controller helper logic.
- Policy input should use session-backed actor context instead of controller-local hard-coded identities.
- Real authentication can replace or enrich the anonymous actor later without changing every endpoint contract first.

## Tracing Pattern

- Java services emit OTLP spans.
- Temporal client and worker tracing is bridged through `OpenTracingOptions`.
- The UI shell forwards browser fetch traces to the same OTLP endpoint through `nginx`.

## Frontend Pattern

- Keep `src/app-shell/` for shell composition and route-level orchestration.
- Keep domain behavior in `src/features/<slice>/` with feature-local queries, API adapters, and UI pieces.
- Keep shared infrastructure in `src/lib/`.
- Put server state behind TanStack Query with shared query keys instead of ad hoc inline arrays.
- Keep test harness helpers under `src/test/` so app-level tests do not rebuild provider setup inline.

## Codex Pattern

- Start with discovery when the domain is unclear.
- Convert interview output into the first durable workflow and data slice.
- Keep the starter repo generic; domain-specific complexity belongs in the next repo phase, not in the baseline.

## Graph Store Follow-On

When a graph database becomes real work:

1. Add a dedicated `libraries/graph-store` boundary instead of importing a graph driver directly into multiple services.
2. Decide whether the graph is:
   - read-optimized query infrastructure
   - orchestration-side relationship storage
   - both
3. Document the decision before spreading graph semantics into the domain model.
