package org.example.converterservice.service;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioService {
    private final MinioClient minioClient;

    public InputStream download(String bucket, String filePath) {
        try {
            log.info("MINIO - начало скачивания файла. bucket={}, filePath={}", bucket, filePath);

            GetObjectResponse inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(filePath)
                            .build()
            );

            log.info("MINIO - поток файла успешно получен. bucket={}, filePath={}", bucket, filePath);

            return inputStream;

        } catch (Exception e) {
            log.error("MINIO - ошибка скачивания файла. bucket={}, filePath={}", bucket, filePath, e);

            throw new RuntimeException("Не удалось скачать файл из MinIO: " + filePath, e);
        }
    }

    public void upload(String bucket, String filePath, byte[] content) {
        try {
            log.info("MINIO - начало загрузки файла. bucket={}, filePath={}, size={} bytes",
                    bucket,
                    filePath,
                    content.length);

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(filePath)
                    .stream(new ByteArrayInputStream(content), content.length, -1)
                    .contentType("application/pdf")
                    .build()
            );

            log.info("MINIO - файл успешно загружен. bucket={}, filePath={}, size={} bytes",
                    bucket,
                    filePath,
                    content.length);

        } catch (Exception e) {
            log.error("MINIO - ошибка загрузки файла. bucket={}, filePath={}", bucket, filePath, e);

            throw new RuntimeException("Не удалось загрузить файл в MinIO: " + filePath, e);
        }
    }
}
