package org.example.converterservice.converter;

import lombok.SneakyThrows;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Component;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Component
public class ImageConverter implements Converter {

    @Override
    public boolean supports(String fileType) {
        return "png".equalsIgnoreCase(fileType) || "jpg".equalsIgnoreCase(fileType);
    }

    @Override
    @SneakyThrows
    public byte[] convert(InputStream inputStream) {
        BufferedImage image = readImage(inputStream);

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = createPage(document, image);
            addImageToPage(document, page, image);
            document.save(outputStream);

            return outputStream.toByteArray();
        }
    }

    @SneakyThrows
    private BufferedImage readImage(InputStream inputStream) {
        BufferedImage image = ImageIO.read(inputStream);

        if (image == null) {
            throw new IllegalArgumentException("Не удалось прочитать изображение");
        }
        return image;
    }

    private PDPage createPage(PDDocument document, BufferedImage image) {
        PDRectangle pageSize = new PDRectangle(image.getWidth(), image.getHeight());
        PDPage page = new PDPage(pageSize);
        document.addPage(page);
        return page;
    }

    @SneakyThrows
    private void addImageToPage(PDDocument document, PDPage page, BufferedImage image) {
        PDImageXObject pdfImage = LosslessFactory.createFromImage(document, image);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            contentStream.drawImage(pdfImage, 0, 0, image.getWidth(), image.getHeight());
        }
    }
}
