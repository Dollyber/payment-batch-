package com.payservice.paymentbatch.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponseDTO {

    private ReceiptInfoDTO receipt;         // Datos del recibo
    private BigDecimal amount;              // Monto que se registró
    private String paymentCurrency;
    private BigDecimal previousPendingAmount;
    private BigDecimal newPendingAmount;

    // Estado del pago real:
    // PAID o PARTIALLY_PAID
    private String paymentStatus;

    private LocalDateTime paymentDate;
}
