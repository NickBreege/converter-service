package org.example.converterservice.converter;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
@RequiredArgsConstructor
public class ZipConverter implements Converter {
    private final List<Converter> converters;

    @Override
    public boolean supports(String fileType) {
        return "zip".equalsIgnoreCase(fileType);
    }

    @Override
    @SneakyThrows
    public byte[] convert(InputStream inputStream) {
        PDFMergerUtility merger = new PDFMergerUtility();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        merger.setDestinationStream(outputStream);

        try (ZipInputStream zipInputStream = new ZipInputStream(inputStream)) {
            ZipEntry entry;

            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                String fileType = getFileType(entry.getName());
                Converter converter = findConverter(fileType);

                if (converter == null) {
                    continue;
                }

                convertEntry(zipInputStream, converter, merger);
                zipInputStream.closeEntry();
            }
        }

        merger.mergeDocuments(null);
        return outputStream.toByteArray();
    }

    private String getFileType(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    private Converter findConverter(String fileType) {
        return converters.stream()
                .filter(converter -> converter.supports(fileType))
                .findFirst()
                .orElse(null);
    }

    @SneakyThrows
    private void convertEntry(ZipInputStream zipInputStream, Converter converter, PDFMergerUtility merger) {
        byte[] fileContent = zipInputStream.readAllBytes();

        try (InputStream fileInputStream = new ByteArrayInputStream(fileContent)) {
            byte[] pdf = converter.convert(fileInputStream);
            merger.addSource(new RandomAccessReadBuffer(pdf));
        }
    }
}
