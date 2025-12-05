package com.payservice.paymentbatch.job;

import com.payservice.paymentbatch.dto.PaymentRequestBatchDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PaymentItemReaderTest {

    PaymentItemReader reader;

    @BeforeEach
    void setup() {
        reader = new PaymentItemReader();
    }

    @Test
    void read_validCsv_returnsCorrectValues() throws Exception {

        String csv = """
        customerId,serviceId,receiptId,amount,currency
        1,2,300,150.50,PEN
        """;

        reader.setResource(new InputStreamResource(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))
        ));

        reader.open(new ExecutionContext());

        PaymentRequestBatchDTO item = reader.read();

        assertNotNull(item);
        assertEquals(1, item.getCustomerId());
        assertEquals(2, item.getServiceId());
        assertEquals(300, item.getReceiptId());
        assertEquals(new BigDecimal("150.50"), item.getAmount());
        assertEquals("PEN", item.getCurrency());

        assertNull(reader.read()); // Ya no hay más registros
    }

    // 2. ARCHIVO VACÍO
    @Test
    void read_emptyCsv_returnsNull() throws Exception {

        String csv = "customerId,serviceId,receiptId,amount,currency\n";

        reader.setResource(new InputStreamResource(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))
        ));

        reader.open(new ExecutionContext());

        assertNull(reader.read());
    }

    // 3. LÍNEA CON COLUMNAS INCOMPLETAS
    @Test
    void read_malformedLine_throwsException() {

        String csv = """
        customerId,serviceId,receiptId,amount,currency
        1,2,300,150.50
        """; // Falta moneda

        reader.setResource(new InputStreamResource(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))
        ));

        reader.open(new ExecutionContext());

        assertThrows(Exception.class, () -> reader.read());
    }

    // 4. AMOUNT NO NUMÉRICO
    @Test
    void read_invalidAmount_throwsException() {

        String csv = """
        customerId,serviceId,receiptId,amount,currency
        1,2,300,ABC,PEN
        """;

        reader.setResource(new InputStreamResource(
                new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8))
        ));

        reader.open(new ExecutionContext());

        assertThrows(Exception.class, () -> reader.read());
    }

    // 5. RESOURCE NULO EN EXECUTION
    @Test
    void read_noResourceConfigured_throwsException() {
        reader = new PaymentItemReader(); // Crea uno nuevo con resource por defecto
        reader.setResource(null);

        assertThrows(Exception.class, () -> reader.open(new ExecutionContext()));
    }
}
