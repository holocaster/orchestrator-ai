package com.holocaster.orchestrator.runs;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("goal_runs")
public record GoalRun(
        @Id UUID id,
        String goal,
        GoalRunState state,
        GoalOutcome outcome,
        String finalAnswer,
        String failureReason,
        Instant createdAt,
        Instant updatedAt) {

    public static GoalRun start(String goal, Instant now) {
        return new GoalRun(null, goal, GoalRunState.RUNNING, null, null, null, now, now);
    }

    public GoalRun complete(GoalOutcome completedOutcome, String answer, Instant now) {
        return new GoalRun(id, goal, GoalRunState.COMPLETED, completedOutcome, answer, null, createdAt, now);
    }

    public GoalRun fail(String reason, Instant now) {
        return new GoalRun(id, goal, GoalRunState.FAILED, null, null, reason, createdAt, now);
    }
}
