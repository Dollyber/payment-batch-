package com.payservice.paymentbatch.job;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class PaymentJobCompletionListener implements JobExecutionListener {

    @Override
    public void afterJob(JobExecution jobExecution) {

        Path report = Paths.get("output/payment-report.csv");
        Path archive = Paths.get("output/archive/payment-report_" + System.currentTimeMillis() + ".csv");

        try {
            Files.createDirectories(archive.getParent()); // crear carpeta archive si no existe
            Files.move(report, archive);
            System.out.println("Reporte archivado: " + archive.toString());
        } catch (IOException e) {
            System.out.println("No se pudo archivar el reporte: " + e.getMessage());
        }

        System.out.println("===== Payment Batch Finished =====");
    }

}

