package com.arteisis.controller;

import com.arteisis.model.dto.ImageUploadResponse;
import com.arteisis.service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/upload")
@Validated
@RequiredArgsConstructor
public class AdminUploadController {

    private final ImageStorageService imageStorageService;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImageUploadResponse uploadImage(@RequestPart("file") MultipartFile file) {
        String url = imageStorageService.storeImage(file);
        return new ImageUploadResponse(url);
    }
}
