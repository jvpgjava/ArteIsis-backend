package com.arteisis.controller;

import com.arteisis.model.dto.ContactRequest;
import com.arteisis.service.ContactMailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/contact")
@Validated
@RequiredArgsConstructor
public class PublicContactController {

    private final ContactMailService contactMailService;

    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void submit(@Valid @RequestBody ContactRequest body) {
        contactMailService.sendContactEmail(body);
    }
}
