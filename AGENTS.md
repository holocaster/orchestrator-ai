# Project Instructions

## Read first

- Read `CONTEXT.md` before changing domain behavior or terminology.
- Read `docs/MVP-ARCHITECTURE.md` before changing product scope or execution flow.
- Read relevant decisions under `docs/adr/` before changing persistence or integrations.
- Surface conflicts with these documents instead of silently overriding them.

## Product boundaries

- This is a private, single-user MVP with no authentication. Never present it as safe for public exposure.
- Keep one deployable Spring Boot application. The React SPA is built into and served from the same JAR.
- PostgreSQL is the only application store. Use Spring Data JDBC and version every schema change with Flyway.
- OpenRouter is the only LLM gateway.
- Only one Goal Run may be active. Orchestration Cycles and Worker Runs execute sequentially.
- Do not introduce microservices, message brokers, parallel Workers, retries, dynamic agent profiles, or other deferred scope from `docs/MVP-ARCHITECTURE.md`.

## Conventions

- Use the canonical terms from `CONTEXT.md` in code, APIs, tests, specs, and documentation.
- Keep Java packages feature-oriented under `runs`, `execution`, and `llm`.
- Keep application endpoints under `/api`; the frontend uses same-origin requests.
- Never persist API keys, authorization headers, cookies, or raw HTTP dumps.
- Keep code, user-facing copy, tests, and documentation in English.
- Do not edit or commit generated output from `target/`, `frontend/dist/`, or `frontend/node_modules/`.

## Verification

Run the smallest checks that cover the change:

```bash
mvn test
cd frontend && npm run typecheck
cd frontend && npm run build
docker compose config --quiet
docker compose build application
```

Java integration tests require Docker or Podman because they use Testcontainers.

## Project workflow

- Local specs and issues live under `.scratch/`; follow `docs/agents/issue-tracker.md` and `docs/agents/triage-labels.md`.
- Keep durable domain knowledge in `CONTEXT.md` and architectural decisions in `docs/adr/`.
