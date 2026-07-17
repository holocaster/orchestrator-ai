# 01 — Complete a Goal Run with `FINISH`

**What to build:** Allow the user to submit one textual Goal, have the fixed Orchestrator finish it through OpenRouter, and inspect the persisted Goal Outcome from the initial single-screen interface. This ticket establishes the smallest complete path through the application and the primary API test seam.

**Blocked by:** None — can start immediately.

**Status:** ready-for-agent

- [ ] The Spring Boot application, React SPA, PostgreSQL schema, and deterministic fake OpenRouter test fixture build and start with the agreed baseline.
- [ ] Creating a Goal Run persists its immutable Goal, starts background execution, and returns its public representation.
- [ ] A persistence-level invariant permits only one `RUNNING` Goal Run, and a concurrent creation attempt returns HTTP `409 Conflict`.
- [ ] The fixed Orchestrator configuration sends one structured request through the OpenRouter integration and accepts a valid `FINISH` decision.
- [ ] A valid `FINISH` decision stores `ACHIEVED` or `NOT_ACHIEVED`, the textual answer, and a terminal `COMPLETED` Goal Run State.
- [ ] The initial interface lets the user submit a Goal and inspect the current or completed Goal Run without exposing profile or account controls.
- [ ] An integration test proves Goal submission through completed Goal Outcome using real PostgreSQL and the fake OpenRouter endpoint.
- [ ] Secrets and authorization material are absent from persisted records and public responses.
