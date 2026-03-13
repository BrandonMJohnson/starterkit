# StarterKit Git Workflow

This document captures the intended git workflow for StarterKit.

## Workflow Model

StarterKit uses a direct-commit workflow unless the change is large enough to benefit from a short-lived branch.

## Branching Strategy

- Small, low-risk changes may commit directly to `main`.
- Larger or experimental work should use short-lived branches with the required `codex/` prefix, for example:
  - `codex/feature-<short-description>`
  - `codex/fix-<short-description>`
  - `codex/refactor-<short-description>`
  - `codex/spike-<short-description>`
- Keep branches focused and merge once the work is stable.

## Commit Discipline

- Make small, focused commits.
- Commit after completing a logical unit of work.
- Do not bundle unrelated changes.
- Separate refactors from feature work when practical.
- Avoid formatting-only noise unless isolated.

Commit message format:

- `<type>: <concise summary>`

Examples:

- `feat: add starterkit tool guidance`
- `fix: correct workflow history query`
- `refactor: extract session bootstrap logic`

## Close-Out Expectations

Before finishing a meaningful pass:

1. Check `git status --short`.
2. Ensure any logical unit that should be preserved is committed or state explicitly why no commit was made.
3. Keep docs current if the platform contract changed.
4. State what you verified and what remains unverified.
