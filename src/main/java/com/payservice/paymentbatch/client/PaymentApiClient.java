package com.payservice.paymentbatch.client;

import com.payservice.paymentbatch.dto.PaymentResponseDTO;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
public class PaymentApiClient {

    private final RestTemplate restTemplate; // Clase de Spring para hacer llamadas HTTP a APIs externas.


    public PaymentApiClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    // Este metodo llama al API de pagos y devuelve la respuesta como un objeto Java
    public PaymentResponseDTO registerPayment(Integer customerId,
                                              Integer receiptId,
                                              BigDecimal amount,
                                              String currency) {

        String url = String.format(
                "http://localhost:8088/payments/receipts/%d/customer/%d",
                receiptId,
                customerId
        );

        Map<String, Object> body = new HashMap<>();
        body.put("amount", amount);
        body.put("paymentCurrency", currency);

        try {
            ResponseEntity<PaymentResponseDTO> response =
                    restTemplate.postForEntity(url, body, PaymentResponseDTO.class);

            return response.getBody();

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new RuntimeException(
                    "Error HTTP al llamar al API de pagos. Status=" + ex.getStatusCode() +
                            ", Body=" + ex.getResponseBodyAsString(),
                    ex
            );

        } catch (ResourceAccessException ex) {
            throw new RuntimeException(
                    "Error de conexión al intentar contactar el API de pagos: " + ex.getMessage(),
                    ex
            );

        } catch (Exception ex) {
            throw new RuntimeException(
                    "Error inesperado al ejecutar la llamada al API de pagos: " + ex.getMessage(),
                    ex
            );
        }

    }

}
