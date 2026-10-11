AI Use Log — Feature 2 Test & Verify

Actions performed by AI assistant:

- Parsed pipeline/features/feature-2-parts-reservation.md (AC1-9, edge cases E1-E4) and pipeline/architecture-adr.md, then wrote pipeline/feature2/test-plan.md mapping every AC/edge case to a concrete test.
- Implemented the production code the ADR described but the repo was missing:
  - backend/src/main/java/com/serviceforge/service/PartsReservationService.java (new)
  - backend/src/main/java/com/serviceforge/service/TechnicianAvailabilityService.java (extended: isValidNonOverlapping, cancelJob)
  - backend/src/main/java/com/serviceforge/data/MockDataStore.java (extended: findReservation, getReservationsForJob, releaseReservation, releaseAllForJob)
  - backend/src/main/java/com/serviceforge/model/PartReservation.java (extended: ACTIVE/RELEASED status)
  - backend/src/main/java/com/serviceforge/controller/PartController.java (refactored to delegate to the service; added DELETE /reservations/{id})
  - backend/src/main/java/com/serviceforge/controller/JobController.java (added GET .../reservations and POST .../cancel)
  - backend/src/main/java/com/serviceforge/util/PromptInjectionGuard.java (new) + ReservePartRequest.notes screened before reaching the service
- Added/updated unit tests for every AC, edge case, and the notes guardrail (PartControllerTest, JobControllerTest, PartsReservationServiceTest, TechnicianAvailabilityServiceTest, MockDataStoreConcurrencyTest).
- Added pipeline/rules/job-notes-injection-guardrail.md (missing from checkout) and the QA agent at .claude/agents/qa-agent.md (single canonical location, no duplicate copy).
- Added .vscode/tasks.json (manual run) and .github/workflows/feature2-tests.yml (CI on backend/** changes) to run the suite automatically.
- Ran mvn -f backend test — 41/41 passing, captured in pipeline/feature2/test-suite-output.txt.

Review stage additions (C2 Review Layer, Claude-only):
- Canonical review checklist and agents live under `.claude/` (`.claude/rules/review-checklist-rule.md`, `.claude/agents/review-agent.md`, `.claude/agents/reviewer-agent.md`), per the Claude-only policy in CLAUDE.md and AGENTS.md — no parallel Copilot review agent/checklist is maintained.
- Added PreToolUse secrets hook (`.claude/hooks/secretsHook.js`, registered in `.claude/settings.json`); verified it blocks a planted fake AWS key (exit 2) and allows clean content (exit 0).
- Produced a sample review report `docs/reviews/feature-2-parts-reservation-review.md` demonstrating a No-Ship with concrete findings.

Reflection:
- The review agent caught a concurrency underflow risk in MockDataStore that nominal unit tests wouldn't simulate. The PreToolUse hook caught a planted secret the qa-agent's tests don't check for.

## C3 — Shipping & Sharing (Claude Code path only)

Actions performed:
- Added project-scope GitHub MCP config at `.mcp.json` (root), using a `${GITHUB_PERSONAL_ACCESS_TOKEN}` runtime placeholder only — no token committed.
- Built the Claude Code plugin at `review-tools-claude/` with `.claude-plugin/plugin.json` (name/description/version/author), `skills/create-pr/SKILL.md`, `skills/review-pr/SKILL.md`, `agents/pr-reviewer.md`, a plugin-scoped `.mcp.json`, `README.md`, and `PLUGIN_INVENTORY.md`.
- `create-pr` is a deliberately-invoked skill only: it confirms source/target branch, repo, and draft status; pulls its PR-body inputs (summary, story, test status, C2 review outcome) from repo-local committed evidence (`pipeline/features/*.md`, `pipeline/feature2/test-suite-output.txt`, `docs/reviews/*.md`); calls GitHub MCP `create_pull_request` with `draft: true`; never merges or auto-runs.
- `pr-reviewer` is a read-only extension of `.claude/agents/reviewer-agent.md`: same checklist (`.claude/rules/review-checklist-rule.md`, read by path), same five checklist areas, but its diff source is the remote PR (`get_pull_request` / `get_pull_request_files` / `get_pull_request_diff`) instead of the local tree. It posts findings via the pending-review transaction (`pull_request_review_write` method `create` → `add_comment_to_pending_review` per finding → `pull_request_review_write` method `submit_pending`), never merges/edits/pushes.
- Environment note: this repository's actual git remote (`origin`) is a GitLab (`git.epam.com`) remote, not GitHub, so Part 1's live draft-PR-and-review run could not be executed end-to-end against the real remote in this environment. The skill/agent/MCP wiring is built exactly to the assignment's GitHub MCP contract (tool names, pending-review transaction, draft-only creation) so it is ready to run unmodified against any GitHub-hosted fork/mirror with `GITHUB_PERSONAL_ACCESS_TOKEN` set.
- Validate/sideload commands to run once a GitHub remote + token are available:
  - `claude plugin validate ./review-tools-claude`
  - `claude --plugin-dir ./review-tools-claude`
  - `/review-tools-claude:create-pr`, `/review-tools-claude:review-pr <pr-number>`, then bump `version` in `.claude-plugin/plugin.json`, make a trivial `SKILL.md` wording change, and run `/reload-plugins` to confirm the update is picked up without restarting the session.

Reflection:
- Moving the review agent from a local tree to a remote PR diff changes nothing about *what* is checked (same five checklist areas, same checklist file read by path) — it only changes *where the evidence comes from* (`get_pull_request_diff` instead of `Read`/`Grep` on the working tree) and adds a transactional posting step (pending review) instead of just returning a markdown report.
- The hardest checklist item to express as a useful inline PR comment is "5) Design alignment" (ADR fidelity) — ADR tradeoffs often span multiple files/commits, so a single diff-line anchor can only point at one symptom (e.g. where `synchronized` actually lives) rather than the whole divergence; the comment has to explicitly reference the ADR section by name to stay traceable.
- Reusable capability kept in the plugin: the PR-creation and PR-review *mechanism* (skills + least-privilege agent + MCP wiring). Deliberately kept in the consuming repo: the checklist itself, feature specs, ADR, and all generated evidence (test output, review reports) — the plugin reads these by path instead of bundling copies, so they can't drift out of sync.
- Portable across tools: the `SKILL.md` content and `.mcp.json` shape (same GitHub MCP server, same env-var placeholder). Client-specific: the agent definition (`agents/pr-reviewer.md`'s Claude Code frontmatter/tool-permission syntax and manifest location `.claude-plugin/plugin.json`) would need a Copilot-specific `*.agent.md` equivalent and a root-level `plugin.json`.
- Inspected before use: the GitHub MCP server's tool list and the `.mcp.json` command/args — confirmed it only needs `GITHUB_PERSONAL_ACCESS_TOKEN` as an env var (never written to disk) and that the agent's tool list is restricted to 5 named read/review tools. Not inspecting this would risk granting the review agent merge/push/delete-scope tools it doesn't need.
- A teammate installing this plugin gets the intended workflow (not a drifting copy) because: the checklist/rules/specs are read by path from the consuming repo at run time rather than duplicated into the plugin, the manifest's `version` field is the single update signal, and `PLUGIN_INVENTORY.md` documents exactly what is and isn't bundled so a stale local copy is easy to spot against the README's install/reload steps.

