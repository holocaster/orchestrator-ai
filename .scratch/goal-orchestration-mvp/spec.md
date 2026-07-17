# Goal Orchestration MVP

Status: ready-for-agent

## Problem Statement

The user needs to validate whether an Orchestrator can decompose a textual Goal, delegate useful work, and return a coherent final answer. The previous design mixed that product hypothesis with multi-user administration, dynamic profiles, parallel execution, file workspaces, exact event replay, and financial governance, making the first release too large to implement and evaluate as an MVP.

## Solution

Provide a private, single-user application in which the user submits one Goal at a time. One fixed Orchestrator may sequentially delegate text assignments to one generic Worker configuration. Each Worker Run returns text, the Orchestrator considers the accumulated history, and the Goal Run finishes with an `ACHIEVED` or `NOT_ACHIEVED` Goal Outcome.

The application persists the execution trace in PostgreSQL, streams current progress through snapshot-based SSE, exposes a single responsive screen, records model usage and cost, and fails explicitly when execution, provider, validation, or configured limits prevent completion.

## User Stories

1. As a user, I want to submit a textual Goal, so that the Orchestrator can begin pursuing the outcome I need.
2. As a user, I want the submitted Goal to remain unchanged during its Goal Run, so that the execution has a stable objective.
3. As a user, I want only one Goal Run active at a time, so that the MVP remains understandable and operationally simple.
4. As a user, I want a new Goal Run request rejected while another run is active, so that concurrent executions cannot be started accidentally.
5. As a user, I want to return to the active Goal Run after a conflicting start attempt, so that I can continue observing the work already in progress.
6. As a user, I want the Orchestrator to decide between delegating work and finishing, so that every cycle advances the Goal through a clear action.
7. As a user, I want each Delegation to contain a bounded textual assignment, so that the Worker receives a focused piece of work.
8. As a user, I want a Worker Run to make exactly one model call, so that delegated work remains bounded and predictable.
9. As a user, I want Worker Runs to execute sequentially, so that results arrive in a deterministic order without parallel coordination complexity.
10. As a user, I want each successful Worker Run to return a textual Worker Result, so that the Orchestrator can use it without file handling.
11. As a user, I want the Orchestrator to receive the Goal and prior Delegations and Worker Results on each cycle, so that it can make informed decisions.
12. As a user, I want the Orchestrator to continue delegating while limits remain, so that it can refine or expand the work when needed.
13. As a user, I want the Orchestrator to finish with `ACHIEVED` when it believes the Goal is satisfied, so that I receive a clear successful outcome.
14. As a user, I want the Orchestrator to finish with `NOT_ACHIEVED` when available information is insufficient, so that failure to satisfy the Goal is explicit.
15. As a user, I want every completed Goal Run to include a textual final answer, so that the execution produces a useful deliverable.
16. As a user, I want configured Orchestration Cycle limits, so that a run cannot continue indefinitely.
17. As a user, I want a configured Worker Run limit, so that the Orchestrator cannot delegate indefinitely.
18. As a user, I want the final available Orchestration Cycle restricted to `FINISH`, so that the Orchestrator gets a final opportunity to produce an answer.
19. As a user, I want a run marked `LIMIT_REACHED` when the Orchestrator exceeds an available limit, so that bounded termination is distinguishable from failure.
20. As a user, I want to request cancellation of the active Goal Run, so that I can stop work I no longer need.
21. As a user, I want cancellation to prevent new model calls immediately, so that no additional work starts after my request.
22. As a user, I want an already active model call allowed to finish only for usage recording, so that accounting remains accurate.
23. As a user, I want a decision or result returned after cancellation ignored, so that cancelled work cannot change the Goal Run outcome.
24. As a user, I want a cancelled Goal Run marked `CANCELLED`, so that its terminal reason is clear.
25. As a user, I want to start another Goal Run after the current run becomes terminal, so that I can continue using the product.
26. As a user, I want provider, network, timeout, and validation errors to mark the Goal Run `FAILED`, so that failures are explicit rather than hidden.
27. As a user, I want no automatic retry after a model-call failure, so that the MVP avoids duplicate work and ambiguous charges.
28. As a user, I want malformed Orchestrator Decisions rejected, so that invalid model output cannot execute an action.
29. As a user, I want completed and terminal Goal Runs preserved, so that I can inspect prior work.
30. As a user, I want an interrupted active Goal Run marked `FAILED` after application restart, so that the system never pretends uncertain work resumed safely.
31. As a user, I want a list of recent Goal Runs, so that I can revisit earlier outcomes.
32. As a user, I want to open a Goal Run and see its current state, so that I understand whether it is active or terminal.
33. As a user, I want a chronological orchestration trace, so that I can follow Orchestrator Decisions, Delegations, and Worker Results.
34. As a user, I want to inspect the prompt and response for each model call, so that I can evaluate and diagnose orchestration behavior.
35. As a user, I want prompt and completion token counts recorded, so that I can understand model usage.
36. As a user, I want provider-reported cost accumulated for a Goal Run, so that I can observe spending without expecting a hard per-run guarantee.
37. As a user, I want an SSE snapshot immediately after connecting, so that the interface starts from authoritative current state.
38. As a user, I want live updates after the SSE snapshot, so that I can follow execution without manually refreshing.
39. As a user, I want reconnection to deliver a fresh snapshot, so that missed updates do not leave the interface stale.
40. As a user, I want the interface to show SSE connection state, so that I can distinguish active updates from reconnection.
41. As a user, I want a single screen for creation, progress, cancellation, outcomes, usage, and recent runs, so that the MVP remains focused.
42. As a user, I want the single screen usable on mobile and desktop, so that no essential operation depends on a specific screen size.
43. As a keyboard user, I want visible focus and semantic status labels, so that the interface remains operable and understandable without relying on color.
44. As a user who prefers reduced motion, I want nonessential motion reduced, so that the interface respects my accessibility preference.
45. As an operator, I want one fixed Orchestrator configuration and one fixed Worker configuration, so that the MVP requires no profile administration.
46. As an operator, I want model, system prompt, temperature, and output limit supplied through application configuration, so that deployment settings remain explicit.
47. As an operator, I want the application restricted to a private network, so that the authentication-free MVP is not exposed publicly.
48. As an operator, I want the OpenRouter key to carry an external spending limit, so that there is a provider-side global boundary while per-run reservations are deferred.
49. As an operator, I want secrets excluded from persisted model exchanges, so that diagnostic visibility does not store API keys or authorization material.
50. As a developer, I want the product loop testable through one public API and SSE seam, so that tests verify behavior without coupling to internal classes.

## Implementation Decisions

- Build one Spring Boot application and one same-origin React SPA. The backend remains a single deployable process with small feature-oriented areas for Goal Runs, execution, and LLM integration.
- Use Java 25, Spring Boot 4.1.0, Spring AI 2.0.0, Maven, PostgreSQL, OpenRouter, React, TypeScript, Vite, Tailwind CSS, and accessible Radix primitives when needed.
- Support exactly one trusted user and one backend instance. Do not implement authentication. Deployment documentation must state that public network exposure is unsupported.
- Enforce at the persistence boundary that at most one Goal Run is `RUNNING`. A create operation returns HTTP `409 Conflict` when the invariant would be violated.
- A Goal Run begins directly in `RUNNING`; there is no queue. Terminal Goal Run States are `COMPLETED`, `FAILED`, `LIMIT_REACHED`, and `CANCELLED`.
- A completed Goal Run stores a Goal Outcome of `ACHIEVED` or `NOT_ACHIEVED` plus a textual answer. Failure states store a safe, user-visible reason but no Goal Outcome.
- Execute Goal Runs on a Spring-managed background executor. Do not introduce a distributed scheduler, message broker, internal HTTP calls, or microservices.
- Configure one Orchestrator and one generic Worker through application properties. The user cannot select or modify models or prompts per run.
- One Orchestration Cycle makes one Orchestrator model call and accepts exactly one structured Orchestrator Decision.
- The `DELEGATE` decision contains one bounded textual assignment. It creates one Worker Run, which makes one non-streaming model call and returns one bounded textual Worker Result.
- The `FINISH` decision contains the Goal Outcome and final textual answer. It transitions the Goal Run to `COMPLETED`.
- Worker Runs execute sequentially. The next Orchestration Cycle starts only after the current Worker Run and its model exchange are persisted.
- Each Orchestrator prompt contains the immutable Goal, prior Delegations and Worker Results, and remaining Orchestration Cycle and Worker Run limits. There is no separate Working Memory.
- Configure maximum Goal length, assignment length, Worker Result length, prompt size, output size, Orchestration Cycles, Worker Runs, model-call timeout, and recent-run list size. Do not hard-code product limits in the domain model.
- The final available Orchestration Cycle accepts only `FINISH`. A `DELEGATE` decision on that cycle, or after the Worker Run limit is exhausted, transitions the Goal Run to `LIMIT_REACHED` without invoking a Worker.
- A Cancellation Request sets a durable cancellation flag and prevents new model calls. If a call is already active, persist its response and usage, discard its action or Worker Result, and transition the Goal Run to `CANCELLED`.
- Do not retry any failed model call automatically. Invalid structured output, empty or oversized required output, provider errors, network errors, and timeouts transition the Goal Run to `FAILED`.
- Missing or invalid required usage data also transitions the Goal Run to `FAILED`. Do not query a secondary generation endpoint for reconciliation in the MVP.
- On application startup, transition any Goal Run still marked `RUNNING` to `FAILED` with an interruption reason. Do not resume execution or reconcile ambiguous calls.
- Use Spring AI's OpenAI-compatible chat integration with the OpenRouter base URL. The configured Orchestrator model must support strict JSON Schema output.
- Disable automatic tool execution and automatic model loops. The application validates structured decisions and owns every state transition.
- Persist Goal Runs, Orchestration Cycles, Orchestrator Decisions, Worker Runs, Delegations, Worker Results, and model exchanges in PostgreSQL.
- A model exchange stores the role, effective prompt, raw response when available, parsed decision when applicable, provider identifiers, prompt tokens, completion tokens, provider-reported cost, timestamps, and a sanitized error summary.
- Store monetary values as decimal values. Do not persist API keys, authorization headers, cookies, or byte-for-byte HTTP wire data.
- Use current-state records as authoritative data. Do not implement event sourcing, a generic Run Event table, or state reconstruction through replay.
- Use Spring Data JDBC for ordinary persistence and Flyway as the only schema migration mechanism.
- The HTTP API exposes operations to create a Goal Run, get the active Goal Run, get one Goal Run, list recent Goal Runs, and request cancellation. Responses use the canonical Goal Run terms from the domain glossary.
- The SSE API is scoped to one Goal Run. Each connection emits a complete `snapshot`, followed by live `update` messages and heartbeat comments.
- SSE does not expose event IDs or exact replay. A reconnect starts with a new authoritative snapshot, and duplicate snapshots must be safe for the SPA to replace wholesale.
- The SPA uses one page with a Goal composer, active-run state, cancellation action, chronological orchestration trace, final answer, accumulated usage, SSE connection state, and recent-run list.
- Disable Goal creation while a run is active. Require confirmation before sending a Cancellation Request.
- Keep technical model-exchange details progressively disclosed within the trace. Do not add separate dashboard, profile, file, account, or administration pages.
- Preserve the approved dark visual direction as a mood reference. Status remains semantic and never depends on color alone.
- Use the OpenRouter API key's provider-side spending limit as the external global boundary. Display recorded cost as observational data, not as an atomic per-run guarantee.

## Testing Decisions

- The primary test seam is the public Goal Run HTTP API plus its SSE stream. Tests start the Spring Boot application with a real PostgreSQL database and a deterministic fake OpenRouter endpoint.
- Good tests assert externally visible behavior: HTTP status, Goal Run State, Goal Outcome, orchestration trace, usage, and SSE messages. They do not assert private method calls, repository implementation, executor internals, or prompt-builder class structure.
- Use PostgreSQL integration tests rather than an in-memory database so that the one-active-run invariant, transactions, decimal costs, migrations, and startup recovery run against the production database behavior.
- The fake OpenRouter endpoint must script ordered Orchestrator Decisions, Worker Results, errors, delays, malformed responses, provider identifiers, token usage, and costs. It must not reproduce OpenRouter's internal implementation.
- Verify the happy path in one acceptance test: create a Goal Run, receive an SSE snapshot, observe `DELEGATE`, observe one Worker Result, observe `FINISH`, and read the completed `ACHIEVED` Goal Outcome.
- Verify a completed `NOT_ACHIEVED` Goal Outcome without requiring a Human Decision Request.
- Verify that a second create request receives `409 Conflict` while a Goal Run is active and succeeds after the first run is terminal.
- Verify that Worker Runs never overlap and that the next Orchestration Cycle sees the prior Worker Result.
- Verify Orchestration Cycle and Worker Run limit behavior, including the final-cycle `FINISH` restriction.
- Verify cooperative cancellation before a call and during a delayed call. The delayed response usage is persisted, while its decision or result is not applied.
- Verify invalid Orchestrator JSON, an invalid action payload, empty Worker output, oversized output, timeout, network failure, provider error, and malformed usage handling.
- Verify that provider failures produce one call only and never trigger an automatic retry.
- Verify startup recovery by seeding a `RUNNING` Goal Run, restarting the application context, and observing a terminal `FAILED` state.
- Verify that a new SSE connection receives a snapshot, live changes follow it, heartbeat comments keep the stream active, and reconnection receives current state without replay assumptions.
- Verify model exchanges through the public Goal Run detail representation: prompts, responses, parsed decisions, usage, cost, identifiers, and sanitized errors are inspectable while secrets are absent.
- Keep SPA logic thin. Add one browser smoke flow for Goal submission, live trace rendering, completion, recent history, and cancellation confirmation; core orchestration behavior remains covered at the API/SSE seam.
- No prior code or test suite exists in the project. The first implementation establishes the Spring Boot integration-test pattern and the deterministic fake-provider fixture for later features.

## Out of Scope

- Multiple users, authentication, authorization, registration, and account administration
- Dynamic, selectable, versioned, retired, or suspended agent profiles
- Specialized Worker types
- Parallel Worker Runs, Delegation Batches, queues, and concurrent Goal Runs
- File input, file output, workspaces, revisions, promotion, merge, binary artifacts, and object storage
- Human Decision Requests, mid-run clarification, pause, response deadlines, expiry, and resume
- Automatic retries, transport-attempt history, ambiguous-call recovery, and automatic execution resume
- Hard per-run cost guarantees, reservations, reconciliation workflows, preflight token pricing, and profile suspension
- Multiple backend instances, distributed leases, distributed scheduling, and automatic failover
- Generic Run Events, exact SSE replay, `Last-Event-ID`, and cross-instance SSE delivery
- Public deployment and cross-origin browser API access
- Dashboards, charts, metrics pages, multi-page navigation, profile editing, administration screens, and file viewers
- Model tools, retrieval, chat memory advisors, general-purpose tool execution, and Worker-to-Worker delegation

## Further Notes

- The domain glossary is authoritative for Goal, Goal Run, Goal Run State, Goal Outcome, Orchestrator, Orchestration Cycle, Orchestrator Decision, Delegation, Worker, Worker Run, Worker Result, Execution Limits, and Cancellation Request.
- The PostgreSQL and OpenRouter ADRs remain active. The earlier PostgreSQL-backed virtual workspace ADR is deprecated and does not apply to this MVP.
- Spring Boot 4.1 background task execution, Spring AI 2.0 OpenAI-compatible configuration and JSON Schema support, and OpenRouter usage and API-key spending-limit capabilities were checked against current documentation during specification.
- The visual baseline is a mood reference only; it does not restore the previously proposed dashboard information architecture.
- This project contained documentation but no application code or tests when this spec was published.
