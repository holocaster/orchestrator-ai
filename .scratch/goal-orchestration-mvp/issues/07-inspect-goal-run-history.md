# 07 — Inspect Goal Run history and model exchanges

**What to build:** Let the user revisit recent Goal Runs and inspect the persisted orchestration evidence needed to evaluate results, usage, costs, and failures.

**Blocked by:** 06 — Fail Goal Runs explicitly and safely.

**Status:** ready-for-agent

- [ ] The public API lists recent Goal Runs in a stable newest-first order with a configured maximum result size.
- [ ] The public API returns one Goal Run with its chronological Orchestration Cycles, Orchestrator Decisions, Worker Runs, Delegations, Worker Results, and model exchanges.
- [ ] Every model exchange records the effective prompt, raw response when available, parsed decision when applicable, provider identifiers, timestamps, tokens, decimal cost, and sanitized error.
- [ ] Goal Run detail exposes accumulated prompt tokens, completion tokens, and provider-reported cost as observational usage.
- [ ] API keys, authorization headers, cookies, and byte-for-byte HTTP wire data are never persisted or returned.
- [ ] The single-screen interface includes a compact recent-run list and can open a terminal Goal Run without starting a new execution.
- [ ] Model-exchange details are progressively disclosed inside the chronological trace instead of requiring a separate administration page.
- [ ] Integration tests verify successful and failed history, stable ordering, usage totals, decimal cost, sanitized errors, and absence of secrets through public responses.
