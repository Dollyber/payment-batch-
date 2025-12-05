package com.payservice.paymentbatch.job;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.payservice.paymentbatch.client.PaymentApiClient;
import com.payservice.paymentbatch.dto.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpHeaders;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpResponse;

@ExtendWith(MockitoExtension.class)
class PaymentItemProcessorTest {

    @Mock
    private PaymentApiClient apiClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PaymentItemProcessor processor;

    private PaymentRequestBatchDTO item;

    @BeforeEach
    void setup() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        apiClient = mock(PaymentApiClient.class);
        processor = new PaymentItemProcessor(apiClient, objectMapper);

    }

    // TEST: caso exitoso (PAID)
    @Test
    void process_successfulPayment_returnsProcessedReport() {
        // Arrange
        PaymentRequestBatchDTO input = new PaymentRequestBatchDTO(1, 1, 100, new BigDecimal("50.00"), "PEN");

        PaymentResponseDTO apiResponse = new PaymentResponseDTO();
        apiResponse.setPaymentStatus("PAID");
        apiResponse.setReceipt(new ReceiptInfoDTO("RC001", "2025-12", new BigDecimal(50), new BigDecimal(0), "PEN"));

        when(apiClient.registerPayment(1, 100, new BigDecimal("50.00"), "PEN"))
                .thenReturn(apiResponse);

        // Act
        PaymentBatchReportDTO result = processor.process(input);

        // Assert
        assertEquals("PROCESSED", result.getBatchStatus());
        assertEquals("Full payment", result.getReason());
        assertEquals("PAID", result.getPaymentStatus());
    }


    // TEST: caso exitoso (PARTIALLY_PAID)
    @Test
    void process_partialPayment_returnsProcessedPartial() {
        PaymentRequestBatchDTO input = new PaymentRequestBatchDTO(1, 1, 100, new BigDecimal("30.00"), "PEN");

        PaymentResponseDTO apiResponse = new PaymentResponseDTO();
        apiResponse.setPaymentStatus("PARTIALLY_PAID");
        apiResponse.setReceipt(new ReceiptInfoDTO("RC001", "2025-12", new BigDecimal(50), new BigDecimal(20), "PEN"));


        when(apiClient.registerPayment(any(), any(), any(), any()))
                .thenReturn(apiResponse);

        PaymentBatchReportDTO result = processor.process(input);

        assertEquals("PROCESSED", result.getBatchStatus());
        assertEquals("Partial payment", result.getReason());
        assertEquals("PARTIALLY_PAID", result.getPaymentStatus());
    }


    // TEST: error controlado (400, JSON válido)
    @Test
    void process_apiError_parsedJsonError_returnsRejected() throws Exception {

        objectMapper.registerModule(new JavaTimeModule());

        PaymentRequestBatchDTO input =
                new PaymentRequestBatchDTO(1, 1, 100, new BigDecimal("10.00"), "PEN");

        String jsonError = "INVALID_JSON";

        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                HttpHeaders.EMPTY,
                jsonError.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        when(apiClient.registerPayment(
                anyInt(),
                anyInt(),
                any(BigDecimal.class),
                anyString()
        )).thenThrow(ex);

        PaymentBatchReportDTO res = processor.process(input);

        assertEquals("REJECTED", res.getBatchStatus());
        assertTrue(res.getReason().contains("Error inesperado:"));
    }

    @Test
    void process_apiError_parseOK() throws Exception {

        // 1. DTO de entrada
        PaymentRequestBatchDTO item = new PaymentRequestBatchDTO(
                1, 2, 3,
                new java.math.BigDecimal("10.00"),
                "EUR"
        );

        // 2. JSON SIMULADO DE ERROR (válido)
        String apiErrorJson = """
        {
          "code": 400,
          "error": "Bad Request",
          "message": "RN1: Only PEN or USD allowed",
          "path": "/payments",
          "timestamp": "2025-12-05T15:54:56.963736"
        }
        """;

        // 3. Mock de HttpClientErrorException con body JSON
        ClientHttpResponse mockResp =
                new MockClientHttpResponse(apiErrorJson.getBytes(StandardCharsets.UTF_8), HttpStatus.BAD_REQUEST);

        HttpClientErrorException ex = HttpClientErrorException.create(
                HttpStatus.BAD_REQUEST,
                "Bad Request",
                mockResp.getHeaders(),
                apiErrorJson.getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8
        );

        // 4. Configurar mock para lanzar la excepción
        when(apiClient.registerPayment(any(), any(), any(), any()))
                .thenThrow(ex);

        // 5. Ejecutar processor
        PaymentBatchReportDTO result = processor.process(item);

        assertEquals("REJECTED", result.getBatchStatus());
        assertNull(result.getReceipt());
        assertEquals("400 - RN1: Only PEN or USD allowed", result.getReason());
    }
}

