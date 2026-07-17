package com.holocaster.orchestrator.runs;

import java.net.URI;
import java.time.Instant;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/goal-runs")
public class GoalRunController {

    private final GoalRunService goalRuns;

    public GoalRunController(GoalRunService goalRuns) {
        this.goalRuns = goalRuns;
    }

    @PostMapping
    ResponseEntity<GoalRunResponse> create(@Valid @RequestBody CreateGoalRunRequest request) {
        GoalRun created = goalRuns.create(request.goal());
        return ResponseEntity.created(URI.create("/api/goal-runs/" + created.id()))
                .body(GoalRunResponse.from(created));
    }

    @GetMapping("/{id}")
    GoalRunResponse get(@PathVariable UUID id) {
        return GoalRunResponse.from(goalRuns.get(id));
    }

    record CreateGoalRunRequest(@NotBlank @Size(max = 10_000) String goal) {
    }

    record GoalRunResponse(
            UUID id,
            String goal,
            GoalRunState state,
            GoalOutcome outcome,
            String finalAnswer,
            String failureReason,
            Instant createdAt,
            Instant updatedAt) {

        static GoalRunResponse from(GoalRun run) {
            return new GoalRunResponse(
                    run.id(),
                    run.goal(),
                    run.state(),
                    run.outcome(),
                    run.finalAnswer(),
                    run.failureReason(),
                    run.createdAt(),
                    run.updatedAt());
        }
    }
}
