package com.billing.software.Service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class FileStorageService {

    private final Path invoiceFolder = Paths.get("invoices");

    public FileStorageService() throws IOException {
        if (!Files.exists(invoiceFolder)) {
            Files.createDirectory(invoiceFolder);
        }
    }

    public String saveInvoice(byte[] pdf, String fileName) throws IOException {
        Path filePath = invoiceFolder.resolve(fileName);
        Files.write(filePath, pdf);
        return filePath.toString();
    }
}

