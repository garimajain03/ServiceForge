# AI Use Log

- PR: https://github.com/garimajain03/ServiceForge/pull/4 — Draft PR created and checklist review initiated by reviewer-agent.

## Reflection

- What changed when the review agent moved from a local working tree to the remote PR diff?

  Nothing about the checklist itself changed; only the evidence source changed. Locally the reviewer reads committed files by path; for a remote PR the agent reads the PR metadata and unified diff via `get_pull_request_diff`. The remote flow adds a transactional posting step (open pending review → add per-finding inline comments → submit pending review) that must be implemented with MCP methods.

- Which C2 checklist item was hardest to express as a useful inline PR finding, and why?

  Item 5) Design alignment (ADR fidelity). ADR tradeoffs often span multiple subsystems and historical commits; anchoring a single inline comment to a diff line captures a symptom but not the full decision context. To be useful, the inline comment must reference the ADR section and explain the broader divergence; sometimes a linked, higher-level report is still needed.

- What reusable capability belonged in the plugin, and what did you deliberately keep in the consuming repository?

  Belongs in plugin: the PR creation and PR review mechanism (skills + least-privilege agent + MCP wiring), and the exact transaction shape for pending reviews so it is portable. Kept in consuming repo: the canonical checklist, ADRs, feature specs, and generated evidence (test output, review reports) because they are policy and story-specific and should be read by path at runtime to avoid drift.

- Which plugin component was portable across tools, and which required client-specific handling?

  Portable: SKILL.md files and `.mcp.json` wiring (same MCP server shape, env-var placeholders). Client-specific: agent frontmatter and plugin manifest location (Claude's `.claude-plugin/plugin.json` vs Copilot's root `plugin.json`) and any agent-formats that embed tool permission syntax.

- What hook or MCP command did you inspect before installation, and what risk would it have introduced if you had not inspected it?

  Inspected: the GitHub MCP server tool list (to confirm only read/review tools are requested) and the `.mcp.json` wiring for placeholder usage. Risk if not inspected: the agent/plugin could be granted merge/push/delete scopes or a committed token could be included in the manifest, enabling accidental or malicious code changes.

- If a teammate installed your plugin tomorrow, what evidence shows they would get the intended workflow rather than a copied, drifting configuration?

  Evidence: `PLUGIN_INVENTORY.md` documents exactly what the plugin includes and excludes; the plugin reads checklist/rules/specs by path from the consuming repo at runtime; `README.md` provides local discovery and sideload steps; the `version` field in plugin manifest is the single update signal. These together prevent a stale plugin from carrying its own copy of policy or evidence.

## PreToolUse hook evidence required (what the reviewer expects)

When the orchestrator provides `PreToolUse` secrets-scan evidence for a proposed change (a PR or a diff), the reviewer treats that evidence as authoritative for Checklist 3 (Secrets-scan). To mark Checklist 3 `Pass`, the reviewer expects the hook evidence to meet all of the following:

- The hook output is a JSON array (or an equivalent structured record) with one entry per scanned file. Each entry must include at least these fields:
  - `file`: the file path that was scanned (workspace-relative)
  - `exitCode`: `0` when the scanner found no secret-like patterns in that file; nonzero otherwise
  - `stderr` (string): empty when no matches; contains matched snippet context or error text when blocked

- Example authoritative evidence that will be accepted by the reviewer:

  ```json
  [
    {"file":"backend/src/main/java/com/serviceforge/service/PartsReservationService.java","exitCode":0,"stderr":""},
    {"file":"backend/src/main/java/com/serviceforge/data/MockDataStore.java","exitCode":0,"stderr":""},
    {"file":"review-tools-copilot/.mcp.json","exitCode":0,"stderr":""}
  ]
  ```

- If any entry shows `exitCode` != 0 or non-empty `stderr`, the reviewer will mark Checklist 3 `Fail` and post an inline comment pointing at the offending file(s). The PR author must either fix the offending file (remove secrets) and push a new commit, or provide a convincingly scoped exception with human justification outside automated acceptance (rare).

- Operational note: the repository provides the hook implementation at `.claude/hooks/secretsHook.js`. The simplest local repro the reviewer accepts is running:

  ```bash
  echo '{"tool_input":{"content": "...file content to scan..."}}' | node .claude/hooks/secretsHook.js
  ```

  and showing the hook's exit code and stderr (or the JSON array) for the actual PR diff files. The orchestrator normally runs this hook against every modified file and bundles the per-file results in the `PreToolUse` evidence object it hands the reviewer.

---

