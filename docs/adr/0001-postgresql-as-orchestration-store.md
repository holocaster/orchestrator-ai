# Use PostgreSQL as the MVP store

PostgreSQL will persist Goal Runs, Orchestration Cycles, Worker Runs, and model exchanges. One database keeps the single-instance MVP operationally simple and preserves completed history, without adding Redis, a message broker, event sourcing, or automatic execution recovery.
