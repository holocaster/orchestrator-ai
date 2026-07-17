package com.holocaster.orchestrator.llm;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

public interface ModelExchangeRepository extends CrudRepository<ModelExchange, UUID> {
}
