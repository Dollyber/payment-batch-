package com.payservice.paymentbatch.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    private int code;
    private String error;
    private String message;
    private String path;
    private LocalDateTime timestamp;
}