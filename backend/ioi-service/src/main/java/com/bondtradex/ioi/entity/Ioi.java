package com.bondtradex.ioi.entity;

import com.bondtradex.ioi.dto.UpdateIoiRequest;
import com.bondtradex.ioi.exception.InvalidIoiStateException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "iois")
public class Ioi {

    @Id
    private UUID id;

    @Column(
            name = "ioi_number",
            nullable = false,
            unique = true,
            length = 50
    )
    private String ioiNumber;

    @Column(
            name = "client_id",
            nullable = false
    )
    private UUID clientId;

    @Column(
            name = "instrument_id",
            nullable = false
    )
    private UUID instrumentId;

    @Column(length = 12)
    private String isin;

    @Column(length = 9)
    private String cusip;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 10
    )
    private IoiSide side;

    @Column(
            nullable = false,
            precision = 19,
            scale = 4
    )
    private BigDecimal quantity;

    @Column(
            name = "target_price",
            precision = 19,
            scale = 4
    )
    private BigDecimal targetPrice;

    @Column(
            nullable = false,
            length = 3
    )
    private String currency;

    @Column(name = "settlement_date")
    private LocalDate settlementDate;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 40
    )
    private IoiStatus status;

    @Column(
            name = "client_comment",
            length = 1000
    )
    private String clientComment;

    @Column(
            name = "sales_comment",
            length = 1000
    )
    private String salesComment;

    @Column(
            name = "trader_comment",
            length = 1000
    )
    private String traderComment;

    @Column(
            name = "rejection_reason",
            length = 1000
    )
    private String rejectionReason;

    @Column(name = "sales_user_id")
    private UUID salesUserId;

    @Column(name = "trader_user_id")
    private UUID traderUserId;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Column(name = "sales_reviewed_at")
    private Instant salesReviewedAt;

    @Column(name = "trader_reviewed_at")
    private Instant traderReviewedAt;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(
            name = "created_at",
            nullable = false
    )
    private Instant createdAt;

    @Column(
            name = "created_by",
            nullable = false,
            length = 100
    )
    private String createdBy;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    @Column(
            name = "updated_by",
            nullable = false,
            length = 100
    )
    private String updatedBy;

    @Version
    @Column(nullable = false)
    private Long version;

    @Column(name = "offering_id")
    private UUID offeringId;

    @Column(name = "offering_created_at")
    private Instant offeringCreatedAt;

    protected Ioi() {
    }

    public Ioi(
            UUID id,
            String ioiNumber,
            UUID clientId,
            UUID instrumentId,
            String isin,
            String cusip,
            IoiSide side,
            BigDecimal quantity,
            BigDecimal targetPrice,
            String currency,
            LocalDate settlementDate,
            IoiStatus status,
            String clientComment,
            Instant createdAt,
            String createdBy,
            Instant updatedAt,
            String updatedBy
    ) {
        this.id = id;
        this.ioiNumber = ioiNumber;
        this.clientId = clientId;
        this.instrumentId = instrumentId;
        this.isin = normalize(isin);
        this.cusip = normalize(cusip);
        this.side = side;
        this.quantity = quantity;
        this.targetPrice = targetPrice;
        this.currency = normalize(currency);
        this.settlementDate = settlementDate;
        this.status = status;
        this.clientComment = clientComment;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public void updateDraft(
            UpdateIoiRequest request,
            String authenticatedUsername,
            Instant updatedAt
    ) {
        ensureDraft();

        this.clientId = request.clientId();
        this.instrumentId = request.instrumentId();
        this.isin = normalize(request.isin());
        this.cusip = normalize(request.cusip());
        this.side = request.side();
        this.quantity = request.quantity();
        this.targetPrice = request.targetPrice();
        this.currency = normalize(request.currency());
        this.settlementDate = request.settlementDate();
        this.clientComment = request.clientComment();
        this.updatedBy = authenticatedUsername;
        this.updatedAt = updatedAt;
    }

    private void ensureSalesReviewPending() {
        if (status != IoiStatus.SALES_REVIEW_PENDING) {
            throw new InvalidIoiStateException(
                    "Sales review can be completed only for " +
                            "SALES_REVIEW_PENDING IOIs. Current status: " + status
            );
        }
    }

    public void completeSalesReview(
            String salesComment,
            String authenticatedUsername,
            Instant reviewedAt
    ) {
        ensureSalesReviewPending();

        this.status = IoiStatus.TRADER_REVIEW_PENDING;
        this.salesComment = salesComment;
        this.salesReviewedAt = reviewedAt;
        this.updatedAt = reviewedAt;
        this.updatedBy = authenticatedUsername;
    }

    public void submit(
            String authenticatedUsername,
            Instant submittedAt
    ) {
        ensureDraft();

        this.status = IoiStatus.SUBMITTED;
        this.submittedAt = submittedAt;
        this.updatedAt = submittedAt;
        this.updatedBy = authenticatedUsername;
    }

    public void startSalesReview(
            UUID salesUserId,
            String authenticatedUsername,
            Instant updatedAt
    ) {
        ensureSubmitted();

        this.status = IoiStatus.SALES_REVIEW_PENDING;
        this.salesUserId = salesUserId;
        this.updatedAt = updatedAt;
        this.updatedBy = authenticatedUsername;
    }

    private void ensureDraft() {
        if (status != IoiStatus.DRAFT) {
            throw new InvalidIoiStateException(
                    "Operation is allowed only for DRAFT IOIs. Current status: "
                            + status
            );
        }
    }

    public void approve(
            UUID traderUserId,
            String traderComment,
            String authenticatedUsername,
            Instant reviewedAt
    ) {
        ensureTraderReviewPending();

        this.status = IoiStatus.APPROVED;
        this.traderUserId = traderUserId;
        this.traderComment = traderComment;
        this.traderReviewedAt = reviewedAt;
        this.approvedAt = reviewedAt;
        this.updatedAt = reviewedAt;
        this.updatedBy = authenticatedUsername;
    }

    public void reject(
            UUID traderUserId,
            String traderComment,
            String rejectionReason,
            String authenticatedUsername,
            Instant reviewedAt
    ) {
        ensureTraderReviewPending();

        this.status = IoiStatus.REJECTED;
        this.traderUserId = traderUserId;
        this.traderComment = traderComment;
        this.rejectionReason = rejectionReason;
        this.traderReviewedAt = reviewedAt;
        this.updatedAt = reviewedAt;
        this.updatedBy = authenticatedUsername;
    }

    private void ensureTraderReviewPending() {
        if (status != IoiStatus.TRADER_REVIEW_PENDING) {
            throw new InvalidIoiStateException(
                    "Trader decision is allowed only for " +
                            "TRADER_REVIEW_PENDING IOIs. Current status: " + status
            );
        }
    }

    private void ensureSubmitted() {
        if (status != IoiStatus.SUBMITTED) {
            throw new InvalidIoiStateException(
                    "Sales review can start only for SUBMITTED IOIs. "
                            + "Current status: "
                            + status
            );
        }
    }

    private String normalize(String value) {
        return value == null
                ? null
                : value.trim().toUpperCase(Locale.ROOT);
    }

    public UUID getId() {
        return id;
    }

    public String getIoiNumber() {
        return ioiNumber;
    }

    public UUID getClientId() {
        return clientId;
    }

    public UUID getInstrumentId() {
        return instrumentId;
    }

    public String getIsin() {
        return isin;
    }

    public String getCusip() {
        return cusip;
    }

    public IoiSide getSide() {
        return side;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getTargetPrice() {
        return targetPrice;
    }

    public String getCurrency() {
        return currency;
    }

    public LocalDate getSettlementDate() {
        return settlementDate;
    }

    public IoiStatus getStatus() {
        return status;
    }

    public String getClientComment() {
        return clientComment;
    }

    public String getSalesComment() {
        return salesComment;
    }

    public String getTraderComment() {
        return traderComment;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public UUID getSalesUserId() {
        return salesUserId;
    }

    public UUID getTraderUserId() {
        return traderUserId;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public Instant getSalesReviewedAt() {
        return salesReviewedAt;
    }

    public Instant getTraderReviewedAt() {
        return traderReviewedAt;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public Long getVersion() {
        return version;
    }

    public UUID getOfferingId() {
        return offeringId;
    }

    public Instant getOfferingCreatedAt() {
        return offeringCreatedAt;
    }
    public void cancel(
            String reason,
            String authenticatedUsername,
            Instant cancelledAt
    ) {
        ensureCancellable();

        this.status = IoiStatus.CANCELLED;
        this.rejectionReason = reason;
        this.updatedAt = cancelledAt;
        this.updatedBy = authenticatedUsername;
    }
    private void ensureCancellable() {
        if (status != IoiStatus.DRAFT && status != IoiStatus.SUBMITTED) {
            throw new InvalidIoiStateException(
                    "Only DRAFT or SUBMITTED IOIs can be cancelled. Current status: "
                            + status
            );
        }
    }

    public void markOfferingCreated(
            UUID offeringId,
            String authenticatedUsername,
            Instant createdAt
    ) {
        ensureApproved();

        this.status = IoiStatus.OFFERING_CREATED;
        this.offeringId = offeringId;
        this.offeringCreatedAt = createdAt;
        this.updatedAt = createdAt;
        this.updatedBy = authenticatedUsername;
    }

    private void ensureApproved() {
        if (status != IoiStatus.APPROVED) {
            throw new InvalidIoiStateException(
                    "An offering can be created only for APPROVED IOIs. " +
                            "Current status: " + status
            );
        }
    }
}