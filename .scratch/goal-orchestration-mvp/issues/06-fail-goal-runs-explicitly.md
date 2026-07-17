# 06 — Fail Goal Runs explicitly and safely

**What to build:** Turn every definitive execution or provider problem into one inspectable `FAILED` Goal Run, without automatic retries or unsafe recovery after an application restart.

**Blocked by:** 03 — Execute sequential Delegations through the Worker.

**Status:** ready-for-agent

- [ ] Invalid Orchestrator JSON, an invalid action payload, empty required output, and oversized required output transition the Goal Run to `FAILED` without executing an action.
- [ ] Provider errors, network failures, and model-call timeouts transition the Goal Run to `FAILED` with a sanitized user-visible reason.
- [ ] Missing or invalid required usage data transitions the Goal Run to `FAILED` without querying a secondary reconciliation endpoint.
- [ ] A failed model call produces exactly one provider attempt and never triggers an automatic retry.
- [ ] A failed Goal Run has no Goal Outcome and cannot start more Orchestrator or Worker calls.
- [ ] On application startup, every previously `RUNNING` Goal Run becomes `FAILED` with an interruption reason and is not resumed.
- [ ] API responses, SSE updates, and the SPA expose failure state and safe reason without secrets or raw authorization data.
- [ ] Integration tests script every failure category through the fake OpenRouter endpoint and verify one attempt, terminal state, and restart behavior.
