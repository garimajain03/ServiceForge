---
name: review-pr
description: Fetches a remote GitHub pull request's metadata, changed files, and diff, then applies the committed C2 review checklist to it, submitting one coherent pending review with inline findings.
---

# review-pr

## Purpose

Extends the repo's C2 review checklist to a **remote** pull request diff instead of a local working tree. This skill is invoked explicitly against a PR number (e.g. `/review-tools-claude:review-pr 42`) and is consumed by the `pr-reviewer` agent, which carries the actual tool-use loop.

## Checklist source of truth

Before evaluating anything, read the canonical checklist by path:

```
.claude/rules/review-checklist-rule.md
```

Do not rely on a pasted or remembered copy of the checklist. If that path does not exist in the consuming repository, stop and report that the checklist could not be located — do not invent checklist items.

## Required review transaction (exact order)

1. **Fetch** PR metadata, the list of changed files, and the unified diff using GitHub MCP (e.g. `get_pull_request`, `get_pull_request_files`, `get_pull_request_diff`).
2. **Create a pending review** using `pull_request_review_write` with `method: "create"`.
3. **Add findings** with `add_comment_to_pending_review`, one call per finding, each anchored to a specific changed file and diff line (never a free-floating/unanchored comment).
4. **Submit** the review using `pull_request_review_write` with `method: "submit_pending"`, with an overall summary body and event (`COMMENT`, `APPROVE`, or `REQUEST_CHANGES` matching the verdict).

Do not post ad hoc single comments outside this pending-review batch.

## Evaluation areas (mirrors `.claude/rules/review-checklist-rule.md`)

1. Jira/spec AC verification — every AC in `pipeline/features/feature-*-*.md` has an implementing file/method visible in the diff.
2. Quality audit — controllers stay thin, services own business rules, DTOs are plain, names are descriptive.
3. Security review — input validation, no secrets, no unsafe deserialization; trust (do not re-run) any existing secrets-scan hook result instead of re-scanning the diff yourself.
4. Compliance — no active rule under `pipeline/rules/*.md` is violated.
5. Design alignment — matches `pipeline/architecture-adr.md`'s recorded decisions for the feature.

Each item resolves to `Pass` / `Fail` / `Needs Human Decision`, same as the local C2 checklist — only the diff source changed (remote PR diff instead of local tree).

## Tool scope

Read/search tools plus only these GitHub MCP tools: `get_pull_request`, `get_pull_request_files`, `get_pull_request_diff`, `pull_request_review_write`, `add_comment_to_pending_review`. Never `merge_pull_request`, never edit/commit/push/branch tools.

## Output contract

Produce the same structured markdown report shape as the local reviewer-agent (`# Review Report: [story-id]`, one section per checklist area, machine-readable summary, final `Ship`/`No-Ship` verdict) and submit it as the PR review body. At least one checklist finding must be posted as an inline comment anchored to a changed file/line, with the specific checklist item named in the comment text.
