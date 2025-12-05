package com.payservice.paymentbatch.job;

import com.payservice.paymentbatch.dto.PaymentBatchReportDTO;
import com.payservice.paymentbatch.dto.ReceiptInfoDTO;
import org.junit.jupiter.api.*;
import org.springframework.batch.item.Chunk;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentItemWriterTest {

    private PaymentItemWriter writer;
    private Path testDir;
    private Path testFile;

    @BeforeEach
    void setUp() throws Exception {

        // Crear carpeta "output-test"
        testDir = Paths.get("output-test");
        Files.createDirectories(testDir);


        // Apuntar al archivo fake
        testFile = testDir.resolve("payment-report.csv");

        // Asegurar inicio limpio
        if (Files.exists(testFile)) {
            Files.delete(testFile);
        }

        writer = new PaymentItemWriter();

        // REEMPLAZAR outputPath mediante reflexión
        Field field = PaymentItemWriter.class.getDeclaredField("outputPath");
        field.setAccessible(true);
        field.set(writer, testFile);   // <<<<<< Hack limpio

        // Ejecutamos init() para simular comportamiento real
        writer.init();
    }

    @AfterEach
    void clean() throws IOException {
        // Limpiar archivos creados
        if (Files.exists(testFile)) Files.delete(testFile);
        if (Files.exists(testDir)) Files.delete(testDir);
    }


    @Test
    void testInitCreaHeader() throws Exception {
        assertTrue(Files.exists(testFile));

        List<String> lines = Files.readAllLines(testFile);
        assertEquals(1, lines.size());
        assertTrue(lines.get(0).contains("customerId,serviceId,receiptId"));
    }

    @Test
    void testWriteAgregaLinea() throws Exception {
        // DTO de prueba
        PaymentBatchReportDTO dto = new PaymentBatchReportDTO();
        dto.setCustomerId(10);
        dto.setServiceId(20);
        dto.setReceiptId(30);
        dto.setAttemptedAmount(new BigDecimal(150.75));
        dto.setPaymentStatus("PROCESSED");
        dto.setBatchStatus("OK");
        dto.setReason("Todo bien");

        Chunk<PaymentBatchReportDTO> chunk = Chunk.of(dto);

        writer.write(chunk);

        List<String> lines = Files.readAllLines(testFile);

        // Debe haber header + 1 línea
        assertEquals(2, lines.size());

        String bodyLine = lines.get(1);
        assertTrue(bodyLine.contains("10,20,30,150.75"));
        assertTrue(bodyLine.contains("PROCESSED"));
        assertTrue(bodyLine.contains("OK"));
        assertTrue(bodyLine.contains("Todo bien"));
    }

    @Test
    void testInitCuandoArchivoYaExiste_noEscribeHeader() throws Exception {
        // Crear archivo antes de llamar init()
        Files.createDirectories(testFile.getParent());
        Files.writeString(testFile, "HEADER_EXISTENTE\n");

        // Ejecutar init() nuevamente
        writer.init();

        // Validar que NO se escribió un header nuevo
        List<String> lines = Files.readAllLines(testFile);
        assertEquals(1, lines.size());
        assertEquals("HEADER_EXISTENTE", lines.get(0));
    }

    @Test
    void testWriteConReceiptNoNull() throws Exception {
        // Crear recibo simulado
        ReceiptInfoDTO receipt = new ReceiptInfoDTO();
        receipt.setReceiptNumber("REC-100");
        receipt.setPeriodLabel("2025-01");
        receipt.setReceiptAmount(new BigDecimal(200.50));
        receipt.setPendingAmount(new BigDecimal(50));
        receipt.setCurrency("PEN");

        // Crear DTO
        PaymentBatchReportDTO dto = new PaymentBatchReportDTO();
        dto.setCustomerId(1);
        dto.setServiceId(2);
        dto.setReceiptId(3);
        dto.setAttemptedAmount(new BigDecimal(150));
        dto.setReceipt(receipt);
        dto.setPaymentStatus("PAID");
        dto.setBatchStatus("OK");
        dto.setReason("Todo OK");

        Chunk<PaymentBatchReportDTO> chunk = Chunk.of(dto);

        writer.write(chunk);

        List<String> lines = Files.readAllLines(testFile);

        // Primera línea = header
        assertEquals(2, lines.size());

        String line = lines.get(1);

        assertTrue(line.contains("REC-100"));
        assertTrue(line.contains("2025-01"));
        assertTrue(line.contains("200.50"));
        assertTrue(line.contains("50.00"));
        assertTrue(line.contains("PEN"));
    }




}
