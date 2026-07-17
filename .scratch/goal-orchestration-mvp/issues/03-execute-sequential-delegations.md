# 03 — Execute sequential Delegations through the Worker

**What to build:** Allow the Orchestrator to delegate bounded textual work to the fixed generic Worker, receive one textual Worker Result, and use that result in a later Orchestration Cycle before finishing the Goal Run.

**Blocked by:** 02 — Stream Goal Run state through snapshot SSE.

**Status:** ready-for-agent

- [ ] The structured Orchestrator Decision accepts `DELEGATE` with exactly one bounded textual assignment as an alternative to `FINISH`.
- [ ] A valid Delegation creates one Worker Run and invokes the fixed Worker configuration exactly once.
- [ ] A successful Worker Run persists one bounded textual Worker Result and its model exchange before another Orchestration Cycle begins.
- [ ] Worker Runs never overlap, and only one Delegation executes at a time.
- [ ] Each later Orchestrator request includes the immutable Goal, prior Delegations and Worker Results, and remaining limits.
- [ ] The public Goal Run detail and SSE updates expose a chronological trace of Orchestrator Decisions, Delegations, and Worker Results.
- [ ] The SPA renders the chronological trace while keeping the Goal and final answer visually dominant.
- [ ] An integration test proves the ordered path `DELEGATE` → Worker Result → `FINISH` → completed Goal Outcome.
