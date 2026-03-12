# AGENTS.md

## Startup Context

At the start of a new thread in this repository, read these files before proposing plans or making changes:

1. `/Users/brandonjohnson/SourceCode/StarterKit/README.md`
2. `/Users/brandonjohnson/SourceCode/StarterKit/docs/status.md`
3. `/Users/brandonjohnson/SourceCode/StarterKit/docs/patterns.md`
4. `/Users/brandonjohnson/SourceCode/StarterKit/docs/interviews/README.md`

Purpose:

- understand the current platform baseline
- see the documented reusable patterns
- recover the current implementation focus
- preserve the interview-first workflow for new domains

## Interview-First Rule

If the user asks for work in a domain that is not already clearly defined in repo docs, begin with a short discovery interview before coding.

Default interview goals:

1. Understand the business problem.
2. Identify the domain vocabulary.
3. Identify the first user or operator.
4. Identify the first durable workflow or data slice.
5. Identify key constraints, integrations, and success criteria.

Keep the interview concise. Ask only what materially changes the first implementation slice.

After the interview:

- summarize the problem and first slice
- update `docs/status.md` if the active focus changes materially
- capture durable notes in `docs/interviews/`

## Working Expectations

- Keep the stack buildable and deployable.
- Preserve these baseline components unless explicitly asked to remove them:
  - API
  - orchestration
  - PostgreSQL
  - Valkey
  - tracing
  - policy/OPA
  - UI shell
- Prefer extending existing documented patterns over inventing new abstractions.
- Treat `docs/patterns.md` as the reusable implementation guide.
- Keep graph database work behind a deliberate boundary. Do not scatter graph-driver usage across services.

## Testing

- Add or update the nearest useful regression test for non-trivial changes.
- Prefer narrow tests that prove the changed behavior.
- For cross-service changes, run the affected Gradle tasks and the frontend build when practical.

## Close-Out

Before finishing a meaningful pass:

1. Check `git status --short`.
2. Keep docs current if the platform contract changed.
3. State what you verified and what remains unverified.
