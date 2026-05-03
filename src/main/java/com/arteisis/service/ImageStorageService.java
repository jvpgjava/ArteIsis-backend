package com.arteisis.service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ImageStorageService {

    private static final long MAX_BYTES = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");

    private final Path root;

    public ImageStorageService(@Value("${arteisis.upload.dir:data/uploads}") String uploadDir) {
        this.root = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() {
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            throw new IllegalStateException("Não foi possível criar a pasta de upload: " + root, e);
        }
    }


    public String storeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ficheiro em falta");
        }
        if (file.getSize() > MAX_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Imagem demasiado grande (máx. 5 MB)");
        }
        String ext = resolveExtension(file);
        String filename = UUID.randomUUID() + "." + ext;
        Path target = root.resolve(filename).normalize();
        if (!target.startsWith(root)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome inválido");
        }
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Não foi possível guardar a imagem");
        }
        return "/api/public/media/" + filename;
    }

    private static String resolveExtension(MultipartFile file) {
        String original = file.getOriginalFilename();
        String fromName = null;
        if (original != null && original.contains(".")) {
            fromName = original.substring(original.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
            if ("jpeg".equals(fromName)) {
                fromName = "jpg";
            }
        }
        String ct = file.getContentType() == null ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        String fromCt =
                switch (ct) {
                    case "image/jpeg", "image/jpg" -> "jpg";
                    case "image/png" -> "png";
                    case "image/webp" -> "webp";
                    default -> null;
                };
        String ext = fromCt != null ? fromCt : fromName;
        if (ext == null || !ALLOWED_EXT.contains(ext)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Formato não permitido. Usa JPG, PNG ou WEBP.");
        }
        return ext;
    }

    public Path resolveExistingFile(String filename) {
        if (filename == null
                || filename.contains("..")
                || !filename.matches("(?i)[0-9a-f\\-]{36}\\.(jpg|jpeg|png|webp)")) {
            return null;
        }
        String name = filename.toLowerCase(Locale.ROOT);
        Path target = root.resolve(name).normalize();
        if (!target.startsWith(root) || !Files.isRegularFile(target)) {
            return null;
        }
        return target;
    }
}
