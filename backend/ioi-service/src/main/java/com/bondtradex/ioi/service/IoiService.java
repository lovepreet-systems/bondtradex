package com.bondtradex.ioi.service;

import com.bondtradex.ioi.dto.CreateIoiRequest;
import com.bondtradex.ioi.dto.IoiResponse;
import com.bondtradex.ioi.dto.PagedResponse;
import com.bondtradex.ioi.entity.Ioi;
import com.bondtradex.ioi.entity.IoiStatus;
import com.bondtradex.ioi.exception.ResourceNotFoundException;
import com.bondtradex.ioi.repository.IoiRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bondtradex.ioi.dto.UpdateIoiRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import com.bondtradex.ioi.dto.SubmitIoiRequest;
import com.bondtradex.ioi.dto.StartSalesReviewRequest;
import com.bondtradex.ioi.dto.CompleteSalesReviewRequest;
import com.bondtradex.ioi.dto.ApproveIoiRequest;
import com.bondtradex.ioi.dto.RejectIoiRequest;
import com.bondtradex.ioi.dto.CancelIoiRequest;
import com.bondtradex.ioi.dto.CreateOfferingRequest;
@Service
public class IoiService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private final IoiRepository ioiRepository;

    public IoiService(IoiRepository ioiRepository) {
        this.ioiRepository = ioiRepository;
    }

    @Transactional
    public IoiResponse create(
            CreateIoiRequest request,
            String authenticatedUsername
    ) {
        Instant now = Instant.now();

        Ioi ioi = new Ioi(
                UUID.randomUUID(),
                generateIoiNumber(),
                request.clientId(),
                request.instrumentId(),
                normalize(request.isin()),
                normalize(request.cusip()),
                request.side(),
                request.quantity(),
                request.targetPrice(),
                request.currency().toUpperCase(Locale.ROOT),
                request.settlementDate(),
                IoiStatus.DRAFT,
                request.clientComment(),
                now,
                authenticatedUsername,
                now,
                authenticatedUsername
        );

        Ioi savedIoi = ioiRepository.saveAndFlush(ioi);

        return toResponse(savedIoi);
    }

    @Transactional(readOnly = true)
    public IoiResponse getById(UUID id) {
        Ioi ioi = ioiRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "IOI not found with ID: " + id
                        )
                );

        return toResponse(ioi);
    }

    @Transactional
    public IoiResponse approve(
            UUID id,
            ApproveIoiRequest request,
            String authenticatedUsername
    ) {
        Ioi ioi = getIoiForCommand(id, request.version());

        ioi.approve(
                request.traderUserId(),
                request.traderComment(),
                authenticatedUsername,
                Instant.now()
        );

        ioiRepository.flush();

        return toResponse(ioi);
    }

    @Transactional
    public IoiResponse markOfferingCreated(
            UUID id,
            CreateOfferingRequest request,
            String authenticatedUsername
    ) {
        Ioi ioi = getIoiForCommand(
                id,
                request.version()
        );

        ioi.markOfferingCreated(
                request.offeringId(),
                authenticatedUsername,
                Instant.now()
        );

        ioiRepository.flush();

        return toResponse(ioi);
    }

    @Transactional
    public IoiResponse reject(
            UUID id,
            RejectIoiRequest request,
            String authenticatedUsername
    ) {
        Ioi ioi = getIoiForCommand(id, request.version());

        ioi.reject(
                request.traderUserId(),
                request.traderComment(),
                request.rejectionReason(),
                authenticatedUsername,
                Instant.now()
        );

        ioiRepository.flush();

        return toResponse(ioi);
    }

    private Ioi getIoiForCommand(
            UUID id,
            Long requestVersion
    ) {
        Ioi ioi = ioiRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "IOI not found with ID: " + id
                        )
                );

        if (!ioi.getVersion().equals(requestVersion)) {
            throw new ObjectOptimisticLockingFailureException(
                    Ioi.class,
                    id
            );
        }

        return ioi;
    }

    @Transactional
    public IoiResponse updateDraft(
            UUID id,
            UpdateIoiRequest request,
            String authenticatedUsername
    ) {
        Ioi ioi = getIoiForCommand(
                id,
                request.version()
        );

        ioi.updateDraft(
                request,
                authenticatedUsername,
                Instant.now()
        );

        ioiRepository.flush();

        return toResponse(ioi);
    }

    @Transactional(readOnly = true)
    public PagedResponse<IoiResponse> findAll(
            IoiStatus status,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Direction.DESC,
                        "createdAt"
                )
        );

        Page<Ioi> ioiPage = status == null
                ? ioiRepository.findAll(pageable)
                : ioiRepository.findByStatus(status, pageable);

        return new PagedResponse<>(
                ioiPage.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                ioiPage.getNumber(),
                ioiPage.getSize(),
                ioiPage.getTotalElements(),
                ioiPage.getTotalPages(),
                ioiPage.isFirst(),
                ioiPage.isLast()
        );
    }

    private String generateIoiNumber() {
        String date = LocalDate.now(ZoneOffset.UTC)
                .format(DATE_FORMATTER);

        String suffix = UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase(Locale.ROOT);

        return "IOI-" + date + "-" + suffix;
    }

    private String normalize(String value) {
        return value == null
                ? null
                : value.trim().toUpperCase(Locale.ROOT);
    }

    private IoiResponse toResponse(Ioi ioi) {
        return new IoiResponse(
                ioi.getId(),
                ioi.getIoiNumber(),
                ioi.getClientId(),
                ioi.getInstrumentId(),
                ioi.getIsin(),
                ioi.getCusip(),
                ioi.getSide(),
                ioi.getQuantity(),
                ioi.getTargetPrice(),
                ioi.getCurrency(),
                ioi.getSettlementDate(),
                ioi.getStatus(),
                ioi.getClientComment(),
                ioi.getSalesUserId(),
                ioi.getSalesComment(),
                ioi.getTraderUserId(),
                ioi.getTraderComment(),
                ioi.getRejectionReason(),
                ioi.getOfferingId(),
                ioi.getSubmittedAt(),
                ioi.getSalesReviewedAt(),
                ioi.getTraderReviewedAt(),
                ioi.getApprovedAt(),
                ioi.getOfferingCreatedAt(),
                ioi.getCreatedAt(),
                ioi.getCreatedBy(),
                ioi.getVersion()
        );
    }

    @Transactional
    public IoiResponse submit(
            UUID id,
            SubmitIoiRequest request,
            String authenticatedUsername
    ) {
        Ioi ioi = getIoiForCommand(
                id,
                request.version()
        );

        ioi.submit(
                authenticatedUsername,
                Instant.now()
        );

        ioiRepository.flush();

        return toResponse(ioi);
    }

    @Transactional
    public IoiResponse startSalesReview(
            UUID id,
            UUID salesUserId,
            StartSalesReviewRequest request,
            String authenticatedUsername
    ) {
        Ioi ioi = getIoiForCommand(
                id,
                request.version()
        );

        ioi.startSalesReview(
                salesUserId,
                authenticatedUsername,
                Instant.now()
        );

        ioiRepository.flush();

        return toResponse(ioi);
    }

    @Transactional
    public IoiResponse completeSalesReview(
            UUID id,
            CompleteSalesReviewRequest request,
            String authenticatedUsername
    ) {
        Ioi ioi = getIoiForCommand(
                id,
                request.version()
        );

        ioi.completeSalesReview(
                request.salesComment(),
                authenticatedUsername,
                Instant.now()
        );

        ioiRepository.flush();

        return toResponse(ioi);
    }

    @Transactional
    public IoiResponse cancel(
            UUID id,
            CancelIoiRequest request,
            String authenticatedUsername
    ) {
        Ioi ioi = getIoiForCommand(id, request.version());

        ioi.cancel(
                request.reason(),
                authenticatedUsername,
                Instant.now()
        );

        ioiRepository.flush();

        return toResponse(ioi);
    }
}