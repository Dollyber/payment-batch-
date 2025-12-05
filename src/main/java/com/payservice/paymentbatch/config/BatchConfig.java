package com.payservice.paymentbatch.config;

import com.payservice.paymentbatch.dto.PaymentBatchReportDTO;
import com.payservice.paymentbatch.dto.PaymentRequestBatchDTO;
import com.payservice.paymentbatch.job.PaymentItemProcessor;
import com.payservice.paymentbatch.job.PaymentItemReader;
import com.payservice.paymentbatch.job.PaymentItemWriter;
import com.payservice.paymentbatch.job.PaymentJobCompletionListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableBatchProcessing
public class BatchConfig {

    @Bean
    public Job paymentJob(JobRepository jobRepository,
                          Step paymentStep,
                          PaymentJobCompletionListener listener) {
        return new JobBuilder("paymentJob", jobRepository)
                .listener(listener) // para ejecutar código cuando el job termina o falla (por ejemplo, enviar un reporte o log
                .start(paymentStep)
                .build();
    }

    @Bean
    public Step paymentStep(JobRepository jobRepository,
                            PlatformTransactionManager txManager, //maneja las transacciones de cada chunk.
                            PaymentItemReader reader,
                            PaymentItemProcessor processor,
                            PaymentItemWriter writer) {

        return new StepBuilder("paymentStep", jobRepository)
                .<PaymentRequestBatchDTO, PaymentBatchReportDTO>chunk(6, txManager) // define el tipo de entrada y salida
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public RestTemplate restTemplate() { //Para hacer llamadas HTTP a servicios externos desde batch
        return new RestTemplate();
    }
}
