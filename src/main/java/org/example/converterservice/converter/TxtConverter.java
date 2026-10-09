package org.example.converterservice.converter;

import lombok.SneakyThrows;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class TxtConverter implements Converter {

    @Override
    public boolean supports(String fileType) {
        return "txt".equalsIgnoreCase(fileType);
    }

    @SneakyThrows
    @Override
    public byte[] convert(InputStream inputStream) {
        String text = readText(inputStream);

        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             InputStream fontStream = getFontStream()) {

            PDPage page = createPage(document);
            PDType0Font font = PDType0Font.load(document, fontStream);
            writeTextToPage(document, page, font, text);

            document.save(outputStream);

            return outputStream.toByteArray();
        }
    }

    @SneakyThrows
    private String readText(InputStream inputStream) {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    private PDPage createPage(PDDocument document) {
        PDPage page = new PDPage();
        document.addPage(page);
        return page;
    }

    private InputStream getFontStream() {
        InputStream fontStream = getClass().getResourceAsStream("/fonts/DejaVuSans.ttf");

        if (fontStream == null) {
            throw new IllegalStateException("Шрифт DejaVuSans.ttf не найден");
        }

        return fontStream;
    }

    @SneakyThrows
    private void writeTextToPage(PDDocument document, PDPage page, PDType0Font font, String text) {
        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

            contentStream.beginText();
            contentStream.setFont(font, 12);
            contentStream.newLineAtOffset(50, 750);

            for (String line : text.split("\\R")) {
                contentStream.showText(line);
                contentStream.newLineAtOffset(0, -15);
            }

            contentStream.endText();
        }
    }
}
