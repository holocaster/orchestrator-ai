# Goal Orchestration MVP

A private, single-user Spring Boot application that runs one textual Goal at a time through a fixed OpenRouter Orchestrator.

## Requirements

- Java 25
- Maven 3.8+
- Node.js 24+
- PostgreSQL 17
- Docker or Podman for integration tests

## Build and test

```bash
cd frontend
npm ci
npm run build
cd ..
mvn test
mvn clean package
```

The packaged JAR includes the React SPA from `frontend/dist`.

## Run

Set `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `OPENROUTER_API_KEY`, then run:

```bash
java -jar target/orchestrator-ia-0.0.1-SNAPSHOT.jar
```

Open `http://localhost:8080`. This authentication-free MVP must remain on a private network and must not be exposed publicly.

## Docker Compose

Set `OPENROUTER_API_KEY`, then build and start the application with PostgreSQL:

```bash
docker compose up --build
```

Open `http://localhost:8080`. PostgreSQL data is stored in the `postgres-data` volume.
