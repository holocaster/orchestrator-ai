package com.holocaster.orchestrator.execution;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

interface OrchestrationCycleRepository extends CrudRepository<OrchestrationCycle, UUID> {
}
