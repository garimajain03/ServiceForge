---
name: review-pr
description: Fetches a remote GitHub pull request's metadata, changed files, and diff, then applies the committed C2 review checklist to it, submitting one coherent pending review with inline findings.
---

# review-pr

This Copilot-hosted skill mirrors review-tools-claude/skills/review-pr/SKILL.md. It requires an explicit invocation with a PR number (e.g. `review-tools-copilot:review-pr 42`) and reads the consuming repo's checklist at `.claude/rules/review-checklist-rule.md` by path before performing any review logic.

Tool permissions: intended to use read/search tools and the GitHub MCP tools: `get_pull_request`, `get_pull_request_files`, `get_pull_request_diff`, `pull_request_review_write`, `add_comment_to_pending_review`.

Required behavior: create a pending review, add inline comments for each finding, then submit the pending review. Never merge or edit code.
