package com.payservice.paymentbatch.config;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JobRunner implements CommandLineRunner { // CommandLineRunner se ejecuta automáticamente al iniciar la app.
    //La clase JobRunner sirve para ejecutar el job automáticamente cuando arranca Spring Boot.
    private final JobLauncher jobLauncher;
    private final Job paymentJob;

    public JobRunner(JobLauncher jobLauncher, Job paymentJob) {
        this.jobLauncher = jobLauncher;
        this.paymentJob = paymentJob;
    }

    @Override
    public void run(String... args) throws Exception { // Se ejecuta automaticamente al iniciat la app
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis()) // asegurarnos de que cada ejecución sea única
                .toJobParameters();

        jobLauncher.run(paymentJob, params); // Lanza el job paymentJob con los parámetros que definimos. El job empezará a ejecutar el Step
    }
}
