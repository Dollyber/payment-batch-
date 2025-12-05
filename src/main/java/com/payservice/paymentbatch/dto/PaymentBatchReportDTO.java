package com.payservice.paymentbatch.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentBatchReportDTO {

    // Datos del archivo masivo
    private Integer customerId;
    private Integer serviceId;
    private Integer receiptId;
    private BigDecimal attemptedAmount;

    // Datos reales del pago desde el API
    private ReceiptInfoDTO receipt;
    private String paymentStatus;   // PAID / PARTIALLY_PAID

    // Estado del batch (operativo)
    private String batchStatus;     // PROCESSED / REJECTED
    private String reason;          // explicación
}


