package com.bondtradex.ioi.controller;

import com.bondtradex.ioi.dto.ApiResponse;
import com.bondtradex.ioi.dto.TestValidationRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ValidationTestController {

    @PostMapping("/api/auth/test-validation")
    public ResponseEntity<ApiResponse<Map<String,String>>> testValidaton(@RequestBody @Valid TestValidationRequest request){
        Map<String,String> map = Map.of(
                "username",request.username(),
                "email",request.email()
        );
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("Validation passed",map));
    }
}
