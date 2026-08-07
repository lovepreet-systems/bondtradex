package com.bondtradex.ioi.downstream.controller;

import com.bondtradex.ioi.downstream.dto.ReplayRequest;
import com.bondtradex.ioi.downstream.dto.ReplayResponse;
import com.bondtradex.ioi.downstream.service.ReplayService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/kafka")
public class ReplayController {

    private final ReplayService replayService;

    @PostMapping(
            "/dead-letters/{deadLetterEventId}/replay"
    )
    public ReplayResponse replay(
            @PathVariable
            UUID deadLetterEventId,

            @Valid
            @RequestBody
            ReplayRequest request
    ) {

        return replayService.replay(
                deadLetterEventId,
                request.reason()
        );
    }
}