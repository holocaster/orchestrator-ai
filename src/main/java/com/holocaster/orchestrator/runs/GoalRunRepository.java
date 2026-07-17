package com.holocaster.orchestrator.runs;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface GoalRunRepository extends CrudRepository<GoalRun, UUID> {

    @Query("SELECT * FROM goal_runs WHERE state = 'RUNNING' LIMIT 1")
    Optional<GoalRun> findRunning();
}
