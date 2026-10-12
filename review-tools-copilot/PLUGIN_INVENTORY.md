# Plugin Inventory — review-tools-copilot

This file lists every included file and why it belongs in the reusable Copilot/VS Code plugin.

| File | Included because |
|---|---|
| `plugin.json` | Root Agent Plugins manifest required by Copilot's plugin discovery. |
| `skills/create-pr/SKILL.md` | Deliberate, explicitly-invoked draft-PR creation mechanism. |
| `skills/review-pr/SKILL.md` | Defines the remote-pending-review transaction (create → inline comments → submit). |
| `agents/pr-reviewer.agent.md` | Read-only Copilot agent with least-privilege tool list for performing PR reviews remotely. |
| `.mcp.json` | MCP server wiring (runtime placeholders only). |
| `README.md` | Local installation and invocation instructions for Copilot in VS Code. |

## Excluded items

- Application source, tests, generated evidence, `.env` files, tokens, credentials, and private keys are intentionally excluded.
- The C2 checklist/rules live in the consuming repository and are read by path at runtime (not packaged here) so they cannot drift.
