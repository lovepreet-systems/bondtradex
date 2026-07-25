package com.bondtradex.ioi.controller;

import com.bondtradex.ioi.config.SecurityConfig;
import com.bondtradex.ioi.dto.*;
import com.bondtradex.ioi.entity.IoiSide;
import com.bondtradex.ioi.entity.IoiStatus;
import com.bondtradex.ioi.exception.GlobalExceptionHandler;
import com.bondtradex.ioi.exception.InvalidIoiStateException;
import com.bondtradex.ioi.exception.ResourceNotFoundException;
import com.bondtradex.ioi.service.IoiService;
import com.fasterxml.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IoiController.class)
@Import({
        SecurityConfig.class,
        GlobalExceptionHandler.class
})
class IoiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IoiService ioiService;

    /*
     * SecurityConfig configures the application as an OAuth2 resource server.
     * Spring therefore expects a JwtDecoder bean while creating the test context.
     *
     * This mock satisfies that dependency. The jwt() test helper creates the
     * authenticated user directly, so no real token decoding takes place.
     */
    @MockitoBean
    private JwtDecoder jwtDecoder;

    private UUID ioiId;
    private UUID clientId;
    private UUID instrumentId;

    private CreateIoiRequest createRequest;
    private IoiResponse ioiResponse;
    private UpdateIoiRequest updateIoiRequest;

    private SubmitIoiRequest submitIoiRequest;

    @BeforeEach
    void setUp() {
        ioiId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        instrumentId = UUID.randomUUID();

        createRequest = new CreateIoiRequest(
                clientId,
                instrumentId,
                "US0378331005",
                "037833100",
                IoiSide.BUY,
                new BigDecimal("1000000.0000"),
                new BigDecimal("101.2500"),
                "USD",
                LocalDate.now().plusDays(2),
                "Need execution today"
        );

        ioiResponse = new IoiResponse(
                ioiId,
                "IOI-20260718-000001",
                clientId,
                instrumentId,
                "US0378331005",
                "037833100",
                IoiSide.BUY,
                new BigDecimal("1000000.0000"),
                new BigDecimal("101.2500"),
                "USD",
                LocalDate.now().plusDays(2),
                IoiStatus.DRAFT,
                "Need execution today",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Instant.now(),
                "admin",
                0L
        );

        updateIoiRequest = new UpdateIoiRequest(clientId,
                 instrumentId,
                "US0378331005",
                "037833100",
                IoiSide.BUY,
                new BigDecimal("2000000.0000"),
                new BigDecimal("102.5000"),
                "USD",
                LocalDate.now().plusDays(3),
                "Updated client comment",
                0L
        );

        submitIoiRequest = new SubmitIoiRequest(0L);
    }

    @Test
    void createIoi_shouldReturn201Created() throws Exception {
        when(ioiService.create(createRequest, "admin"))
                .thenReturn(ioiResponse);

        mockMvc.perform(
                        post("/api/v1/iois")

                                /*
                                 * Simulates an authenticated JWT user.
                                 *
                                 * subject("admin") makes authentication.getName()
                                 * return "admin" inside the controller.
                                 */
                                .with(jwt()
                                        .jwt(jwtBuilder ->
                                                jwtBuilder.subject("admin")
                                        )
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "IOI_CREATE"
                                                )
                                        )
                                )

                                /*
                                 * Tells Spring that the request body is JSON.
                                 */
                                .contentType(MediaType.APPLICATION_JSON)

                                /*
                                 * Converts the CreateIoiRequest record into JSON.
                                 */
                                .content(
                                        objectMapper.writeValueAsString(
                                                createRequest
                                        )
                                )
                )

                /*
                 * Controller returns HttpStatus.CREATED.
                 */
                .andExpect(status().isCreated())

                /*
                 * Confirms that the response content type is JSON.
                 */
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )

                /*
                 * ApiResponse.success(...) sets success to true.
                 */
                .andExpect(
                        jsonPath("$.success").value(true)
                )

                .andExpect(
                        jsonPath("$.message")
                                .value("IOI created successfully")
                )

                /*
                 * The actual IoiResponse is inside ApiResponse.data.
                 */
                .andExpect(
                        jsonPath("$.data.id")
                                .value(ioiId.toString())
                )

                .andExpect(
                        jsonPath("$.data.ioiNumber")
                                .value("IOI-20260718-000001")
                )

                .andExpect(
                        jsonPath("$.data.clientId")
                                .value(clientId.toString())
                )

                .andExpect(
                        jsonPath("$.data.instrumentId")
                                .value(instrumentId.toString())
                )

                .andExpect(
                        jsonPath("$.data.isin")
                                .value("US0378331005")
                )

                .andExpect(
                        jsonPath("$.data.cusip")
                                .value("037833100")
                )

                .andExpect(
                        jsonPath("$.data.side")
                                .value("BUY")
                )

                .andExpect(
                        jsonPath("$.data.quantity")
                                .value(1_000_000.0000)
                )

                .andExpect(
                        jsonPath("$.data.targetPrice")
                                .value(101.2500)
                )

                .andExpect(
                        jsonPath("$.data.currency")
                                .value("USD")
                )

                .andExpect(
                        jsonPath("$.data.status")
                                .value("DRAFT")
                )

                .andExpect(
                        jsonPath("$.data.createdBy")
                                .value("admin")
                )

                .andExpect(
                        jsonPath("$.data.version")
                                .value(0)
                )

                /*
                 * ApiResponse creates the timestamp automatically.
                 * We only verify that it exists.
                 */
                .andExpect(
                        jsonPath("$.timestamp").exists()
                );

        /*
         * Verifies that the controller passed the deserialized request and
         * authenticated username to the service.
         *
         * Records implement equals() automatically, so eq(createRequest)
         * is not necessary here.
         */
        verify(ioiService).create(createRequest, "admin");
    }

    @Test
    void createIoi_shouldReturn400_whenRequestIsInvalid() throws Exception {

        CreateIoiRequest invalidRequest = new CreateIoiRequest(
                null,                               // clientId
                null,                               // instrumentId
                "",                                 // isin
                "",                                 // cusip
                null,                               // side
                null,                               // quantity
                null,                               // targetPrice
                "",                                 // currency
                null,                               // settlementDate
                ""                                  // clientComment
        );

        mockMvc.perform(
                        post("/api/v1/iois")
                                .with(jwt()
                                        .jwt(jwt -> jwt.subject("admin"))
                                        .authorities(
                                                new SimpleGrantedAuthority("IOI_CREATE")
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(invalidRequest))
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Failed Validation"))
                .andExpect(jsonPath("$.data").isMap());

        verify(ioiService, never()).create(any(), any());
    }

    @Test
    void createIoi_shouldReturn401_whenUserIsNotAuthenticated() throws Exception {

        mockMvc.perform(
                        post("/api/v1/iois")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isUnauthorized());

        verify(ioiService, never()).create(any(), any());
    }

    @Test
    void createIoi_shouldReturn403_whenUserHasNoCreatePermission() throws Exception {

        mockMvc.perform(
                        post("/api/v1/iois")
                                .with(jwt()
                                        .jwt(jwt -> jwt.subject("admin"))
                                        .authorities(
                                                new SimpleGrantedAuthority("IOI_UPDATE")
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createRequest))
                )
                .andExpect(status().isForbidden());

        verify(ioiService, never()).create(any(), any());
    }
    @Test
    void createIoi_shouldReturn404_whenResourceIsNotFound() throws Exception {

        when(ioiService.create(createRequest, "admin"))
                .thenThrow(new ResourceNotFoundException("Client not found"));

        mockMvc.perform(
                        post("/api/v1/iois")
                                .with(jwt()
                                        .jwt(jwt -> jwt.subject("admin"))
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "IOI_CREATE"
                                                )
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                createRequest
                                        )
                                )
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(
                        jsonPath("$.message")
                                .value("Client not found")
                )
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(ioiService).create(createRequest, "admin");
    }

    @Test
    void createIoi_shouldReturn409_whenIoiStateIsInvalid() throws Exception {

        when(ioiService.create(createRequest, "admin"))
                .thenThrow(
                        new InvalidIoiStateException(
                                "IOI cannot be created in the current state"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/iois")
                                .with(jwt()
                                        .jwt(jwt -> jwt.subject("admin"))
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "IOI_CREATE"
                                                )
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                createRequest
                                        )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "IOI cannot be created in the current state"
                                )
                )
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(ioiService).create(createRequest, "admin");
    }

    @Test
    void createIoi_shouldReturn409_whenOptimisticLockingFails()
            throws Exception {

        when(ioiService.create(createRequest, "admin"))
                .thenThrow(
                        new OptimisticLockingFailureException(
                                "Concurrent update detected"
                        )
                );

        mockMvc.perform(
                        post("/api/v1/iois")
                                .with(jwt()
                                        .jwt(jwt -> jwt.subject("admin"))
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "IOI_CREATE"
                                                )
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                createRequest
                                        )
                                )
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(
                        jsonPath("$.message").value(
                                "The IOI was updated by another request. " +
                                        "Reload it and try again."
                        )
                )
                .andExpect(jsonPath("$.data").doesNotExist())
                .andExpect(jsonPath("$.timestamp").exists());

        verify(ioiService).create(createRequest, "admin");
    }

    @Test
    void createIoi_shouldReturn500_whenUnexpectedExceptionOccurs()
            throws Exception {

        when(ioiService.create(createRequest, "admin"))
                .thenThrow(
                        new RuntimeException("Database unavailable")
                );

        mockMvc.perform(
                        post("/api/v1/iois")
                                .with(jwt()
                                        .jwt(jwt -> jwt.subject("admin"))
                                        .authorities(
                                                new SimpleGrantedAuthority(
                                                        "IOI_CREATE"
                                                )
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(
                                                createRequest
                                        )
                                )
                )
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(
                        jsonPath("$.message")
                                .value("Something went wrong")
                )
                .andExpect(
                        jsonPath("$.data")
                                .value("Database unavailable")
                )
                .andExpect(jsonPath("$.timestamp").exists());

        verify(ioiService).create(createRequest, "admin");
    }

    @Test
    void updateDraft_shouldReturn200() throws Exception {

        UpdateIoiRequest request = updateIoiRequest;// build valid request

                when(ioiService.updateDraft(
                        eq(ioiId),
                        eq(request),
                        eq("admin")
                )).thenReturn(ioiResponse);

        mockMvc.perform(
                        put("/api/v1/iois/{id}", ioiId)
                                .with(jwt()
                                        .jwt(jwt -> jwt.subject("admin"))
                                        .authorities(new SimpleGrantedAuthority("IOI_UPDATE")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message")
                        .value("IOI updated successfully"));

        verify(ioiService)
                .updateDraft(ioiId, request, "admin");
    }

    @Test
    void submit_shouldReturn200() throws Exception {

        SubmitIoiRequest request = submitIoiRequest; // build request

                when(ioiService.submit(
                        eq(ioiId),
                        eq(request),
                        eq("admin")
                )).thenReturn(ioiResponse);

        mockMvc.perform(
                        post("/api/v1/iois/{id}/submit", ioiId)
                                .with(jwt()
                                        .jwt(jwt -> jwt.subject("admin"))
                                        .authorities(new SimpleGrantedAuthority("IOI_SUBMIT")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(ioiService)
                .submit(ioiId, request, "admin");
    }

    @Test
    void startSalesReview_shouldReturn200() throws Exception {

        UUID salesUserId = UUID.randomUUID();

        StartSalesReviewRequest request = new StartSalesReviewRequest(0L);// build request

                when(ioiService.startSalesReview(
                        eq(ioiId),
                        eq(salesUserId),
                        eq(request),
                        eq("admin")
                )).thenReturn(ioiResponse);

        mockMvc.perform(
                        post("/api/v1/iois/{id}/sales-review/start", ioiId)
                                .param("salesUserId", salesUserId.toString())
                                .with(jwt()
                                        .jwt(jwt -> jwt.subject("admin"))
                                        .authorities(new SimpleGrantedAuthority("IOI_SALES_REVIEW")))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());

        verify(ioiService)
                .startSalesReview(
                        ioiId,
                        salesUserId,
                        request,
                        "admin"
                );
    }

    @Test
    void getById_shouldReturn200() throws Exception {

        when(ioiService.getById(ioiId))
                .thenReturn(ioiResponse);

        mockMvc.perform(
                        get("/api/v1/iois/{id}", ioiId)
                                .with(jwt()
                                        .jwt(jwt -> jwt.subject("admin"))
                                        .authorities(new SimpleGrantedAuthority("IOI_CREATE")))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(ioiService).getById(ioiId);
    }

    @Test
    void findAll_shouldReturn200() throws Exception {

        PagedResponse<IoiResponse> page =
                new PagedResponse<>(
                        List.of(ioiResponse),
                        1,
                        0,
                        10,
                        1,
                        true,
                        true
                );

        when(ioiService.findAll(
                IoiStatus.DRAFT,
                0,
                10
        )).thenReturn(page);

        mockMvc.perform(
                        get("/api/v1/iois")
                                .param("status", "DRAFT")
                                .param("page", "0")
                                .param("size", "10")
                                .with(jwt()
                                        .jwt(jwt -> jwt.subject("admin"))
                                        .authorities(new SimpleGrantedAuthority("IOI_CREATE")))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(ioiService)
                .findAll(
                        IoiStatus.DRAFT,
                        0,
                        10
                );
    }
}