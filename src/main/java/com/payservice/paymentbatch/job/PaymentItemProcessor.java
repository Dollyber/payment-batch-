package com.payservice.paymentbatch.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payservice.paymentbatch.client.PaymentApiClient;
import com.payservice.paymentbatch.dto.ErrorResponseDTO;
import com.payservice.paymentbatch.dto.PaymentBatchReportDTO;
import com.payservice.paymentbatch.dto.PaymentRequestBatchDTO;
import com.payservice.paymentbatch.dto.PaymentResponseDTO;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;

@Component
// Define que esta clase procesa objetos de tipo PaymentRequestBatchDTO y devuelve PaymentBatchReportDTO
public class PaymentItemProcessor implements ItemProcessor<PaymentRequestBatchDTO, PaymentBatchReportDTO> {

    private final PaymentApiClient apiClient;
    private ObjectMapper objectMapper;

    public PaymentItemProcessor(PaymentApiClient apiClient, ObjectMapper objectMapper) {
        this.apiClient = apiClient;
        this.objectMapper = objectMapper;
    }

    @Override
    // Se llama por cada registro leído del CSV.
    public PaymentBatchReportDTO process(PaymentRequestBatchDTO item) {

        // Llama a PaymentApiClient para registrar el pago.
        // Recibe un PaymentResponseDTO que indica si el pago fue PAID o PARTIALLY_PAID.
        try {
            PaymentResponseDTO response = apiClient.registerPayment(
                    item.getCustomerId(),
                    item.getReceiptId(),
                    item.getAmount(),
                    item.getCurrency()
            );

            // Construcción del reporte exitoso
            return new PaymentBatchReportDTO(
                    item.getCustomerId(),
                    item.getServiceId(),
                    item.getReceiptId(),
                    item.getAmount(),
                    response.getReceipt(),
                    response.getPaymentStatus(),    // PAID o PARTIALLY_PAID
                    "PROCESSED",
                    response.getPaymentStatus().equals("PAID") ?
                            "Full payment" : "Partial payment"
            );

        } catch (HttpClientErrorException ex) {

            try {
                ErrorResponseDTO apiError =
                        objectMapper.readValue(ex.getResponseBodyAsString(), ErrorResponseDTO.class);

                String errorMessage = apiError.getCode() + " - " + apiError.getMessage();

                return new PaymentBatchReportDTO(
                        item.getCustomerId(),
                        item.getServiceId(),
                        item.getReceiptId(),
                        item.getAmount(),
                        null,
                        null,
                        "REJECTED",
                        errorMessage
                );

            } catch (Exception parseEx) {
                return new PaymentBatchReportDTO(
                        item.getCustomerId(),
                        item.getServiceId(),
                        item.getReceiptId(),
                        item.getAmount(),
                        null,
                        null,
                        "REJECTED",
                        "Error inesperado: " + ex.getStatusCode() + " - " + ex.getStatusText()
                );
            }
        }
    }
}
