package com.holocaster.orchestrator.execution;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("orchestration_cycles")
public record OrchestrationCycle(
        @Id UUID id,
        UUID goalRunId,
        int cycleNumber,
        String decision,
        Instant createdAt,
        Instant completedAt) {

    static OrchestrationCycle start(UUID goalRunId, Instant now) {
        return new OrchestrationCycle(null, goalRunId, 1, null, now, null);
    }

    OrchestrationCycle finish(Instant now) {
        return new OrchestrationCycle(id, goalRunId, cycleNumber, "FINISH", createdAt, now);
    }
}
