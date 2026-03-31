# AGENTS.md

## Startup Context

At the start of a new thread in this repository, read these files before proposing plans or making changes:

1. `/Users/brandonjohnson/SourceCode/StarterKit/README.md`
2. `/Users/brandonjohnson/SourceCode/StarterKit/docs/status.md`
3. `/Users/brandonjohnson/SourceCode/StarterKit/docs/patterns.md`
4. `/Users/brandonjohnson/SourceCode/StarterKit/docs/interviews/README.md`
5. `/Users/brandonjohnson/SourceCode/StarterKit/docs/tools.md`
6. `/Users/brandonjohnson/SourceCode/StarterKit/docs/git-workflow.md`

Purpose:

- understand the current platform baseline
- see the documented reusable patterns
- recover the current implementation focus
- preserve the interview-first workflow for new domains
- apply the intended MCP and tool usage for this repo
- apply the intended git workflow for this repo

Tool-specific guidance lives in `/Users/brandonjohnson/SourceCode/StarterKit/docs/tools.md`.
Git workflow guidance lives in `/Users/brandonjohnson/SourceCode/StarterKit/docs/git-workflow.md`.

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

## Git Workflow

Follow `/Users/brandonjohnson/SourceCode/StarterKit/docs/git-workflow.md` for branch naming, commit discipline, and close-out expectations.
By default, commit each completed logical unit of work unless the user explicitly asks to defer commits.

## Close-Out

Before finishing a meaningful pass:

1. Check `git status --short`.
2. Follow `/Users/brandonjohnson/SourceCode/StarterKit/docs/git-workflow.md`.
3. Commit the completed logical unit unless the user asked not to.
4. Keep docs current if the platform contract changed.
5. State what you verified and what remains unverified.


<!-- nx configuration start-->
<!-- Leave the start & end comments to automatically receive updates. -->

## General Guidelines for working with Nx

- For navigating/exploring the workspace, invoke the `nx-workspace` skill first - it has patterns for querying projects, targets, and dependencies
- When running tasks (for example build, lint, test, e2e, etc.), always prefer running the task through `nx` (i.e. `nx run`, `nx run-many`, `nx affected`) instead of using the underlying tooling directly
- Prefix nx commands with the workspace's package manager (e.g., `pnpm nx build`, `npm exec nx test`) - avoids using globally installed CLI
- You have access to the Nx MCP server and its tools, use them to help the user
- For Nx plugin best practices, check `node_modules/@nx/<plugin>/PLUGIN.md`. Not all plugins have this file - proceed without it if unavailable.
- NEVER guess CLI flags - always check nx_docs or `--help` first when unsure

## Scaffolding & Generators

- For scaffolding tasks (creating apps, libs, project structure, setup), ALWAYS invoke the `nx-generate` skill FIRST before exploring or calling MCP tools

## When to use nx_docs

- USE for: advanced config options, unfamiliar flags, migration guides, plugin configuration, edge cases
- DON'T USE for: basic generator syntax (`nx g @nx/react:app`), standard commands, things you already know
- The `nx-generate` skill handles generator discovery internally - don't call nx_docs just to look up generator syntax


<!-- nx configuration end-->