package hu.tamas.szakdolgozatjava.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class ProfileImageService {

    private final Path uploadDir = Paths.get("uploads", "profiles");

    public String saveProfileImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String ext = "";

        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) {
            ext = original.substring(dot).toLowerCase();
        }

        // Minimális "kép" ellenőrzés (ne legyen kötelező, de hasznos)
        // Ha már van nálad validáció events képeknél, ezt akár ki is veheted.
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Csak képfájl tölthető fel!");
        }

        Files.createDirectories(uploadDir);

        String filename = UUID.randomUUID() + ext;
        Path target = uploadDir.resolve(filename);

        // felülírás védelem
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // WebConfig-od miatt /uploads/** elérhető, így ez működni fog:
        return "/uploads/profiles/" + filename;
    }

    public void deleteIfExists(String profileImagePath) {
        if (profileImagePath == null || profileImagePath.isBlank()) return;

        // profileImagePath: "/uploads/profiles/xyz.png"
        String prefix = "/uploads/profiles/";
        if (!profileImagePath.startsWith(prefix)) return;

        String filename = profileImagePath.substring(prefix.length());
        Path target = uploadDir.resolve(filename);

        try {
            Files.deleteIfExists(target);
        } catch (IOException ignored) {
            // nem kritikus
        }
    }
}
