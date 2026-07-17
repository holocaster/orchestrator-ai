# MVP Architecture

## Product slice

The MVP validates one idea: an Orchestrator can decompose a textual Goal, delegate sequential text work to a generic Worker, and return a useful final answer.

The complete flow is:

1. The user submits a Goal.
2. The Orchestrator chooses `DELEGATE` or `FINISH`.
3. A Delegation invokes one Worker and produces one textual Worker Result.
4. The Orchestrator receives prior results and decides again.
5. The Goal Run ends with a Goal Outcome or an explicit terminal failure state.

## Baseline

- Java 25
- Spring Boot 4.1.0
- Spring AI 2.0.0
- Maven
- PostgreSQL
- OpenRouter as the only LLM gateway
- React, TypeScript, and Vite

The product is a single Spring Boot application with a same-origin SPA. Keep package boundaries small and feature-oriented; the initial backend needs only `runs`, `execution`, and `llm` areas. It does not need microservices, a message broker, or a distributed scheduler.

## Operating constraints

- The deployment has one backend instance and one trusted user.
- The deployment must remain on a private network; public access is unsupported because the MVP has no authentication.
- Only one Goal Run may be `RUNNING`. Creation returns `409 Conflict` while another run is active.
- PostgreSQL persists current state and completed history.
- On startup, any previously `RUNNING` Goal Run becomes `FAILED`; execution is not resumed.

## Execution

Run a Goal Run on a Spring-managed background executor. Processing is sequential:

1. Build the Orchestrator prompt from the Goal, prior Delegations and Worker Results, and remaining limits.
2. Request one structured Orchestrator Decision from OpenRouter.
3. For `DELEGATE`, persist the assignment, invoke the Worker once, persist its textual result, and begin the next Orchestration Cycle.
4. For `FINISH`, persist `ACHIEVED` or `NOT_ACHIEVED` and mark the run `COMPLETED`.

The application validates every structured response. Invalid output, provider errors, network errors, and timeouts mark the Goal Run `FAILED`. The MVP performs no automatic retry.

The final available Orchestration Cycle may only return `FINISH`. Mark the Goal Run `LIMIT_REACHED` if that cycle returns `DELEGATE`, or if the Orchestrator requests another Worker Run after the Worker limit is exhausted.

### Cancellation

A cancellation request blocks new model calls. If a call is already active, allow it to finish so usage can be recorded, ignore its decision or result, and then mark the Goal Run `CANCELLED`. Do not attempt forced interruption.

## Agent configuration

Configure exactly one Orchestrator and one generic Worker through application properties. Each defines its OpenRouter model, system prompt, temperature, and maximum output tokens.

There is no profile database, profile versioning, administrator UI, per-run model selection, or arbitrary provider options.

Use Spring AI's OpenAI-compatible chat integration with the OpenRouter base URL. Request strict JSON Schema output for Orchestrator Decisions. Disable automatic tool execution; the application owns the orchestration loop.

## Context and limits

Each Orchestrator request includes the bounded, persisted run history rather than a separate Working Memory. Validate configured maximum sizes for the Goal, Delegations, Worker Results, model prompts, and model outputs.

Application properties define the maximum Orchestration Cycles and Worker Runs. OpenRouter's API-key credit limit is the external spending boundary.

Record provider-reported prompt tokens, completion tokens, total cost, and identifiers after each call. Display accumulated usage, but do not promise an atomic per-run cost ceiling or implement reservations and reconciliation.

## Persistence

Persist only the records needed to reconstruct and inspect a run:

- Goal Run and current state
- Orchestration Cycles and parsed decisions
- Worker Runs, Delegations, and textual Worker Results
- Model prompts, responses, usage, cost, provider identifiers, and summarized errors

Use Spring Data JDBC for ordinary persistence and Flyway for schema migrations. Current-state rows are authoritative; there is no event-sourced model, generic Run Event log, or replay-based recovery.

Never persist API keys, authorization headers, cookies, or HTTP wire dumps.

## API and live updates

The MVP needs these operations:

- Create a Goal Run
- Read the active or a specific Goal Run
- List recent Goal Runs
- Request cancellation
- Open the Goal Run SSE stream

An SSE connection first receives a complete current snapshot and then live updates from the running application. Send a heartbeat to keep the connection alive. On reconnect, send a new snapshot; do not implement `Last-Event-ID`, event sequencing, or exact replay.

## Interface

Use one screen containing:

- Goal input and start action
- Current state and cancellation action
- Orchestrator decisions, Delegations, and Worker Results
- Final answer
- Accumulated token and cost usage
- A simple recent-run list

Do not add dashboards, charts, files, profile management, account administration, or multi-page navigation.

## Verification

Prioritize tests for the product loop:

- Orchestrator `DELEGATE` and `FINISH` decisions
- sequential Worker execution
- cycle and Worker Run limits
- one-active-run invariant
- cooperative cancellation
- startup failure of interrupted runs
- invalid model responses and provider failures
- SSE snapshot and live update delivery
- PostgreSQL persistence with integration tests

## After the MVP

Defer all of the following until the core orchestration flow is validated:

- multiple users, authentication, and account administration
- dynamic and versioned agent profiles
- parallel Workers and Delegation Batches
- file workspaces, revisions, and promotion
- Human Decision Requests, pause, response, expiry, and resume
- automatic retries and ambiguous-call recovery
- hard per-run cost guarantees and reservations
- multiple backend instances and automatic run recovery
- exact SSE replay and a general event log
- richer dashboards, metrics, and administration screens
