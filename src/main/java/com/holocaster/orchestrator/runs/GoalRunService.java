package com.holocaster.orchestrator.runs;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.holocaster.orchestrator.execution.GoalRunExecutor;
import org.springframework.stereotype.Service;

@Service
public class GoalRunService {

    private final GoalRunRepository goalRuns;
    private final GoalRunExecutor executor;
    private final Clock clock;

    public GoalRunService(GoalRunRepository goalRuns, GoalRunExecutor executor) {
        this.goalRuns = goalRuns;
        this.executor = executor;
        this.clock = Clock.systemUTC();
    }

    public GoalRun create(String goal) {
        Instant now = clock.instant();
        GoalRun created = goalRuns.save(GoalRun.start(goal.strip(), now));
        executor.execute(created.id());
        return created;
    }

    public GoalRun get(UUID id) {
        return goalRuns.findById(id).orElseThrow(() -> new GoalRunNotFoundException(id));
    }
}
