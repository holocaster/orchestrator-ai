CREATE TABLE goal_runs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal TEXT NOT NULL,
    state VARCHAR(32) NOT NULL,
    outcome VARCHAR(32),
    final_answer TEXT,
    failure_reason TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT goal_runs_completed_outcome CHECK (
        (state = 'COMPLETED' AND outcome IS NOT NULL AND final_answer IS NOT NULL)
        OR (state <> 'COMPLETED' AND outcome IS NULL AND final_answer IS NULL)
    )
);

CREATE UNIQUE INDEX one_running_goal_run
    ON goal_runs ((state))
    WHERE state = 'RUNNING';

CREATE TABLE orchestration_cycles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    goal_run_id UUID NOT NULL REFERENCES goal_runs(id),
    cycle_number INTEGER NOT NULL,
    decision VARCHAR(32),
    created_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    UNIQUE (goal_run_id, cycle_number)
);

CREATE TABLE model_exchanges (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    orchestration_cycle_id UUID NOT NULL REFERENCES orchestration_cycles(id),
    role VARCHAR(32) NOT NULL,
    prompt TEXT NOT NULL,
    response TEXT,
    provider_request_id VARCHAR(255),
    prompt_tokens INTEGER,
    completion_tokens INTEGER,
    created_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ
);
