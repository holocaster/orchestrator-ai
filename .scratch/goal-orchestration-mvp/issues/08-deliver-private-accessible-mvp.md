# 08 — Deliver the private, accessible MVP experience

**What to build:** Finish the Goal Orchestration MVP as one responsive, accessible screen and a documented private single-instance deployment that a user can operate from Goal submission through terminal inspection.

**Blocked by:** 04 — Enforce Execution Limits; 05 — Cancel a Goal Run cooperatively; 07 — Inspect Goal Run history and model exchanges.

**Status:** ready-for-agent

- [ ] One screen contains the Goal composer, active Goal Run State, Cancellation Request action, connection state, chronological trace, final answer, usage totals, and recent-run list.
- [ ] Goal creation is disabled while a Goal Run is active, and a conflicting creation response returns the user to the active run.
- [ ] Every essential operation works on mobile and desktop without desktop-only controls.
- [ ] Interactive elements have visible keyboard focus, semantic names, and usable keyboard order.
- [ ] Goal Run States use text or semantic labels and never depend on color alone.
- [ ] Functional motion respects reduced-motion preferences while preserving state-change clarity.
- [ ] Deployment guidance requires one backend instance, a private network, same-origin serving, no public exposure, and an OpenRouter API key with a provider-side spending limit.
- [ ] Configuration and logs do not disclose secrets, and the interface does not expose authentication, profiles, files, dashboards, or administration features.
- [ ] A browser smoke test covers Goal submission, SSE progress, sequential Delegation, completed outcome, history inspection, and cancellation confirmation.
- [ ] The complete API/SSE acceptance suite remains green with real PostgreSQL and deterministic fake OpenRouter behavior.
