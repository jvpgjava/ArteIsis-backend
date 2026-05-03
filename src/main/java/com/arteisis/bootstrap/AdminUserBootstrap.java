package com.arteisis.bootstrap;

import com.arteisis.model.entity.AppUser;
import com.arteisis.model.entity.Role;
import com.arteisis.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminUserBootstrap implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (appUserRepository.count() > 0) {
            return;
        }
        AppUser admin = new AppUser();
        admin.setEmail("admin@arteisis.local");
        admin.setPasswordHash(passwordEncoder.encode("admin"));
        admin.setRole(Role.ADMIN);
        admin.setFullName("Administrador");
        appUserRepository.save(admin);
        log.warn(
                "Base sem utilizadores: foi criado admin@arteisis.local com palavra-passe \"admin\". Altere em produção.");
    }
}
