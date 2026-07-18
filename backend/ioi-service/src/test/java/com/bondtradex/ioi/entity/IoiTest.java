package com.bondtradex.ioi.entity;

import com.bondtradex.ioi.dto.UpdateIoiRequest;
import com.bondtradex.ioi.exception.InvalidIoiStateException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IoiTest {

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

    private static final Instant ACTION_TIME =
            Instant.parse("2026-07-17T11:00:00Z");

    /*
     * ------------------------------------------------------
     * Constructor tests
     * ------------------------------------------------------
     */

    @Test
    void constructor_shouldCreateIoiWithExpectedValues() {
        Ioi ioi = createDraftIoi();

        assertEquals(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
                ioi.getId()
        );
        assertEquals("IOI-20260717-ABC12345", ioi.getIoiNumber());
        assertEquals(CLIENT_ID, ioi.getClientId());
        assertEquals(INSTRUMENT_ID, ioi.getInstrumentId());
        assertEquals("US1234567890", ioi.getIsin());
        assertEquals("AB1234567", ioi.getCusip());
        assertEquals(IoiSide.BUY, ioi.getSide());
        assertEquals(new BigDecimal("1000000.0000"), ioi.getQuantity());
        assertEquals(new BigDecimal("99.5000"), ioi.getTargetPrice());
        assertEquals("CAD", ioi.getCurrency());
        assertEquals(LocalDate.of(2026, 7, 20), ioi.getSettlementDate());
        assertEquals(IoiStatus.DRAFT, ioi.getStatus());
        assertEquals("Initial client request", ioi.getClientComment());
        assertEquals(CREATED_AT, ioi.getCreatedAt());
        assertEquals("client-user", ioi.getCreatedBy());
        assertEquals(CREATED_AT, ioi.getUpdatedAt());
        assertEquals("client-user", ioi.getUpdatedBy());
    }

    @Test
    void constructor_shouldNormalizeIsinCusipAndCurrency() {
        Ioi ioi = new Ioi(
                UUID.randomUUID(),
                "IOI-20260717-ABC12345",
                CLIENT_ID,
                INSTRUMENT_ID,
                " us1234567890 ",
                " ab1234567 ",
                IoiSide.BUY,
                new BigDecimal("1000000.0000"),
                new BigDecimal("99.5000"),
                " cad ",
                LocalDate.of(2026, 7, 20),
                IoiStatus.DRAFT,
                "Initial client request",
                CREATED_AT,
                "client-user",
                CREATED_AT,
                "client-user"
        );

        assertEquals("US1234567890", ioi.getIsin());
        assertEquals("AB1234567", ioi.getCusip());
        assertEquals("CAD", ioi.getCurrency());
    }

    @Test
    void constructor_shouldAllowNullIsinAndCusip() {
        Ioi ioi = new Ioi(
                UUID.randomUUID(),
                "IOI-20260717-ABC12345",
                CLIENT_ID,
                INSTRUMENT_ID,
                null,
                null,
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

        assertNull(ioi.getIsin());
        assertNull(ioi.getCusip());
    }

    /*
     * ------------------------------------------------------
     * updateDraft() tests
     * ------------------------------------------------------
     */

    @Test
    void updateDraft_shouldUpdateAllEditableFields() {
        Ioi ioi = createDraftIoi();

        UUID newClientId = UUID.randomUUID();
        UUID newInstrumentId = UUID.randomUUID();
        LocalDate newSettlementDate = LocalDate.of(2026, 7, 25);

        UpdateIoiRequest request = mock(UpdateIoiRequest.class);

        when(request.clientId()).thenReturn(newClientId);
        when(request.instrumentId()).thenReturn(newInstrumentId);
        when(request.isin()).thenReturn("CA1234567890");
        when(request.cusip()).thenReturn("CD1234567");
        when(request.side()).thenReturn(IoiSide.SELL);
        when(request.quantity()).thenReturn(new BigDecimal("2000000.0000"));
        when(request.targetPrice()).thenReturn(new BigDecimal("98.2500"));
        when(request.currency()).thenReturn("USD");
        when(request.settlementDate()).thenReturn(newSettlementDate);
        when(request.clientComment()).thenReturn("Updated client request");

        ioi.updateDraft(
                request,
                "updated-client-user",
                ACTION_TIME
        );

        assertEquals(newClientId, ioi.getClientId());
        assertEquals(newInstrumentId, ioi.getInstrumentId());
        assertEquals("CA1234567890", ioi.getIsin());
        assertEquals("CD1234567", ioi.getCusip());
        assertEquals(IoiSide.SELL, ioi.getSide());
        assertEquals(new BigDecimal("2000000.0000"), ioi.getQuantity());
        assertEquals(new BigDecimal("98.2500"), ioi.getTargetPrice());
        assertEquals("USD", ioi.getCurrency());
        assertEquals(newSettlementDate, ioi.getSettlementDate());
        assertEquals("Updated client request", ioi.getClientComment());
        assertEquals(IoiStatus.DRAFT, ioi.getStatus());
        assertEquals(ACTION_TIME, ioi.getUpdatedAt());
        assertEquals("updated-client-user", ioi.getUpdatedBy());
    }

    @Test
    void updateDraft_shouldNormalizeIdentifiersAndCurrency() {
        Ioi ioi = createDraftIoi();

        UpdateIoiRequest request = mock(UpdateIoiRequest.class);

        when(request.clientId()).thenReturn(CLIENT_ID);
        when(request.instrumentId()).thenReturn(INSTRUMENT_ID);
        when(request.isin()).thenReturn(" ca1234567890 ");
        when(request.cusip()).thenReturn(" cd1234567 ");
        when(request.side()).thenReturn(IoiSide.SELL);
        when(request.quantity()).thenReturn(new BigDecimal("2000000.0000"));
        when(request.targetPrice()).thenReturn(new BigDecimal("98.2500"));
        when(request.currency()).thenReturn(" usd ");
        when(request.settlementDate())
                .thenReturn(LocalDate.of(2026, 7, 25));
        when(request.clientComment()).thenReturn("Updated request");

        ioi.updateDraft(
                request,
                "updated-client-user",
                ACTION_TIME
        );

        assertEquals("CA1234567890", ioi.getIsin());
        assertEquals("CD1234567", ioi.getCusip());
        assertEquals("USD", ioi.getCurrency());
    }

    @Test
    void updateDraft_shouldThrowWhenStatusIsNotDraft() {
        Ioi ioi = createDraftIoi();

        ioi.submit("client-user", ACTION_TIME);

        UpdateIoiRequest request = mock(UpdateIoiRequest.class);

        InvalidIoiStateException exception = assertThrows(
                InvalidIoiStateException.class,
                () -> ioi.updateDraft(
                        request,
                        "client-user",
                        ACTION_TIME
                )
        );

        assertEquals(IoiStatus.SUBMITTED, ioi.getStatus());
        assertEquals(
                "Operation is allowed only for DRAFT IOIs. " +
                        "Current status: SUBMITTED",
                exception.getMessage()
        );
    }

    /*
     * ------------------------------------------------------
     * submit() tests
     * ------------------------------------------------------
     */

    @Test
    void submit_shouldTransitionDraftToSubmitted() {
        Ioi ioi = createDraftIoi();

        ioi.submit("client-user", ACTION_TIME);

        assertEquals(IoiStatus.SUBMITTED, ioi.getStatus());
        assertEquals(ACTION_TIME, ioi.getSubmittedAt());
        assertEquals(ACTION_TIME, ioi.getUpdatedAt());
        assertEquals("client-user", ioi.getUpdatedBy());
    }

    @Test
    void submit_shouldThrowWhenStatusIsNotDraft() {
        Ioi ioi = createDraftIoi();

        ioi.submit("client-user", ACTION_TIME);

        InvalidIoiStateException exception = assertThrows(
                InvalidIoiStateException.class,
                () -> ioi.submit(
                        "client-user",
                        ACTION_TIME.plusSeconds(60)
                )
        );

        assertEquals(IoiStatus.SUBMITTED, ioi.getStatus());
        assertEquals(
                "Operation is allowed only for DRAFT IOIs. " +
                        "Current status: SUBMITTED",
                exception.getMessage()
        );
    }

    /*
     * ------------------------------------------------------
     * startSalesReview() tests
     * ------------------------------------------------------
     */

    @Test
    void startSalesReview_shouldTransitionSubmittedToSalesReviewPending() {
        Ioi ioi = createSubmittedIoi();

        ioi.startSalesReview(
                SALES_USER_ID,
                "sales-user",
                ACTION_TIME
        );

        assertEquals(IoiStatus.SALES_REVIEW_PENDING, ioi.getStatus());
        assertEquals(SALES_USER_ID, ioi.getSalesUserId());
        assertEquals(ACTION_TIME, ioi.getUpdatedAt());
        assertEquals("sales-user", ioi.getUpdatedBy());
    }

    @Test
    void startSalesReview_shouldThrowWhenStatusIsNotSubmitted() {
        Ioi ioi = createDraftIoi();

        InvalidIoiStateException exception = assertThrows(
                InvalidIoiStateException.class,
                () -> ioi.startSalesReview(
                        SALES_USER_ID,
                        "sales-user",
                        ACTION_TIME
                )
        );

        assertEquals(IoiStatus.DRAFT, ioi.getStatus());
        assertEquals(
                "Sales review can start only for SUBMITTED IOIs. " +
                        "Current status: DRAFT",
                exception.getMessage()
        );
    }

    /*
     * ------------------------------------------------------
     * completeSalesReview() tests
     * ------------------------------------------------------
     */

    @Test
    void completeSalesReview_shouldTransitionToTraderReviewPending() {
        Ioi ioi = createSalesReviewPendingIoi();

        ioi.completeSalesReview(
                "Client eligibility verified",
                "sales-user",
                ACTION_TIME
        );

        assertEquals(IoiStatus.TRADER_REVIEW_PENDING, ioi.getStatus());
        assertEquals(
                "Client eligibility verified",
                ioi.getSalesComment()
        );
        assertEquals(ACTION_TIME, ioi.getSalesReviewedAt());
        assertEquals(ACTION_TIME, ioi.getUpdatedAt());
        assertEquals("sales-user", ioi.getUpdatedBy());
    }

    @Test
    void completeSalesReview_shouldThrowWhenStatusIsInvalid() {
        Ioi ioi = createSubmittedIoi();

        InvalidIoiStateException exception = assertThrows(
                InvalidIoiStateException.class,
                () -> ioi.completeSalesReview(
                        "Client eligibility verified",
                        "sales-user",
                        ACTION_TIME
                )
        );

        assertEquals(IoiStatus.SUBMITTED, ioi.getStatus());
        assertEquals(
                "Sales review can be completed only for " +
                        "SALES_REVIEW_PENDING IOIs. Current status: SUBMITTED",
                exception.getMessage()
        );
    }

    /*
     * ------------------------------------------------------
     * approve() tests
     * ------------------------------------------------------
     */

    @Test
    void approve_shouldTransitionTraderReviewPendingToApproved() {
        Ioi ioi = createTraderReviewPendingIoi();

        ioi.approve(
                TRADER_USER_ID,
                "Approved based on available liquidity",
                "trader-user",
                ACTION_TIME
        );

        assertEquals(IoiStatus.APPROVED, ioi.getStatus());
        assertEquals(TRADER_USER_ID, ioi.getTraderUserId());
        assertEquals(
                "Approved based on available liquidity",
                ioi.getTraderComment()
        );
        assertEquals(ACTION_TIME, ioi.getTraderReviewedAt());
        assertEquals(ACTION_TIME, ioi.getApprovedAt());
        assertEquals(ACTION_TIME, ioi.getUpdatedAt());
        assertEquals("trader-user", ioi.getUpdatedBy());
    }

    @Test
    void approve_shouldThrowWhenStatusIsNotTraderReviewPending() {
        Ioi ioi = createSubmittedIoi();

        InvalidIoiStateException exception = assertThrows(
                InvalidIoiStateException.class,
                () -> ioi.approve(
                        TRADER_USER_ID,
                        "Approved",
                        "trader-user",
                        ACTION_TIME
                )
        );

        assertEquals(IoiStatus.SUBMITTED, ioi.getStatus());
        assertEquals(
                "Trader decision is allowed only for " +
                        "TRADER_REVIEW_PENDING IOIs. Current status: SUBMITTED",
                exception.getMessage()
        );
    }

    /*
     * ------------------------------------------------------
     * reject() tests
     * ------------------------------------------------------
     */

    @Test
    void reject_shouldTransitionTraderReviewPendingToRejected() {
        Ioi ioi = createTraderReviewPendingIoi();

        ioi.reject(
                TRADER_USER_ID,
                "Reviewed by trader",
                "Insufficient liquidity",
                "trader-user",
                ACTION_TIME
        );

        assertEquals(IoiStatus.REJECTED, ioi.getStatus());
        assertEquals(TRADER_USER_ID, ioi.getTraderUserId());
        assertEquals("Reviewed by trader", ioi.getTraderComment());
        assertEquals(
                "Insufficient liquidity",
                ioi.getRejectionReason()
        );
        assertEquals(ACTION_TIME, ioi.getTraderReviewedAt());
        assertNull(ioi.getApprovedAt());
        assertEquals(ACTION_TIME, ioi.getUpdatedAt());
        assertEquals("trader-user", ioi.getUpdatedBy());
    }

    @Test
    void reject_shouldThrowWhenStatusIsNotTraderReviewPending() {
        Ioi ioi = createDraftIoi();

        InvalidIoiStateException exception = assertThrows(
                InvalidIoiStateException.class,
                () -> ioi.reject(
                        TRADER_USER_ID,
                        "Reviewed by trader",
                        "Insufficient liquidity",
                        "trader-user",
                        ACTION_TIME
                )
        );

        assertEquals(IoiStatus.DRAFT, ioi.getStatus());
        assertEquals(
                "Trader decision is allowed only for " +
                        "TRADER_REVIEW_PENDING IOIs. Current status: DRAFT",
                exception.getMessage()
        );
    }

    /*
     * ------------------------------------------------------
     * cancel() tests
     * ------------------------------------------------------
     */

    @Test
    void cancel_shouldCancelDraftIoi() {
        Ioi ioi = createDraftIoi();

        ioi.cancel(
                "Client no longer interested",
                "client-user",
                ACTION_TIME
        );

        assertEquals(IoiStatus.CANCELLED, ioi.getStatus());
        assertEquals(
                "Client no longer interested",
                ioi.getRejectionReason()
        );
        assertEquals(ACTION_TIME, ioi.getUpdatedAt());
        assertEquals("client-user", ioi.getUpdatedBy());
    }

    @Test
    void cancel_shouldCancelSubmittedIoi() {
        Ioi ioi = createSubmittedIoi();

        ioi.cancel(
                "Client withdrew the request",
                "client-user",
                ACTION_TIME
        );

        assertEquals(IoiStatus.CANCELLED, ioi.getStatus());
        assertEquals(
                "Client withdrew the request",
                ioi.getRejectionReason()
        );
        assertEquals(ACTION_TIME, ioi.getUpdatedAt());
        assertEquals("client-user", ioi.getUpdatedBy());
    }

    @Test
    void cancel_shouldThrowWhenStatusIsNotCancellable() {
        Ioi ioi = createApprovedIoi();

        InvalidIoiStateException exception = assertThrows(
                InvalidIoiStateException.class,
                () -> ioi.cancel(
                        "Client wants to cancel",
                        "client-user",
                        ACTION_TIME
                )
        );

        assertEquals(IoiStatus.APPROVED, ioi.getStatus());
        assertEquals(
                "Only DRAFT or SUBMITTED IOIs can be cancelled. " +
                        "Current status: APPROVED",
                exception.getMessage()
        );
    }

    /*
     * ------------------------------------------------------
     * markOfferingCreated() tests
     * ------------------------------------------------------
     */

    @Test
    void markOfferingCreated_shouldTransitionApprovedToOfferingCreated() {
        Ioi ioi = createApprovedIoi();

        ioi.markOfferingCreated(
                OFFERING_ID,
                "offering-service",
                ACTION_TIME
        );

        assertEquals(IoiStatus.OFFERING_CREATED, ioi.getStatus());
        assertEquals(OFFERING_ID, ioi.getOfferingId());
        assertEquals(ACTION_TIME, ioi.getOfferingCreatedAt());
        assertEquals(ACTION_TIME, ioi.getUpdatedAt());
        assertEquals("offering-service", ioi.getUpdatedBy());
    }

    @Test
    void markOfferingCreated_shouldThrowWhenStatusIsNotApproved() {
        Ioi ioi = createTraderReviewPendingIoi();

        InvalidIoiStateException exception = assertThrows(
                InvalidIoiStateException.class,
                () -> ioi.markOfferingCreated(
                        OFFERING_ID,
                        "offering-service",
                        ACTION_TIME
                )
        );

        assertEquals(
                IoiStatus.TRADER_REVIEW_PENDING,
                ioi.getStatus()
        );
        assertNull(ioi.getOfferingId());
        assertNull(ioi.getOfferingCreatedAt());
        assertEquals(
                "An offering can be created only for APPROVED IOIs. " +
                        "Current status: TRADER_REVIEW_PENDING",
                exception.getMessage()
        );
    }

    /*
     * ------------------------------------------------------
     * Test-data helper methods
     * ------------------------------------------------------
     */

    private Ioi createDraftIoi() {
        return new Ioi(
                UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"),
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
}