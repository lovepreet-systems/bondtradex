package com.bondtradex.auth.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HealthControllerTest {


    @Test
    void health_shouldReturnStatusUp(){
        HealthController controller = new HealthController();
        ResponseEntity<?> response = controller.health();
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }
}
