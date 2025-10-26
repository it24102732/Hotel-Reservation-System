package com.hotelmanagement.system.controller;

import com.hotelmanagement.system.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileUploadController {

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Handles file upload, delegates logic to FileStorageService.
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> response = fileStorageService.store(file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to store file. Please try again."));
        }
    }

    /**
     * Serves the uploaded file.
     */
    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        try {
            Resource file = fileStorageService.loadAsResource(filename);
            String contentType = fileStorageService.probeContentType(file.getFile().toPath());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(file);
        } catch (RuntimeException | IOException e) {
            // Throws a 404 Not Found if the resource doesn't exist or there's an error.
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found", e);
        }
    }

    /**
     * Deletes the specified file.
     */
    @DeleteMapping("/{filename:.+}")
    public ResponseEntity<Map<String, String>> deleteFile(@PathVariable String filename) {
        try {
            fileStorageService.delete(filename);
            return ResponseEntity.ok(Map.of("message", "File deleted successfully: " + filename));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Could not delete file: " + filename));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}