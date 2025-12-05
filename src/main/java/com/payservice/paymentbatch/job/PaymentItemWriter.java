package com.payservice.paymentbatch.job;

import com.payservice.paymentbatch.dto.PaymentBatchReportDTO;
import jakarta.annotation.PostConstruct;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Component
// Define que esta clase recibe objetos PaymentBatchReportDTO del Processor y los escribe en algún destino (archivo, DB, etc.).
public class PaymentItemWriter implements ItemWriter<PaymentBatchReportDTO> {

    private final Path outputPath = Paths.get("output/payment-report.csv");

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(outputPath.getParent());

        if (!Files.exists(outputPath)) {
            Files.writeString(outputPath,
                    "customerId,serviceId,receiptId,attemptedAmount,receiptNumber,periodLabel," +
                            "receiptAmount,pendingAmount,currency,paymentStatus,batchStatus,reason\n");
        }
    }

    @Override
    public void write(Chunk<? extends PaymentBatchReportDTO> chunk) throws Exception {

        for (PaymentBatchReportDTO r : chunk.getItems()) {

            String line = String.format(
                    "%d,%d,%d,%.2f,%s,%s,%.2f,%.2f,%s,%s,%s,%s%n",

                    r.getCustomerId(),
                    r.getServiceId(),
                    r.getReceiptId(),
                    r.getAttemptedAmount(),

                    r.getReceipt() != null ? r.getReceipt().getReceiptNumber() : "",
                    r.getReceipt() != null ? r.getReceipt().getPeriodLabel() : "",
                    r.getReceipt() != null ? r.getReceipt().getReceiptAmount() : 0.00,
                    r.getReceipt() != null ? r.getReceipt().getPendingAmount() : 0.00,
                    r.getReceipt() != null ? r.getReceipt().getCurrency() : "",

                    r.getPaymentStatus(),
                    r.getBatchStatus(),
                    r.getReason()
            );

            Files.writeString(outputPath, line, StandardOpenOption.APPEND);
        }
    }
}
