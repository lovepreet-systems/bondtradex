package com.bondtradex.ioi.controller;

import com.bondtradex.ioi.dto.ApiResponse;
import com.bondtradex.ioi.dto.CreateIoiRequest;
import com.bondtradex.ioi.dto.IoiResponse;
import com.bondtradex.ioi.service.IoiService;
import jakarta.validation.Valid;
import com.bondtradex.ioi.dto.UpdateIoiRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import com.bondtradex.ioi.dto.SubmitIoiRequest;
import com.bondtradex.ioi.dto.CompleteSalesReviewRequest;
import com.bondtradex.ioi.dto.StartSalesReviewRequest;
import com.bondtradex.ioi.dto.ApproveIoiRequest;
import com.bondtradex.ioi.dto.RejectIoiRequest;
import com.bondtradex.ioi.dto.PagedResponse;
import com.bondtradex.ioi.entity.IoiStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import com.bondtradex.ioi.dto.CancelIoiRequest;
import com.bondtradex.ioi.dto.CreateOfferingRequest;
import java.util.UUID;

@Validated
@RestController
@RequestMapping("/api/v1/iois")
public class IoiController {

    private final IoiService ioiService;

    public IoiController(IoiService ioiService) {
        this.ioiService = ioiService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('IOI_CREATE')")
    public ResponseEntity<ApiResponse<IoiResponse>> createIoi(
            @Valid @RequestBody CreateIoiRequest request,
            Authentication authentication) {
        IoiResponse response = ioiService.create(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(
                        ApiResponse.success(
                                "IOI created successfully",
                                response
                        )
                );
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('IOI_UPDATE')")
    public ResponseEntity<ApiResponse<IoiResponse>> updateDraftIoi(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateIoiRequest request,
            Authentication authentication
    ) {
        IoiResponse response = ioiService.updateDraft(
                id,
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "IOI updated successfully",
                        response
                )
        );
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAuthority('IOI_SUBMIT')")
    public ResponseEntity<ApiResponse<IoiResponse>> submitIoi(
            @PathVariable UUID id,
            @Valid @RequestBody SubmitIoiRequest request,
            Authentication authentication
    ) {
        IoiResponse response = ioiService.submit(
                id,
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "IOI submitted successfully",
                        response
                )
        );
    }

    @PostMapping("/{id}/sales-review/start")
    @PreAuthorize("hasAuthority('IOI_SALES_REVIEW')")
    public ResponseEntity<ApiResponse<IoiResponse>> startSalesReview(
            @PathVariable UUID id,
            @RequestParam UUID salesUserId,
            @Valid @RequestBody StartSalesReviewRequest request,
            Authentication authentication
    ) {
        IoiResponse response = ioiService.startSalesReview(
                id,
                salesUserId,
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Sales review started successfully",
                        response
                )
        );
    }

    @PostMapping("/{id}/sales-review/complete")
    @PreAuthorize("hasAuthority('IOI_SALES_REVIEW')")
    public ResponseEntity<ApiResponse<IoiResponse>> completeSalesReview(
            @PathVariable UUID id,
            @Valid @RequestBody CompleteSalesReviewRequest request,
            Authentication authentication
    ) {
        IoiResponse response = ioiService.completeSalesReview(
                id,
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Sales review completed successfully",
                        response
                )
        );
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('IOI_APPROVE')")
    public ResponseEntity<ApiResponse<IoiResponse>> approveIoi(
            @PathVariable UUID id,
            @Valid @RequestBody ApproveIoiRequest request,
            Authentication authentication
    ) {
        IoiResponse response = ioiService.approve(
                id,
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "IOI approved successfully",
                        response
                )
        );
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('IOI_REJECT')")
    public ResponseEntity<ApiResponse<IoiResponse>> rejectIoi(
            @PathVariable UUID id,
            @Valid @RequestBody RejectIoiRequest request,
            Authentication authentication
    ) {
        IoiResponse response = ioiService.reject(
                id,
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "IOI rejected successfully",
                        response
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyAuthority('IOI_CREATE', 'IOI_UPDATE', 'IOI_SUBMIT', " +
                    "'IOI_SALES_REVIEW', 'IOI_APPROVE', 'IOI_REJECT')"
    )
    public ResponseEntity<ApiResponse<IoiResponse>> getIoiById(
            @PathVariable UUID id
    ) {
        IoiResponse response = ioiService.getById(id);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "IOI retrieved successfully",
                        response
                )
        );
    }

    @GetMapping
    @PreAuthorize(
            "hasAnyAuthority('IOI_CREATE', 'IOI_UPDATE', 'IOI_SUBMIT', " +
                    "'IOI_SALES_REVIEW', 'IOI_APPROVE', 'IOI_REJECT')"
    )
    public ResponseEntity<ApiResponse<PagedResponse<IoiResponse>>> getIoIs(
            @RequestParam(required = false) IoiStatus status,

            @RequestParam(defaultValue = "0")
            @Min(value = 0, message = "Page cannot be negative")
            int page,

            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "Size must be at least 1")
            @Max(value = 100, message = "Size cannot exceed 100")
            int size
    ) {
        PagedResponse<IoiResponse> response =
                ioiService.findAll(status, page, size);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "IOIs retrieved successfully",
                        response
                )
        );
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('IOI_CANCEL')")
    public ResponseEntity<ApiResponse<IoiResponse>> cancelIoi(
            @PathVariable UUID id,
            @Valid @RequestBody CancelIoiRequest request,
            Authentication authentication
    ) {
        IoiResponse response = ioiService.cancel(
                id,
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "IOI cancelled successfully",
                        response
                )
        );
    }

    @PostMapping("/{id}/offering-created")
    @PreAuthorize("hasAuthority('IOI_OFFERING_CREATE')")
    public ResponseEntity<ApiResponse<IoiResponse>> markOfferingCreated(
            @PathVariable UUID id,
            @Valid @RequestBody CreateOfferingRequest request,
            Authentication authentication
    ) {
        IoiResponse response = ioiService.markOfferingCreated(
                id,
                request,
                authentication.getName()
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "IOI marked as offering created successfully",
                        response
                )
        );
    }
}