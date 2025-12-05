package com.payservice.paymentbatch.job;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

@Component
public class PaymentJobCompletionListener implements JobExecutionListener {

    Logger logger = Logger.getLogger(getClass().getName());

    @Override
    public void afterJob(JobExecution jobExecution) {

        Path report = Paths.get("output/payment-report.csv");
        Path archive = Paths.get("output/archive/payment-report_" + System.currentTimeMillis() + ".csv");

        try {
            Files.createDirectories(archive.getParent()); // crear carpeta archive si no existe
            Files.move(report, archive);
        } catch (IOException e) {
            logger.info("No se pudo archivar el reporte: " + e.getMessage());
        }

        logger.info("===== Payment Batch Finished =====");
    }

}

