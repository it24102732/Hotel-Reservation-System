package com.hotelmanagement.system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class FileUploadService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.max-size:5242880}") // 5MB default
    private long maxFileSize;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    /**
     * Upload file with validation
     */
    public String uploadFile(MultipartFile file) throws IOException {
        // Validation 1: Check if file is empty
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validation 2: Check file size
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " + (maxFileSize / 1024 / 1024) + "MB");
        }

        // Validation 3: Check file content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Only images (JPEG, PNG, GIF, WebP) are allowed");
        }

        // Validation 4: Check file extension
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("Invalid file extension. Allowed: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // Validation 5: Sanitize filename to prevent path traversal
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Filename contains invalid path sequence");
        }

        // Create directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String newFilename = UUID.randomUUID().toString() + "." + fileExtension;

        // Save the file
        Path filePath = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return the URL path
        return "/api/files/" + newFilename;
    }

    /**
     * Delete file
     */
    public boolean deleteFile(String filename) throws IOException {
        if (filename == null || filename.contains("..")) {
            throw new IllegalArgumentException("Invalid filename");
        }

        Path filePath = Paths.get(uploadDir).resolve(filename).normalize();

        if (!filePath.startsWith(Paths.get(uploadDir).toAbsolutePath())) {
            throw new IllegalArgumentException("Access denied");
        }

        return Files.deleteIfExists(filePath);
    }
}