package com.holocaster.orchestrator.llm;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("model_exchanges")
public record ModelExchange(
        @Id UUID id,
        UUID orchestrationCycleId,
        String role,
        String prompt,
        String response,
        String providerRequestId,
        Integer promptTokens,
        Integer completionTokens,
        Instant createdAt,
        Instant completedAt) {

    public static ModelExchange start(UUID cycleId, String prompt, Instant now) {
        return new ModelExchange(null, cycleId, "ORCHESTRATOR", prompt, null, null, null, null, now, null);
    }

    public ModelExchange finish(
            String rawResponse,
            String requestId,
            Integer usedPromptTokens,
            Integer usedCompletionTokens,
            Instant now) {
        return new ModelExchange(
                id,
                orchestrationCycleId,
                role,
                prompt,
                rawResponse,
                requestId,
                usedPromptTokens,
                usedCompletionTokens,
                createdAt,
                now);
    }
}
