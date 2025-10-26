package com.hotelmanagement.system.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${file.max-size:5242880}") // 5MB default
    private long maxFileSize;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp");

    private Path rootLocation;

    @PostConstruct
    public void init() throws IOException {
        this.rootLocation = Paths.get(uploadDir);
        Files.createDirectories(rootLocation);
    }

    public Map<String, Object> store(MultipartFile file) throws IOException {
        // --- VALIDATION ---
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty. Please select a valid file.");
        }
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of " + (maxFileSize / 1024 / 1024) + "MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Only images (JPEG, PNG, GIF, WebP) are allowed.");
        }
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (originalFilename.contains("..")) {
            throw new IllegalArgumentException("Filename contains invalid path sequence.");
        }
        String fileExtension = StringUtils.getFilenameExtension(originalFilename);
        if (fileExtension == null || !ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file extension. Allowed: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // --- FILE SAVING ---
        String newFilename = UUID.randomUUID().toString() + "." + fileExtension.toLowerCase();
        Path destinationFile = this.rootLocation.resolve(Paths.get(newFilename)).normalize().toAbsolutePath();

        // Security check to ensure file is stored within the upload directory
        if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
            throw new IllegalArgumentException("Cannot store file outside current directory.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }

        // --- RESPONSE ---
        Map<String, Object> response = new HashMap<>();
        response.put("message", "File uploaded successfully");
        response.put("filename", newFilename);
        response.put("url", "/api/files/" + newFilename);
        response.put("contentType", contentType);
        response.put("size", file.getSize());
        return response;
    }

    public Path load(String filename) {
        return rootLocation.resolve(filename);
    }

    public Resource loadAsResource(String filename) {
        try {
            Path file = load(filename);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read file: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not read file: " + filename, e);
        }
    }

    public void delete(String filename) throws IOException {
        if (filename == null || filename.contains("..")) {
            throw new IllegalArgumentException("Invalid filename.");
        }
        Path file = load(filename);
        if (!Files.exists(file)) {
            throw new IllegalArgumentException("File not found: " + filename);
        }
        Files.delete(file);
    }

    public String probeContentType(Path filePath) throws IOException {
        String contentType = Files.probeContentType(filePath);
        return contentType == null ? "application/octet-stream" : contentType;
    }
}