# Plugin Inventory — review-tools-claude

Every file in this plugin and why it belongs in a reusable, portable review/shipping workflow.

| File | Included because |
|---|---|
| `.claude-plugin/plugin.json` | Required Claude Code plugin manifest (name, description, version, author). |
| `README.md` | Install/sideload/reload instructions for a consuming team. |
| `PLUGIN_INVENTORY.md` | This file — required inventory with inclusion rationale. |
| `skills/create-pr/SKILL.md` | Deliberate, explicitly-invoked draft-PR creation mechanism. Reusable across any repo with a GitHub MCP server and a feature-spec convention. |
| `skills/review-pr/SKILL.md` | Defines the exact pending-review transaction (create → inline comments → submit) so review findings land as one coherent GitHub review. |
| `agents/pr-reviewer.md` | Read-only Claude Code subagent; least-privilege tool list (`Read`, `Grep`, `Glob` + 5 named GitHub MCP tools only). No edit/write/merge/branch tools. |
| `.mcp.json` | GitHub MCP server wiring. Uses `${GITHUB_PERSONAL_ACCESS_TOKEN}` runtime placeholder only — no resolved token/secret value. |

## Deliberately excluded

| Excluded | Reason |
|---|---|
| Feature-2 application source/tests (`backend/`, `frontend/`) | Story-specific application code, not a reusable workflow asset. |
| `docs/reviews/*.md`, `pipeline/feature2/*` | Generated, story-specific evidence (review reports, test output, time logs) — proves a past run, not part of the reusable mechanism. |
| `.env`, tokens, credentials, private keys | Never packaged; `.mcp.json` references an env var name only. |
| `pipeline/rules/*.md`, `.claude/rules/review-checklist-rule.md` | The checklist/rules are consuming-repo policy, read by path at runtime — not duplicated into the plugin so they can't drift out of sync with the source of truth. |
| Hooks (e.g. `secretsHook.js`) | Not included: this plugin's agent never edits/writes files, so the `PreToolUse` secrets hook (which guards `Edit|Write`) adds no value here. The hook stays owned by the consuming repo's `.claude/hooks/`. |
| `scripts/` | Left empty — no safe support script was needed beyond the skills/agent themselves; kept as a placeholder directory for future non-destructive helper scripts only. |

## Portability notes

- `skills/*/SKILL.md` and `.mcp.json` are the portable core — same Markdown/JSON shape is reusable as the Copilot-in-VS-Code plugin's skills and MCP config.
- `agents/pr-reviewer.md` uses Claude Code's agent frontmatter (`tools:`, `model: inherit`) and is **not** portable as-is; a GitHub Copilot equivalent needs its own `*.agent.md` format and tool-permission syntax.
- The manifest location (`.claude-plugin/plugin.json`) is Claude-specific; a Copilot VS Code plugin uses a root-level `plugin.json` instead.
