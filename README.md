# Goal Orchestration MVP

A private, single-user application that runs one bounded Goal at a time through an OpenRouter-backed Orchestrator.

The backend is a Spring Boot application, the UI is a React SPA, and PostgreSQL stores execution state. The production build packages the SPA into the Spring Boot JAR and serves both from the same origin.

This MVP has no authentication and must not be exposed publicly.

## Stack

- Java 25, Spring Boot 4.1, Spring AI 2.0, and Maven
- React 19, TypeScript 7, Vite 8, and Tailwind CSS 4
- PostgreSQL 17 and Flyway
- Docker Compose and Testcontainers

## Run with Docker Compose

Set an OpenRouter API key and start the complete application:

```bash
export OPENROUTER_API_KEY="your-key"
docker compose up --build
```

Open `http://localhost:8080`.

PostgreSQL data persists in the `postgres-data` volume. Stop the containers with:

```bash
docker compose down
```

## Local development

Requirements: Java 25, Maven 3.8+, Node.js 24+, and Docker or Podman.

Start PostgreSQL and the backend:

```bash
docker compose up -d postgres
export OPENROUTER_API_KEY="your-key"
mvn spring-boot:run
```

For frontend development with hot reload, run Vite in another terminal. Requests under `/api` are proxied to the backend on port 8080.

```bash
cd frontend
npm ci
npm run dev
```

Open `http://localhost:5173`.

## Build and test

Build the frontend before packaging the JAR:

```bash
cd frontend
npm ci
npm run build
cd ..
mvn clean package
```

Run the backend integration tests and frontend type checking with:

```bash
mvn test
cd frontend && npm run typecheck
```

The backend tests use Testcontainers and require a running Docker or Podman daemon.

## Configuration

| Variable | Default | Purpose |
| --- | --- | --- |
| `OPENROUTER_API_KEY` | none | OpenRouter credential required for model calls |
| `OPENROUTER_BASE_URL` | `https://openrouter.ai/api/v1` | OpenAI-compatible gateway URL |
| `ORCHESTRATOR_MODEL` | `openai/gpt-5-mini` | Orchestrator model |
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/orchestrator` | JDBC connection URL |
| `DATABASE_USERNAME` | `orchestrator` | Database user |
| `DATABASE_PASSWORD` | `orchestrator` | Database password |

Additional model settings are defined in `src/main/resources/application.yml`.

## Project documentation

- [Domain language](CONTEXT.md)
- [MVP architecture](docs/MVP-ARCHITECTURE.md)
- [Architecture decisions](docs/adr/)
