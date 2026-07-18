package com.holocaster.orchestrator.execution;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import com.holocaster.orchestrator.llm.ModelExchange;
import com.holocaster.orchestrator.llm.ModelExchangeRepository;
import com.holocaster.orchestrator.llm.OrchestratorClient;
import com.holocaster.orchestrator.llm.OrchestratorClient.OrchestratorCall;
import com.holocaster.orchestrator.runs.GoalRun;
import com.holocaster.orchestrator.runs.GoalRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class GoalRunExecutor {

    private static final Logger logger = LoggerFactory.getLogger(GoalRunExecutor.class);

    private final GoalRunRepository goalRuns;
    private final OrchestrationCycleRepository cycles;
    private final ModelExchangeRepository exchanges;
    private final OrchestratorClient orchestrator;
    private final Clock clock;

    public GoalRunExecutor(
            GoalRunRepository goalRuns,
            OrchestrationCycleRepository cycles,
            ModelExchangeRepository exchanges,
            OrchestratorClient orchestrator) {
        this.goalRuns = goalRuns;
        this.cycles = cycles;
        this.exchanges = exchanges;
        this.orchestrator = orchestrator;
        this.clock = Clock.systemUTC();
    }

    @Async
    public void execute(UUID goalRunId) {
        GoalRun run = goalRuns.findById(goalRunId).orElseThrow();
        Instant startedAt = clock.instant();
        OrchestrationCycle cycle = cycles.save(OrchestrationCycle.start(goalRunId, startedAt));
        String prompt = orchestrator.buildPrompt(run.goal());
        ModelExchange exchange = exchanges.save(ModelExchange.start(
                cycle.id(),
                prompt,
                startedAt));
        try {
            OrchestratorCall call = orchestrator.decide(prompt);
            Instant completedAt = clock.instant();
            exchanges.save(exchange.finish(
                    call.rawResponse(),
                    call.providerRequestId(),
                    call.promptTokens(),
                    call.completionTokens(),
                    completedAt));
            cycles.save(cycle.finish(completedAt));
            goalRuns.save(run.complete(call.decision().outcome(), call.decision().answer(), completedAt));
        } catch (RuntimeException exception) {
            logger.error("Goal Run {} failed during Orchestrator execution", goalRunId, exception);
            goalRuns.save(run.fail("O Orquestrador não conseguiu concluir esta execução.", clock.instant()));
        }
    }
}
