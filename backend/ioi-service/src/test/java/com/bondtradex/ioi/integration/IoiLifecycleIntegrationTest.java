package com.bondtradex.ioi.integration;

import com.bondtradex.ioi.entity.Ioi;
import com.bondtradex.ioi.entity.IoiSide;
import com.bondtradex.ioi.entity.IoiStatus;
import com.bondtradex.ioi.repository.IoiRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import com.bondtradex.ioi.kafka.producer.IoiEventProducer;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import com.bondtradex.ioi.kafka.event.IoiCreatedEvent;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class IoiLifecycleIntegrationTest {

    private static final String BASE_URL = "/api/v1/iois";
    private static final String USERNAME = "integration-test-user";

    private static final UUID CLIENT_ID =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    private static final UUID INSTRUMENT_ID =
            UUID.fromString("33333333-3333-3333-3333-333333333333");

    private static final UUID SALES_USER_ID =
            UUID.fromString("44444444-4444-4444-4444-444444444444");

    private static final UUID TRADER_USER_ID =
            UUID.fromString("55555555-5555-5555-5555-555555555555");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IoiRepository ioiRepository;

    @Value("${spring.datasource.url}")
    private String datasourceUrl;

    /*
     * Required because SecurityConfig configures the application
     * as an OAuth2 resource server.
     *
     * The actual JWT signature is not tested here. We create an
     * authenticated JWT using Spring Security's test support.
     */
    @MockitoBean
    private JwtDecoder jwtDecoder;
    @MockitoBean
    private IoiEventProducer ioiEventProducer;
    @BeforeEach
    void setUp() {
        verifyTestDatabase();
        ioiRepository.deleteAll();
    }

    @AfterEach
    void cleanUp() {
        ioiRepository.deleteAll();
    }

    @Test
    void completeIoiLifecycle_shouldPersistAllStatusChanges() throws Exception {

        /*
         * =========================================================
         * Step 1: Create IOI
         * =========================================================
         */

        ObjectNode createRequest = objectMapper.createObjectNode();

        createRequest.put("clientId", CLIENT_ID.toString());
        createRequest.put("instrumentId", INSTRUMENT_ID.toString());
        createRequest.put("isin", "US0378331005");
        createRequest.put("cusip", "037833100");
        createRequest.put("side", "BUY");
        createRequest.put("quantity", 1_000_000);
        createRequest.put("targetPrice", 99.25);
        createRequest.put("currency", "USD");
        createRequest.put(
                "settlementDate",
                LocalDate.now().plusDays(3).toString()
        );
        createRequest.put(
                "clientComment",
                "Created through integration test"
        );

        mockMvc.perform(
                        post(BASE_URL)
                                .with(jwtWithAuthority("IOI_CREATE"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                createRequest
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.message")
                                .value("IOI created successfully")
                )
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.version").value(0));
        verify(ioiEventProducer)
                .publishIoiCreated(any(IoiCreatedEvent.class));
        assertThat(ioiRepository.count()).isEqualTo(1);

        Ioi createdIoi = ioiRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow(
                        () -> new AssertionError(
                                "Created IOI was not found in the database"
                        )
                );

        UUID ioiId = createdIoi.getId();

        assertThat(createdIoi.getClientId()).isEqualTo(CLIENT_ID);
        assertThat(createdIoi.getInstrumentId())
                .isEqualTo(INSTRUMENT_ID);
        assertThat(createdIoi.getIsin()).isEqualTo("US0378331005");
        assertThat(createdIoi.getCusip()).isEqualTo("037833100");
        assertThat(createdIoi.getSide()).isEqualTo(IoiSide.BUY);

        assertThat(createdIoi.getQuantity())
                .isEqualByComparingTo(new BigDecimal("1000000"));

        assertThat(createdIoi.getTargetPrice())
                .isEqualByComparingTo(new BigDecimal("99.25"));

        assertThat(createdIoi.getCurrency()).isEqualTo("USD");
        assertThat(createdIoi.getStatus()).isEqualTo(IoiStatus.DRAFT);
        assertThat(createdIoi.getCreatedBy()).isEqualTo(USERNAME);
        assertThat(createdIoi.getVersion()).isEqualTo(0L);

        /*
         * =========================================================
         * Step 2: Update draft IOI
         * =========================================================
         */

        ObjectNode updateRequest = objectMapper.createObjectNode();

        updateRequest.put("clientId", CLIENT_ID.toString());
        updateRequest.put("instrumentId", INSTRUMENT_ID.toString());
        updateRequest.put("isin", "US0378331005");
        updateRequest.put("cusip", "037833100");
        updateRequest.put("side", "BUY");
        updateRequest.put("quantity", 1_500_000);
        updateRequest.put("targetPrice", 99.50);
        updateRequest.put("currency", "USD");
        updateRequest.put(
                "settlementDate",
                LocalDate.now().plusDays(4).toString()
        );
        updateRequest.put(
                "clientComment",
                "Updated through integration test"
        );
        updateRequest.put("version", createdIoi.getVersion());

        mockMvc.perform(
                        put(BASE_URL + "/{id}", ioiId)
                                .with(jwtWithAuthority("IOI_UPDATE"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                updateRequest
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.message")
                                .value("IOI updated successfully")
                )
                .andExpect(jsonPath("$.data.status").value("DRAFT"))
                .andExpect(jsonPath("$.data.version").value(1));

        Ioi updatedIoi = findIoi(ioiId);

        assertThat(updatedIoi.getQuantity())
                .isEqualByComparingTo(new BigDecimal("1500000"));

        assertThat(updatedIoi.getTargetPrice())
                .isEqualByComparingTo(new BigDecimal("99.50"));

        assertThat(updatedIoi.getClientComment())
                .isEqualTo("Updated through integration test");

        assertThat(updatedIoi.getStatus()).isEqualTo(IoiStatus.DRAFT);
        assertThat(updatedIoi.getVersion()).isEqualTo(1L);

        /*
         * =========================================================
         * Step 3: Submit IOI
         * =========================================================
         *
         * We send JSON directly, so the test does not need to import
         * or instantiate SubmitIoiRequest.
         */

        ObjectNode submitRequest = versionRequest(
                updatedIoi.getVersion()
        );

        mockMvc.perform(
                        post(BASE_URL + "/{id}/submit", ioiId)
                                .with(jwtWithAuthority("IOI_SUBMIT"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                submitRequest
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.message")
                                .value("IOI submitted successfully")
                )
                .andExpect(jsonPath("$.data.status").value("SUBMITTED"))
                .andExpect(jsonPath("$.data.version").value(2));

        Ioi submittedIoi = findIoi(ioiId);

        assertThat(submittedIoi.getStatus())
                .isEqualTo(IoiStatus.SUBMITTED);

        assertThat(submittedIoi.getSubmittedAt()).isNotNull();
        assertThat(submittedIoi.getVersion()).isEqualTo(2L);

        /*
         * =========================================================
         * Step 4: Start sales review
         * =========================================================
         *
         * salesUserId is a query parameter, not part of the JSON body.
         */

        ObjectNode startSalesReviewRequest = versionRequest(
                submittedIoi.getVersion()
        );

        mockMvc.perform(
                        post(
                                BASE_URL + "/{id}/sales-review/start",
                                ioiId
                        )
                                .queryParam(
                                        "salesUserId",
                                        SALES_USER_ID.toString()
                                )
                                .with(
                                        jwtWithAuthority(
                                                "IOI_SALES_REVIEW"
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                startSalesReviewRequest
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Sales review started successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.status")
                                .value("SALES_REVIEW_PENDING")
                )
                .andExpect(jsonPath("$.data.version").value(3));

        Ioi salesReviewPendingIoi = findIoi(ioiId);

        assertThat(salesReviewPendingIoi.getStatus())
                .isEqualTo(IoiStatus.SALES_REVIEW_PENDING);

        assertThat(salesReviewPendingIoi.getSalesUserId())
                .isEqualTo(SALES_USER_ID);

        assertThat(salesReviewPendingIoi.getVersion()).isEqualTo(3L);

        /*
         * =========================================================
         * Step 5: Complete sales review
         * =========================================================
         */

        ObjectNode completeSalesReviewRequest =
                objectMapper.createObjectNode();

        completeSalesReviewRequest.put(
                "salesComment",
                "Client eligibility and limits verified"
        );

        completeSalesReviewRequest.put(
                "version",
                salesReviewPendingIoi.getVersion()
        );

        mockMvc.perform(
                        post(
                                BASE_URL + "/{id}/sales-review/complete",
                                ioiId
                        )
                                .with(
                                        jwtWithAuthority(
                                                "IOI_SALES_REVIEW"
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                completeSalesReviewRequest
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Sales review completed successfully"
                                )
                )
                .andExpect(
                        jsonPath("$.data.status")
                                .value("TRADER_REVIEW_PENDING")
                )
                .andExpect(jsonPath("$.data.version").value(4));

        Ioi traderReviewPendingIoi = findIoi(ioiId);

        assertThat(traderReviewPendingIoi.getStatus())
                .isEqualTo(IoiStatus.TRADER_REVIEW_PENDING);

        assertThat(traderReviewPendingIoi.getSalesComment())
                .isEqualTo(
                        "Client eligibility and limits verified"
                );

        assertThat(traderReviewPendingIoi.getSalesReviewedAt())
                .isNotNull();

        assertThat(traderReviewPendingIoi.getVersion())
                .isEqualTo(4L);

        /*
         * =========================================================
         * Step 6: Approve IOI
         * =========================================================
         */

        ObjectNode approveRequest = objectMapper.createObjectNode();

        approveRequest.put(
                "traderUserId",
                TRADER_USER_ID.toString()
        );
        approveRequest.put(
                "traderComment",
                "Approved after trader review"
        );
        approveRequest.put(
                "version",
                traderReviewPendingIoi.getVersion()
        );

        mockMvc.perform(
                        post(BASE_URL + "/{id}/approve", ioiId)
                                .with(jwtWithAuthority("IOI_APPROVE"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                approveRequest
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(
                        jsonPath("$.message")
                                .value("IOI approved successfully")
                )
                .andExpect(jsonPath("$.data.status").value("APPROVED"))
                .andExpect(jsonPath("$.data.version").value(5));

        Ioi approvedIoi = findIoi(ioiId);

        assertThat(approvedIoi.getStatus())
                .isEqualTo(IoiStatus.APPROVED);

        assertThat(approvedIoi.getTraderUserId())
                .isEqualTo(TRADER_USER_ID);

        assertThat(approvedIoi.getTraderComment())
                .isEqualTo("Approved after trader review");

        assertThat(approvedIoi.getTraderReviewedAt()).isNotNull();
        assertThat(approvedIoi.getApprovedAt()).isNotNull();
        assertThat(approvedIoi.getVersion()).isEqualTo(5L);

        /*
         * The full lifecycle must update the same database row.
         */
        assertThat(ioiRepository.count()).isEqualTo(1);
    }

    private Ioi findIoi(UUID ioiId) {
        return ioiRepository.findById(ioiId)
                .orElseThrow(
                        () -> new AssertionError(
                                "IOI was not found: " + ioiId
                        )
                );
    }

    private ObjectNode versionRequest(Long version) {
        ObjectNode request = objectMapper.createObjectNode();
        request.put("version", version);
        return request;
    }

    private RequestPostProcessor jwtWithAuthority(
            String authority
    ) {
        return jwt()
                .jwt(
                        jwtBuilder -> jwtBuilder
                                .subject(USERNAME)
                                .claim(
                                        "preferred_username",
                                        USERNAME
                                )
                )
                .authorities(() -> authority);
    }

    private void verifyTestDatabase() {
        assertThat(datasourceUrl)
                .as(
                        "Integration tests must use a database " +
                                "whose URL contains 'test'"
                )
                .containsIgnoringCase("test");
    }
}