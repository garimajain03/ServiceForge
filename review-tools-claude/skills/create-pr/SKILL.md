---
name: create-pr
description: Deliberately invoked skill that opens a draft GitHub pull request for a committed, reviewed change via the GitHub MCP server. Never runs automatically; never merges or pushes unrelated work.
---

# create-pr

## Purpose

Opens a **draft** pull request for already-committed, already-pushed work through the GitHub MCP `create_pull_request` tool. This skill is a deliberate side-effecting action — it must be explicitly invoked by name (e.g. `/review-tools-claude:create-pr`) and never triggered automatically by another skill, agent, or hook.

## Preconditions (confirm all before calling any tool)

1. **Source branch** — the branch containing the committed change (ask the user if ambiguous; do not guess from an unrelated branch).
2. **Target/base branch** — the branch the PR should merge into (default to the repository's default branch only if the user confirms).
3. **Repository** — owner/repo the PR targets. Confirm it matches the remote the source branch was pushed to.
4. **Draft status** — the PR must be created with `draft: true`. Never create a non-draft PR from this skill.
5. **Push state** — the source branch must already be pushed to the remote. This skill does not push commits; if the branch isn't pushed, stop and tell the user to push it first.

## What this skill must never do

- Never merge, approve, or close a pull request.
- Never push commits, create branches, or force-push.
- Never create a PR without explicit human invocation of this skill.
- Never include unrelated/out-of-scope commits in the PR description.

## Steps

1. Ask for or confirm: source branch, target branch, repository, and the story/feature id (e.g. `feature-2-parts-reservation`).
2. Gather the PR body inputs from repo-local, already-committed evidence only:
   - Feature summary — from `pipeline/features/feature-N-<slug>.md` (`In scope` / Definition of Done).
   - Related story id — the feature's id/filename slug.
   - Test status — from the most recent test run evidence (e.g. `pipeline/feature2/test-suite-output.txt` or a fresh `mvn -f backend test` run).
   - C2 review outcome — the `Final verdict` line from `docs/reviews/[story-id]-review.md`.
3. Compose a concise PR title (`feat(feature-N): <slug>`) and a Markdown body with sections: `## Summary`, `## Related story`, `## Test status`, `## Review outcome`.
4. Call the GitHub MCP `create_pull_request` tool with `owner`, `repo`, `title`, `head` (source branch), `base` (target branch), `body`, and `draft: true`.
5. Return the real PR URL and PR number from the tool's response verbatim. Do not fabricate a URL/number and do not just print a Markdown body for the user to paste — the tool call itself must have run.
6. Stop. Do not run the reviewer agent automatically — that is a separate, explicitly invoked step.

## Output contract

Report back exactly:
- PR URL
- PR number
- Draft: true
- Source → target branches
- Repository

If `create_pull_request` fails (auth, branch not found, etc.), report the exact tool error and stop — do not retry with a different base/head guess.
