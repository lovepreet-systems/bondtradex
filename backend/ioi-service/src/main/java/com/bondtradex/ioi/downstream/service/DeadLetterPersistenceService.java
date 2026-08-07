package com.bondtradex.ioi.downstream.service;

import com.bondtradex.ioi.downstream.entity.DeadLetterEvent;
import com.bondtradex.ioi.downstream.model.DeadLetterEventMessage;
import com.bondtradex.ioi.downstream.repository.DeadLetterEventRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeadLetterPersistenceService {

    private final DeadLetterEventRepository repository;

    public void persist(DeadLetterEventMessage message
    ) {
        DeadLetterEvent entity = DeadLetterEvent.from(message);
        repository.save(entity);
    }
}