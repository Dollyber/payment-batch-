package com.payservice.paymentbatch.job;

import com.payservice.paymentbatch.dto.PaymentRequestBatchDTO;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Component;


@Component
public class PaymentItemReader extends FlatFileItemReader<PaymentRequestBatchDTO> {
    //FlatFileItemReader es un lector de archivos planos de Spring Batch
    //Define que cada línea del archivo se convertirá en un objeto PaymentRequestBatchDTO

    public PaymentItemReader() {

        setName("paymentItemReader"); //Nombre del Reader
        setResource(new FileSystemResource("input/payments.csv")); // Archivo CSV que se va a leer
        setLinesToSkip(1); //Se salta encabezados

        DefaultLineMapper<PaymentRequestBatchDTO> lineMapper = new DefaultLineMapper<>(); // Convierte cada línea del archivo en un objeto Java (PaymentRequestBatchDTO).

        // Tokenizer
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setNames("customerId", "serviceId", "receiptId", "amount", "currency");
        tokenizer.setDelimiter(",");

        // FieldSetMapper -> Convierte los valores del CSV (strings) en tipos de datos del DTO.
        lineMapper.setFieldSetMapper(field -> new PaymentRequestBatchDTO(
                field.readInt("customerId"),
                field.readInt("serviceId"),
                field.readInt("receiptId"),
                field.readBigDecimal("amount"),
                field.readString("currency")
        ));

        lineMapper.setLineTokenizer(tokenizer); // Le dice al lineMapper cómo dividir la línea en columnas.
        setLineMapper(lineMapper);
    }

}

