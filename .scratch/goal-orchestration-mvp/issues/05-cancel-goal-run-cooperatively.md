# 05 — Cancel a Goal Run cooperatively

**What to build:** Let the user request cancellation of the active Goal Run, prevent additional model work, and reach a reliable `CANCELLED` state without forcibly interrupting a model call already in progress.

**Blocked by:** 03 — Execute sequential Delegations through the Worker.

**Status:** ready-for-agent

- [ ] The public API accepts a Cancellation Request only for the active Goal Run and makes repeated requests safe.
- [ ] The SPA asks for confirmation before sending a Cancellation Request and immediately reflects that cancellation is pending.
- [ ] A durable cancellation flag prevents every new Orchestrator or Worker model call after the request is accepted.
- [ ] If no model call is active, the Goal Run transitions promptly to `CANCELLED`.
- [ ] If a model call is active, its response and usage are persisted, but its Orchestrator Decision or Worker Result is not applied.
- [ ] The Goal Run transitions to `CANCELLED` after the active call is recorded, and SSE communicates the terminal state.
- [ ] A new Goal Run can be created after cancellation becomes terminal.
- [ ] Integration tests cover cancellation between calls, during an Orchestrator call, during a Worker call, and repeated cancellation requests.
