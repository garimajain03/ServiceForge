# review-tools-copilot — Local plugin for GitHub Copilot in VS Code

Install and test steps (local discovery):

1. Ensure VS Code has GitHub Copilot Chat installed and `chat.plugins.enabled` is `true`.
2. Add the plugin folder to workspace settings (or user settings):

```
"chat.pluginLocations": {
  "${workspaceFolder}/review-tools-copilot": true
}
```

3. Open the Chat view → Gear → Agent Customizations → Plugins (or use the Extensions view to find Agent Plugins). The plugin should appear under "Agent Plugins - Installed".
4. Enable the plugin.
5. Invoke the create-pr skill by name in chat: `review-tools-copilot:create-pr` and follow prompts. The skill will call GitHub MCP `create_pull_request` with `draft: true` and return the PR URL and number.
6. Invoke the pr-reviewer agent from the agent picker or by running the namespaced skill `review-tools-copilot:review-pr <pr-number>` and confirm it submits a pending review.

Notes:
- The plugin's `.mcp.json` references the MCP server; keep authentication (PAT) out of committed files — configure your Copilot MCP auth in VS Code or environment variables instead.
- This plugin packages only reusable skills/agents and no application source, tests, reports, or secrets.
