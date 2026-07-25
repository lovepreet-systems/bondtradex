package com.bondtradex.ioi.kafka.producer;

import com.bondtradex.ioi.kafka.event.IoiCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class IoiEventProducer {

    private static final String IOI_EVENTS_TOPIC = "ioi-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public IoiEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishIoiCreated(IoiCreatedEvent event) {
        kafkaTemplate.send(
                IOI_EVENTS_TOPIC,
                event.ioiId().toString(),
                event
        );
    }
}
