# 02 — Stream Goal Run state through snapshot SSE

**What to build:** Let the user observe an active Goal Run through a live SSE connection that always begins from authoritative current state and recovers through a fresh snapshot instead of event replay.

**Blocked by:** 01 — Complete a Goal Run with `FINISH`.

**Status:** ready-for-agent

- [ ] Opening the SSE stream for a Goal Run emits a complete `snapshot` of its current public representation before live updates.
- [ ] Goal Run State and Goal Outcome changes emit live `update` messages to the connected user.
- [ ] Heartbeat comments keep an otherwise idle connection alive without becoming domain events.
- [ ] Reconnection emits a new authoritative snapshot and does not require event IDs, `Last-Event-ID`, or exact replay.
- [ ] The SPA replaces local run state with each snapshot, applies subsequent live updates, and displays connected or reconnecting status.
- [ ] Disconnecting the browser releases server-side stream resources promptly.
- [ ] Integration tests cover initial snapshot, live completion, heartbeat, disconnect, and reconnect behavior through the public SSE seam.
