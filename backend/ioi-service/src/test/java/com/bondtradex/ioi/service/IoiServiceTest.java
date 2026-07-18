package com.bondtradex.ioi.service;

import com.bondtradex.ioi.dto.ApproveIoiRequest;
import com.bondtradex.ioi.dto.CancelIoiRequest;
import com.bondtradex.ioi.dto.CompleteSalesReviewRequest;
import com.bondtradex.ioi.dto.CreateIoiRequest;
import com.bondtradex.ioi.dto.CreateOfferingRequest;
import com.bondtradex.ioi.dto.IoiResponse;
import com.bondtradex.ioi.dto.PagedResponse;
import com.bondtradex.ioi.dto.RejectIoiRequest;
import com.bondtradex.ioi.dto.StartSalesReviewRequest;
import com.bondtradex.ioi.dto.SubmitIoiRequest;
import com.bondtradex.ioi.dto.UpdateIoiRequest;
import com.bondtradex.ioi.entity.Ioi;
import com.bondtradex.ioi.entity.IoiSide;
import com.bondtradex.ioi.entity.IoiStatus;
import com.bondtradex.ioi.exception.ResourceNotFoundException;
import com.bondtradex.ioi.repository.IoiRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IoiServiceTest {

    private static final UUID IOI_ID =
            UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");

    private static final UUID CLIENT_ID =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private static final UUID INSTRUMENT_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID SALES_USER_ID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID TRADER_USER_ID =
            UUID.fromString("44444444-4444-4444-4444-444444444444");

    private static final UUID OFFERING_ID =
            UUID.fromString("55555555-5555-5555-5555-555555555555");

    private static final Instant CREATED_AT =
            Instant.parse("2026-07-17T10:00:00Z");

    private static final long CURRENT_VERSION = 1L;

    @Mock
    private IoiRepository ioiRepository;

    @InjectMocks
    private IoiService ioiService;

    /*
     * ------------------------------------------------------
     * create()
     * ------------------------------------------------------
     */

    @Test
    void create_shouldCreateAndReturnDraftIoi() {
        CreateIoiRequest request = createRequest();

        when(ioiRepository.saveAndFlush(any(Ioi.class)))
                .thenAnswer(invocation -> {
                    Ioi ioi = invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            ioi,
                            "version",
                            0L
                    );

                    return ioi;
                });

        IoiResponse response = ioiService.create(
                request,
                "client-user"
        );

        ArgumentCaptor<Ioi> captor =
                ArgumentCaptor.forClass(Ioi.class);

        verify(ioiRepository).saveAndFlush(captor.capture());

        Ioi savedIoi = captor.getValue();

        assertNotNull(savedIoi.getId());
        assertNotNull(savedIoi.getIoiNumber());
        assertTrue(
                savedIoi.getIoiNumber()
                        .matches("^IOI-\\d{8}-[A-F0-9]{8}$")
        );

        assertEquals(CLIENT_ID, response.clientId());
        assertEquals(INSTRUMENT_ID, response.instrumentId());
        assertEquals("US1234567890", response.isin());
        assertEquals("AB1234567", response.cusip());
        assertEquals(IoiSide.BUY, response.side());
        assertEquals(
                new BigDecimal("1000000.0000"),
                response.quantity()
        );
        assertEquals(
                new BigDecimal("99.5000"),
                response.targetPrice()
        );
        assertEquals("CAD", response.currency());
        assertEquals(IoiStatus.DRAFT, response.status());
        assertEquals("client-user", response.createdBy());
        assertEquals(0L, response.version());
    }

    @Test
    void create_shouldNormalizeIsinCusipAndCurrency() {
        CreateIoiRequest request = new CreateIoiRequest(
                CLIENT_ID,
                INSTRUMENT_ID,
                " us1234567890 ",
                " ab1234567 ",
                IoiSide.BUY,
                new BigDecimal("1000000.0000"),
                new BigDecimal("99.5000"),
                "cad",
                LocalDate.of(2026, 7, 20),
                "Initial request"
        );

        when(ioiRepository.saveAndFlush(any(Ioi.class)))
                .thenAnswer(invocation -> {
                    Ioi ioi = invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            ioi,
                            "version",
                            0L
                    );

                    return ioi;
                });

        IoiResponse response = ioiService.create(
                request,
                "client-user"
        );

        assertEquals("US1234567890", response.isin());
        assertEquals("AB1234567", response.cusip());
        assertEquals("CAD", response.currency());
    }

    @Test
    void create_shouldAllowNullIsinAndCusip() {
        CreateIoiRequest request = new CreateIoiRequest(
                CLIENT_ID,
                INSTRUMENT_ID,
                null,
                null,
                IoiSide.BUY,
                new BigDecimal("1000000.0000"),
                new BigDecimal("99.5000"),
                "CAD",
                LocalDate.of(2026, 7, 20),
                "Initial request"
        );

        when(ioiRepository.saveAndFlush(any(Ioi.class)))
                .thenAnswer(invocation -> {
                    Ioi ioi = invocation.getArgument(0);

                    ReflectionTestUtils.setField(
                            ioi,
                            "version",
                            0L
                    );

                    return ioi;
                });

        IoiResponse response = ioiService.create(
                request,
                "client-user"
        );

        assertEquals(null, response.isin());
        assertEquals(null, response.cusip());
    }

    /*
     * ------------------------------------------------------
     * getById()
     * ------------------------------------------------------
     */

    @Test
    void getById_shouldReturnIoiWhenItExists() {
        Ioi ioi = createDraftIoi();

        when(ioiRepository.findById(IOI_ID))
                .thenReturn(Optional.of(ioi));

        IoiResponse response = ioiService.getById(IOI_ID);

        assertEquals(IOI_ID, response.id());
        assertEquals("IOI-20260717-ABC12345", response.ioiNumber());
        assertEquals(IoiStatus.DRAFT, response.status());
        assertEquals(CURRENT_VERSION, response.version());

        verify(ioiRepository).findById(IOI_ID);
    }

    @Test
    void getById_shouldThrowWhenIoiDoesNotExist() {
        when(ioiRepository.findById(IOI_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ioiService.getById(IOI_ID)
        );

        assertEquals(
                "IOI not found with ID: " + IOI_ID,
                exception.getMessage()
        );

        verify(ioiRepository).findById(IOI_ID);
    }

    /*
     * ------------------------------------------------------
     * updateDraft()
     * ------------------------------------------------------
     */

    @Test
    void updateDraft_shouldUpdateEntityAndFlushChanges() {
        Ioi ioi = createDraftIoi();
        UpdateIoiRequest request = mock(UpdateIoiRequest.class);

        UUID newClientId = UUID.randomUUID();
        UUID newInstrumentId = UUID.randomUUID();

        when(request.version()).thenReturn(CURRENT_VERSION);
        when(request.clientId()).thenReturn(newClientId);
        when(request.instrumentId()).thenReturn(newInstrumentId);
        when(request.isin()).thenReturn(" ca1234567890 ");
        when(request.cusip()).thenReturn(" cd1234567 ");
        when(request.side()).thenReturn(IoiSide.SELL);
        when(request.quantity())
                .thenReturn(new BigDecimal("2000000.0000"));
        when(request.targetPrice())
                .thenReturn(new BigDecimal("98.2500"));
        when(request.currency()).thenReturn(" usd ");
        when(request.settlementDate())
                .thenReturn(LocalDate.of(2026, 7, 25));
        when(request.clientComment())
                .thenReturn("Updated request");

        when(ioiRepository.findById(IOI_ID))
                .thenReturn(Optional.of(ioi));

        IoiResponse response = ioiService.updateDraft(
                IOI_ID,
                request,
                "updated-client-user"
        );

        assertEquals(newClientId, response.clientId());
        assertEquals(newInstrumentId, response.instrumentId());
        assertEquals("CA1234567890", response.isin());
        assertEquals("CD1234567", response.cusip());
        assertEquals(IoiSide.SELL, response.side());
        assertEquals("USD", response.currency());
        assertEquals(IoiStatus.DRAFT, response.status());

        verify(ioiRepository).findById(IOI_ID);
        verify(ioiRepository).flush();
    }

    /*
     * ------------------------------------------------------
     * submit()
     * ------------------------------------------------------
     */

    @Test
    void submit_shouldTransitionIoiToSubmittedAndFlush() {
        Ioi ioi = createDraftIoi();
        SubmitIoiRequest request = mock(SubmitIoiRequest.class);

        when(request.version()).thenReturn(CURRENT_VERSION);
        when(ioiRepository.findById(IOI_ID))
                .thenReturn(Optional.of(ioi));

        IoiResponse response = ioiService.submit(
                IOI_ID,
                request,
                "client-user"
        );

        assertEquals(IoiStatus.SUBMITTED, response.status());
        assertNotNull(response.submittedAt());

        verify(ioiRepository).findById(IOI_ID);
        verify(ioiRepository).flush();
    }

    /*
     * ------------------------------------------------------
     * startSalesReview()
     * ------------------------------------------------------
     */

    @Test
    void startSalesReview_shouldTransitionToSalesReviewPending() {
        Ioi ioi = createSubmittedIoi();
        StartSalesReviewRequest request =
                mock(StartSalesReviewRequest.class);

        when(request.version()).thenReturn(CURRENT_VERSION);
        when(ioiRepository.findById(IOI_ID))
                .thenReturn(Optional.of(ioi));

        IoiResponse response = ioiService.startSalesReview(
                IOI_ID,
                SALES_USER_ID,
                request,
                "sales-user"
        );

        assertEquals(
                IoiStatus.SALES_REVIEW_PENDING,
                response.status()
        );
        assertEquals(SALES_USER_ID, response.salesUserId());

        verify(ioiRepository).flush();
    }

    /*
     * ------------------------------------------------------
     * completeSalesReview()
     * ------------------------------------------------------
     */

    @Test
    void completeSalesReview_shouldTransitionToTraderReviewPending() {
        Ioi ioi = createSalesReviewPendingIoi();

        CompleteSalesReviewRequest request =
                mock(CompleteSalesReviewRequest.class);

        when(request.version()).thenReturn(CURRENT_VERSION);
        when(request.salesComment())
                .thenReturn("Client eligibility verified");

        when(ioiRepository.findById(IOI_ID))
                .thenReturn(Optional.of(ioi));

        IoiResponse response = ioiService.completeSalesReview(
                IOI_ID,
                request,
                "sales-user"
        );

        assertEquals(
                IoiStatus.TRADER_REVIEW_PENDING,
                response.status()
        );
        assertEquals(
                "Client eligibility verified",
                response.salesComment()
        );
        assertNotNull(response.salesReviewedAt());

        verify(ioiRepository).flush();
    }

    /*
     * ------------------------------------------------------
     * approve()
     * ------------------------------------------------------
     */

    @Test
    void approve_shouldTransitionIoiToApproved() {
        Ioi ioi = createTraderReviewPendingIoi();
        ApproveIoiRequest request = mock(ApproveIoiRequest.class);

        when(request.version()).thenReturn(CURRENT_VERSION);
        when(request.traderUserId()).thenReturn(TRADER_USER_ID);
        when(request.traderComment())
                .thenReturn("Approved based on liquidity");

        when(ioiRepository.findById(IOI_ID))
                .thenReturn(Optional.of(ioi));

        IoiResponse response = ioiService.approve(
                IOI_ID,
                request,
                "trader-user"
        );

        assertEquals(IoiStatus.APPROVED, response.status());
        assertEquals(TRADER_USER_ID, response.traderUserId());
        assertEquals(
                "Approved based on liquidity",
                response.traderComment()
        );
        assertNotNull(response.traderReviewedAt());
        assertNotNull(response.approvedAt());

        verify(ioiRepository).flush();
    }

    /*
     * ------------------------------------------------------
     * reject()
     * ------------------------------------------------------
     */

    @Test
    void reject_shouldTransitionIoiToRejected() {
        Ioi ioi = createTraderReviewPendingIoi();
        RejectIoiRequest request = mock(RejectIoiRequest.class);

        when(request.version()).thenReturn(CURRENT_VERSION);
        when(request.traderUserId()).thenReturn(TRADER_USER_ID);
        when(request.traderComment())
                .thenReturn("Reviewed by trader");
        when(request.rejectionReason())
                .thenReturn("Insufficient liquidity");

        when(ioiRepository.findById(IOI_ID))
                .thenReturn(Optional.of(ioi));

        IoiResponse response = ioiService.reject(
                IOI_ID,
                request,
                "trader-user"
        );

        assertEquals(IoiStatus.REJECTED, response.status());
        assertEquals(TRADER_USER_ID, response.traderUserId());
        assertEquals(
                "Insufficient liquidity",
                response.rejectionReason()
        );
        assertNotNull(response.traderReviewedAt());

        verify(ioiRepository).flush();
    }

    /*
     * ------------------------------------------------------
     * cancel()
     * ------------------------------------------------------
     */

    @Test
    void cancel_shouldTransitionIoiToCancelled() {
        Ioi ioi = createDraftIoi();
        CancelIoiRequest request = mock(CancelIoiRequest.class);

        when(request.version()).thenReturn(CURRENT_VERSION);
        when(request.reason())
                .thenReturn("Client withdrew request");

        when(ioiRepository.findById(IOI_ID))
                .thenReturn(Optional.of(ioi));

        IoiResponse response = ioiService.cancel(
                IOI_ID,
                request,
                "client-user"
        );

        assertEquals(IoiStatus.CANCELLED, response.status());
        assertEquals(
                "Client withdrew request",
                response.rejectionReason()
        );

        verify(ioiRepository).flush();
    }

    /*
     * ------------------------------------------------------
     * markOfferingCreated()
     * ------------------------------------------------------
     */

    @Test
    void markOfferingCreated_shouldTransitionToOfferingCreated() {
        Ioi ioi = createApprovedIoi();
        CreateOfferingRequest request =
                mock(CreateOfferingRequest.class);

        when(request.version()).thenReturn(CURRENT_VERSION);
        when(request.offeringId()).thenReturn(OFFERING_ID);

        when(ioiRepository.findById(IOI_ID))
                .thenReturn(Optional.of(ioi));

        IoiResponse response = ioiService.markOfferingCreated(
                IOI_ID,
                request,
                "offering-service"
        );

        assertEquals(
                IoiStatus.OFFERING_CREATED,
                response.status()
        );
        assertEquals(OFFERING_ID, response.offeringId());
        assertNotNull(response.offeringCreatedAt());

        verify(ioiRepository).flush();
    }

    /*
     * ------------------------------------------------------
     * Shared command failure tests
     * ------------------------------------------------------
     */

    @Test
    void submit_shouldThrowWhenIoiDoesNotExist() {
        SubmitIoiRequest request = mock(SubmitIoiRequest.class);

        when(request.version()).thenReturn(CURRENT_VERSION);
        when(ioiRepository.findById(IOI_ID))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> ioiService.submit(
                        IOI_ID,
                        request,
                        "client-user"
                )
        );

        assertEquals(
                "IOI not found with ID: " + IOI_ID,
                exception.getMessage()
        );

        verify(ioiRepository, never()).flush();
    }

    @Test
    void submit_shouldThrowWhenRequestVersionIsStale() {
        Ioi ioi = createDraftIoi();
        SubmitIoiRequest request = mock(SubmitIoiRequest.class);

        when(request.version()).thenReturn(0L);
        when(ioiRepository.findById(IOI_ID))
                .thenReturn(Optional.of(ioi));

        assertThrows(
                ObjectOptimisticLockingFailureException.class,
                () -> ioiService.submit(
                        IOI_ID,
                        request,
                        "client-user"
                )
        );

        assertEquals(IoiStatus.DRAFT, ioi.getStatus());

        verify(ioiRepository, never()).flush();
    }

    /*
     * ------------------------------------------------------
     * findAll()
     * ------------------------------------------------------
     */

    @Test
    void findAll_shouldReturnAllIoIsWhenStatusIsNull() {
        Ioi firstIoi = createDraftIoi();
        Ioi secondIoi = createSubmittedIoi();

        Page<Ioi> page = new PageImpl<>(
                List.of(firstIoi, secondIoi),
                Pageable.ofSize(10),
                2
        );

        when(ioiRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        PagedResponse<IoiResponse> response =
                ioiService.findAll(
                        null,
                        0,
                        10
                );

        assertEquals(2, response.content().size());
        assertEquals(0, response.page());
        assertEquals(10, response.size());
        assertEquals(2, response.totalElements());
        assertEquals(1, response.totalPages());
        assertTrue(response.first());
        assertTrue(response.last());

        verify(ioiRepository).findAll(any(Pageable.class));
        verify(
                ioiRepository,
                never()
        ).findByStatus(
                any(IoiStatus.class),
                any(Pageable.class)
        );
    }

    @Test
    void findAll_shouldFilterByStatusWhenStatusIsProvided() {
        Ioi approvedIoi = createApprovedIoi();

        Page<Ioi> page = new PageImpl<>(
                List.of(approvedIoi),
                Pageable.ofSize(5),
                1
        );

        when(
                ioiRepository.findByStatus(
                        eq(IoiStatus.APPROVED),
                        any(Pageable.class)
                )
        ).thenReturn(page);

        PagedResponse<IoiResponse> response =
                ioiService.findAll(
                        IoiStatus.APPROVED,
                        0,
                        5
                );

        assertEquals(1, response.content().size());
        assertEquals(
                IoiStatus.APPROVED,
                response.content().getFirst().status()
        );
        assertEquals(1, response.totalElements());

        verify(
                ioiRepository
        ).findByStatus(
                eq(IoiStatus.APPROVED),
                any(Pageable.class)
        );

        verify(
                ioiRepository,
                never()
        ).findAll(any(Pageable.class));
    }

    @Test
    void findAll_shouldUseDescendingCreatedAtSort() {
        Page<Ioi> page = new PageImpl<>(List.of());

        when(ioiRepository.findAll(any(Pageable.class)))
                .thenReturn(page);

        ioiService.findAll(
                null,
                2,
                20
        );

        ArgumentCaptor<Pageable> captor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(ioiRepository).findAll(captor.capture());

        Pageable pageable = captor.getValue();
        Sort.Order createdAtOrder =
                pageable.getSort().getOrderFor("createdAt");

        assertEquals(2, pageable.getPageNumber());
        assertEquals(20, pageable.getPageSize());
        assertNotNull(createdAtOrder);
        assertEquals(
                Sort.Direction.DESC,
                createdAtOrder.getDirection()
        );
    }

    @Test
    void findAll_shouldReturnEmptyContentWhenNoIoIsExist() {
        Page<Ioi> emptyPage = new PageImpl<>(
                List.of(),
                Pageable.ofSize(10),
                0
        );

        when(ioiRepository.findAll(any(Pageable.class)))
                .thenReturn(emptyPage);

        PagedResponse<IoiResponse> response =
                ioiService.findAll(
                        null,
                        0,
                        10
                );

        assertTrue(response.content().isEmpty());
        assertEquals(0, response.totalElements());
        assertEquals(0, response.totalPages());
        assertTrue(response.first());
        assertTrue(response.last());
    }

    /*
     * ------------------------------------------------------
     * Test-data helpers
     * ------------------------------------------------------
     */

    private CreateIoiRequest createRequest() {
        return new CreateIoiRequest(
                CLIENT_ID,
                INSTRUMENT_ID,
                "US1234567890",
                "AB1234567",
                IoiSide.BUY,
                new BigDecimal("1000000.0000"),
                new BigDecimal("99.5000"),
                "CAD",
                LocalDate.of(2026, 7, 20),
                "Initial client request"
        );
    }

    private Ioi createDraftIoi() {
        Ioi ioi = new Ioi(
                IOI_ID,
                "IOI-20260717-ABC12345",
                CLIENT_ID,
                INSTRUMENT_ID,
                "US1234567890",
                "AB1234567",
                IoiSide.BUY,
                new BigDecimal("1000000.0000"),
                new BigDecimal("99.5000"),
                "CAD",
                LocalDate.of(2026, 7, 20),
                IoiStatus.DRAFT,
                "Initial client request",
                CREATED_AT,
                "client-user",
                CREATED_AT,
                "client-user"
        );

        setVersion(ioi);

        return ioi;
    }

    private Ioi createSubmittedIoi() {
        Ioi ioi = createDraftIoi();

        ioi.submit(
                "client-user",
                CREATED_AT.plusSeconds(60)
        );

        return ioi;
    }

    private Ioi createSalesReviewPendingIoi() {
        Ioi ioi = createSubmittedIoi();

        ioi.startSalesReview(
                SALES_USER_ID,
                "sales-user",
                CREATED_AT.plusSeconds(120)
        );

        return ioi;
    }

    private Ioi createTraderReviewPendingIoi() {
        Ioi ioi = createSalesReviewPendingIoi();

        ioi.completeSalesReview(
                "Client eligibility verified",
                "sales-user",
                CREATED_AT.plusSeconds(180)
        );

        return ioi;
    }

    private Ioi createApprovedIoi() {
        Ioi ioi = createTraderReviewPendingIoi();

        ioi.approve(
                TRADER_USER_ID,
                "Approved by trader",
                "trader-user",
                CREATED_AT.plusSeconds(240)
        );

        return ioi;
    }

    private void setVersion(Ioi ioi) {
        ReflectionTestUtils.setField(
                ioi,
                "version",
                CURRENT_VERSION
        );
    }
}