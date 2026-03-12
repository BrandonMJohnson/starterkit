package net.mudpot.starterkit.apiservice;

import jakarta.inject.Singleton;
import net.mudpot.starterkit.commons.orchestration.system.model.HelloHistoryEntry;
import net.mudpot.starterkit.persistence.history.PromptRunRepository;

import java.util.List;

@Singleton
public class HelloHistoryQueryService {
    private final PromptRunRepository promptRunRepository;

    public HelloHistoryQueryService(final PromptRunRepository promptRunRepository) {
        this.promptRunRepository = promptRunRepository;
    }

    public List<HelloHistoryEntry> recent(final int limit) {
        return promptRunRepository.findRecent(limit).stream()
            .map(entity -> new HelloHistoryEntry(
                entity.getId(),
                entity.getWorkflowId(),
                entity.getUserName(),
                entity.getUseCase(),
                entity.getResponseText(),
                entity.getProvider(),
                entity.getModel(),
                entity.isCacheHit(),
                entity.getPromptTemplate(),
                entity.getCreatedAt()
            ))
            .toList();
    }
}
