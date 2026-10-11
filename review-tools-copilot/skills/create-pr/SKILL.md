---
name: create-pr
description: Deliberately invoked skill that opens a draft GitHub pull request for a committed, reviewed change via the GitHub MCP server. Never runs automatically; never merges or pushes unrelated work.
---

# create-pr

This is a Copilot/VS-Code-hosted skill equivalent of the Claude skill. It follows the same preconditions and steps: confirm source branch, target branch, repository, and that the branch is already pushed; gather PR body inputs from repo-local committed evidence; call the GitHub MCP `create_pull_request` tool with `draft: true`; and return the PR URL and number.

Preconditions and steps mirror review-tools-claude/skills/create-pr/SKILL.md. This skill must be invoked explicitly in Copilot chat by its namespaced skill id: `review-tools-copilot:create-pr`.

Output: exact PR URL, PR number, draft status, source->target branches, and repository.
