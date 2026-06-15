package com.arteisis.service;

import com.arteisis.model.dto.LoginRequest;
import com.arteisis.model.dto.RegisterRequest;
import com.arteisis.model.dto.TokenResponse;
import com.arteisis.model.entity.AppUser;
import com.arteisis.model.entity.Customer;
import com.arteisis.model.entity.Role;
import com.arteisis.repository.AppUserRepository;
import com.arteisis.repository.CustomerRepository;
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
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final NotificationMailService notificationMailService;

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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já registrado");
        }
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        String name = request.fullName() == null ? null : request.fullName().trim();
        user.setFullName(name == null || name.isEmpty() ? null : name);
        String phone = normalizeOptionalBrazilPhone(request.phone());
        user.setPhone(phone);
        appUserRepository.save(user);

        // Garante que o cliente apareça na aba de clientes do admin
        String customerName = (user.getFullName() != null) ? user.getFullName() : email;
        String customerPhone = (phone != null) ? phone : "";
        customerRepository.findByEmailIgnoreCase(email).ifPresentOrElse(
                c -> {
                    c.setName(customerName);
                    if (phone != null) c.setPhone(phone);
                    customerRepository.save(c);
                },
                () -> {
                    Customer c = new Customer();
                    c.setName(customerName);
                    c.setEmail(email);
                    c.setPhone(customerPhone);
                    customerRepository.save(c);
                });

        notificationMailService.sendWelcome(email, user.getFullName());
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
