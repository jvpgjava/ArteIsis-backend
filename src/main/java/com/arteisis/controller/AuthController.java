package com.arteisis.controller;

import com.arteisis.model.dto.AuthMeResponse;
import com.arteisis.model.dto.LoginRequest;
import com.arteisis.model.dto.RegisterRequest;
import com.arteisis.model.dto.TokenResponse;
import com.arteisis.model.entity.AppUser;
import com.arteisis.repository.AppUserRepository;
import com.arteisis.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AppUserRepository appUserRepository;

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest body) {
        return authService.login(body);
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse register(@Valid @RequestBody RegisterRequest body) {
        return authService.register(body);
    }

    @GetMapping("/me")
    public AuthMeResponse me(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        String email = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .findFirst()
                .map(a -> a.substring("ROLE_".length()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        AppUser user = appUserRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return new AuthMeResponse(email, role, user.getPhone());
    }
}
