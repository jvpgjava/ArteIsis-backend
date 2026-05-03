package com.arteisis.controller;

import com.arteisis.service.ImageStorageService;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/public/media")
@RequiredArgsConstructor
public class PublicMediaController {

    private final ImageStorageService imageStorageService;

    @GetMapping("/{filename:.+}")
    public ResponseEntity<Resource> get(@PathVariable String filename) {
        Path path = imageStorageService.resolveExistingFile(filename);
        if (path == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        FileSystemResource resource = new FileSystemResource(path.toFile());
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        String lower = filename.toLowerCase();
        if (lower.endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (lower.endsWith(".webp")) {
            mediaType = MediaType.parseMediaType("image/webp");
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            mediaType = MediaType.IMAGE_JPEG;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .contentType(mediaType)
                .body(resource);
    }
}
