# StarterKit Tool Intent

This document captures the intended use of MCP servers and adjacent tools in StarterKit.

Use tools to tighten execution, not to replace the repo docs. For this project, local repo documentation remains the first source of truth:

1. `README.md`
2. `docs/status.md`
3. `docs/patterns.md`
4. `docs/interviews/README.md`

## General Rule

- Start with the repo docs and the exact code you need.
- Use MCP tools when they materially improve reasoning, retrieval, or cross-thread continuity.
- Prefer small, targeted tool usage over broad scans.
- Store durable project knowledge in one place. Do not duplicate the same fact across repo docs, Memory, and ad hoc notes unless there is a clear resume benefit.

## sequential-thinking

Use `sequential-thinking` when the task needs structured reasoning before acting.

### Use For

- architecture or boundary decisions
- extraction tradeoffs from Aviation into StarterKit
- multi-service debugging across API, orchestration, policy, persistence, and UI
- deciding the first implementation slice after a discovery interview
- refactoring plans where sequencing matters

### Do Not Use For

- simple factual lookups
- straightforward code or docs edits
- narrow bug fixes with an obvious cause
- routine test or build commands

### Intent In This Repo

- Make reasoning explicit before changing shared platform seams.
- Use it to reduce design drift when adapting patterns from Aviation.
- Return the synthesized conclusion, not the intermediate reasoning, unless the user asks for it.

## Memory

Use the Knowledge Graph as a small, durable cross-thread memory for facts that help future work resume cleanly.

### Retrieval Protocol

When the task is meaningful and likely depends on prior decisions:

- search with `StarterKit`-prefixed terms first
- open only the exact nodes that look relevant
- fall back to direct file reads when Memory is missing, stale, or less precise than the repo docs

Good starter queries:

- `StarterKit Current Resume Point`
- `StarterKit <workstream>`
- `StarterKit <constraint>`
- `StarterKit <integration>`

### Storage Protocol

Store only durable facts that are useful across threads, such as:

- the current resume point when a pass ends with a clear next step
- architectural seams not yet obvious from code alone
- cross-service integration constraints
- domain interview conclusions that affect the first implementation slice
- project conventions that are easy to forget and not yet documented elsewhere

Prefer a small graph shape:

- one canonical node: `StarterKit Current Resume Point`
- a few active workstream or constraint nodes when needed
- simple relation types like `tracks`, `depends_on`, `blocks`, `implements`, and `configures`

### Do Not Store

- ephemeral debugging output
- validation logs
- temporary experiments
- broad copies of repo documentation
- pass-by-pass narrative history

### Intent In This Repo

- Keep Memory lean. StarterKit is intentionally small, so the graph should stay small too.
- Use Memory to preserve cross-thread continuity, not as a second `docs/status.md`.
- If repo docs and Memory disagree, treat the repo docs as the execution source of truth until reconciled.

## Browser Automation

Use browser automation when code inspection is not enough to validate the UI shell or an end-to-end path.

### Use For

- verifying the compose-served UI shell loads and calls the API correctly
- reproducing UI regressions that depend on real browser behavior
- capturing screenshots or traces for frontend issues

### Do Not Use For

- static content changes that can be validated from source
- API-only changes with no browser-facing effect

### Intent In This Repo

- Prefer the lightest useful check.
- Use browser automation to validate real seams, especially UI-to-API behavior, not as the default for every frontend edit.

## Skills

- Use a skill when the task clearly matches a listed skill or the user names it.
- Read only the part of the skill needed to execute the task.
- Reuse skill scripts and assets when they exist instead of recreating the workflow manually.

## Documentation Boundary

When tool usage changes the durable operating model of the repo:

- update `docs/tools.md`
- update `docs/status.md` only if the active execution focus or near-term plan changes materially
- capture interview output in `docs/interviews/` when the work introduces a new domain
