package com.arteisis.service;

import com.arteisis.model.dto.LoginRequest;
import com.arteisis.model.dto.RegisterRequest;
import com.arteisis.model.dto.TokenResponse;
import com.arteisis.model.entity.AppUser;
import com.arteisis.model.entity.Role;
import com.arteisis.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        AppUser user = appUserRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciais inválidas");
        }
        return buildTokenResponse(user);
    }

    @Transactional
    public TokenResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já registado");
        }
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        String name = request.fullName() == null ? null : request.fullName().trim();
        user.setFullName(name == null || name.isEmpty() ? null : name);
        user.setPhone(normalizeOptionalBrazilPhone(request.phone()));
        appUserRepository.save(user);
        return buildTokenResponse(user);
    }

    private static String normalizeOptionalBrazilPhone(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String digits = raw.replaceAll("\\D", "");
        if (digits.isEmpty()) {
            return null;
        }
        if (digits.length() < 10 || digits.length() > 11) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Telefone inválido");
        }
        return digits;
    }

    private TokenResponse buildTokenResponse(AppUser user) {
        String token = jwtService.createToken(user);
        return new TokenResponse(token, "Bearer", jwtService.getExpirationSeconds());
    }
}
