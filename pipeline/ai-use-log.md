AI Use Log

PR: https://github.com/garimajain03/ServiceForge/pull/4

This file records reviewer-facing reflections and the Minimum Acceptance Criteria mapping. It is intentionally concise; feature-scoped details live under pipeline/feature2/ai-use-log.md.

Reflection (short)

- Moving the review agent from a local working tree to the remote PR diff changes the evidence source (`get_pull_request_diff` vs local file reads) and adds a transactional posting step (create pending review → add inline comments → submit pending review).
- The checklist item hardest to express inline is design/ADR alignment because it spans multiple files and historical decisions; an inline comment must reference the ADR section for context.

PreToolUse hook evidence required (reviewer expects):

- The hook output should be a JSON array with one entry per scanned file: {"file":"path","exitCode":0,"stderr":""} for clean files. Any nonzero exitCode or non-empty stderr will cause the reviewer to mark the secrets-scan as Fail and require remediation.
