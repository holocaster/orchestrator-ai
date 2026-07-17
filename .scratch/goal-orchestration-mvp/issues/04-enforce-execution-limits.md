# 04 — Enforce Execution Limits

**What to build:** Bound every Goal Run by configured Orchestration Cycle and Worker Run limits, give the Orchestrator a final opportunity to finish, and expose limit termination clearly to the user.

**Blocked by:** 03 — Execute sequential Delegations through the Worker.

**Status:** ready-for-agent

- [ ] Application configuration defines positive maximums for Orchestration Cycles and Worker Runs and exposes remaining limits to the Orchestrator.
- [ ] Every Orchestration Cycle is counted once, including a cycle that returns an invalid or disallowed decision.
- [ ] The final available Orchestration Cycle accepts `FINISH` and rejects `DELEGATE` without invoking a Worker.
- [ ] A `DELEGATE` decision after the Worker Run limit is exhausted invokes no Worker.
- [ ] Violating either available limit transitions the Goal Run to `LIMIT_REACHED` with a safe explanatory reason and no Goal Outcome.
- [ ] API responses, SSE updates, and the SPA distinguish `LIMIT_REACHED` from `FAILED`.
- [ ] Integration tests cover cycle exhaustion, Worker Run exhaustion, successful `FINISH` on the last cycle, and rejection of a final-cycle Delegation.
