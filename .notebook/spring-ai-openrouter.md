# Spring AI 2 with OpenRouter
> Configuration details confirmed through the fake-provider acceptance test

Entry: `src/main/java/com/holocaster/orchestrator/llm/OrchestratorClient.java:finish()`

- Boot 4 JSON bean uses Jackson 3 (`tools.jackson.*`)
- `src/main/resources/application.yml` — OpenRouter base URL includes `/v1`; SDK appends `/chat/completions`
- Chat properties live directly under `spring.ai.openai.chat`
- Per-call structured options mutate `ChatModel.getDefaultOptions()`; a fresh builder replaces the configured model with its own default
- `src/test/java/com/holocaster/orchestrator/GoalRunAcceptanceTest.java` proves URL, configured model, and strict JSON Schema request

Updated: 2026-07-17
