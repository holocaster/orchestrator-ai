package com.holocaster.orchestrator.runs;

import java.util.UUID;

public class GoalRunNotFoundException extends RuntimeException {

    public GoalRunNotFoundException(UUID id) {
        super("Execução não encontrada: " + id);
    }
}
