package com.payservice.paymentbatch.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptInfoDTO {
    private String receiptNumber;
    private String periodLabel;
    private BigDecimal receiptAmount;
    private BigDecimal pendingAmount;
    private String currency;
}