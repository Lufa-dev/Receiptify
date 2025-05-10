package com.thesis.receiptify.service;

import io.minio.*;
import io.minio.errors.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Service responsible for managing file storage operations using MinIO.
 * Handles uploading, retrieving, and deleting files like recipe images.
 */
@Service
public class FileStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    public FileStorageService(
            @Value("${minio.url}") String minioUrl,
            @Value("${minio.access-key}") String accessKey,
            @Value("${minio.secret-key}") String secretKey) {
        this.minioClient = MinioClient.builder()
                .endpoint(minioUrl)
                .credentials(accessKey, secretKey)
                .build();
    }

    /**
     * Initializes MinIO bucket if it doesn't exist.
     * Sets up appropriate access policies for public read access.
     *
     * @throws RuntimeException if bucket initialization fails
     */
    public void init() {
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());

            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());

                // Set bucket policy to allow public read access if needed
                String policy =
                        "{\n" +
                                "    \"Version\": \"2012-10-17\",\n" +
                                "    \"Statement\": [\n" +
                                "        {\n" +
                                "            \"Effect\": \"Allow\",\n" +
                                "            \"Principal\": \"*\",\n" +
                                "            \"Action\": [\"s3:GetObject\"],\n" +
                                "            \"Resource\": [\"arn:aws:s3:::" + bucketName + "/*\"]\n" +
                                "        }\n" +
                                "    ]\n" +
                                "}";

                minioClient.setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(bucketName)
                        .config(policy)
                        .build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error initializing MinIO bucket: " + e.getMessage(), e);
        }
    }

    /**
     * Stores a file in MinIO and returns the object name.
     * Generates a unique filename using UUID to prevent collisions.
     *
     * @param file The multipart file to store
     * @return The generated object name (UUID with file extension)
     * @throws IOException if file storage fails
     */
    public String storeFile(MultipartFile file) throws IOException {
        try {
            // Generate unique filename
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String fileExtension = getFileExtension(originalFileName);
            String objectName = UUID.randomUUID().toString() + fileExtension;

            // Upload to MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .contentType(file.getContentType())
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .build());

            return objectName;
        } catch (Exception e) {
            throw new IOException("Failed to store file in MinIO", e);
        }
    }

    /**
     * Gets a direct URL for an object in the MinIO bucket.
     *
     * @param objectName The object name/key
     * @return Full URL to access the file
     */
    public String getDirectFileUrl(String objectName) {
        return minioUrl + "/" + bucketName + "/" + objectName;
    }

    /**
     * Deletes a file from MinIO.
     *
     * @param objectName The object name/key to delete
     * @throws RuntimeException if deletion fails
     */
    public void deleteFile(String objectName) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException("Error deleting file: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts the file extension from a filename.
     *
     * @param fileName The filename
     * @return The file extension with dot (e.g., ".jpg") or empty string if no extension
     */
    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.lastIndexOf(".") == -1) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }
}
