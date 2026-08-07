package com.bondtradex.ioi.downstream.service;

import com.bondtradex.ioi.downstream.dto.ReplayResponse;
import com.bondtradex.ioi.downstream.entity.DeadLetterEvent;
import com.bondtradex.ioi.downstream.model.ReplayStatus;
import com.bondtradex.ioi.downstream.repository.DeadLetterEventRepository;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReplayService {

    private final DeadLetterEventRepository repository;

    private final KafkaTemplate<String, Object>
            kafkaTemplate;

    @Value("${app.kafka.topics.ioi-events-replay}")
    private String replayTopic;

    @Transactional
    public ReplayResponse replay(
            UUID deadLetterEventId,
            String reason
    ) {

        DeadLetterEvent event =
                repository
                        .findByDeadLetterEventId(
                                deadLetterEventId
                        )
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "DLT record not found."
                                        )
                        );

        if (event.getStatus()
                == ReplayStatus.REPLAYED) {

            throw new IllegalStateException(
                    "Event already replayed."
            );
        }

        event.markReplayRequested(reason);

        JsonNode payload =
                event.getOriginalPayload();

        kafkaTemplate.send(
                replayTopic,
                event.getOriginalEventId()
                        .toString(),
                payload
        );

        event.setReplayCount(
                event.getReplayCount() + 1
        );

        event.setLastReplayedAt(
                Instant.now()
        );

        return new ReplayResponse(
                event.getDeadLetterEventId(),
                event.getOriginalEventId(),
                event.getStatus().name(),
                event.getLastReplayedAt()
        );
    }
}