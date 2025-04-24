package xyz.brianuceda.sserafimflow.utils;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import xyz.brianuceda.sserafimflow.exceptions.SecurityExceptions.ProtectedResource;
import xyz.brianuceda.sserafimflow.exceptions.SecurityExceptions.SQLInjectionException;

import java.util.Arrays;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class DataUtils {
  public static void verifyAllowedOrigin(HttpServletRequest request) {
    final List<String> ALLOWED_ORIGINS = Arrays.asList(
        "https://sserafimflow.vercel.app",
        "http://localhost:4200");

    String origin = request.getHeader("Origin");

    if (origin == null || !ALLOWED_ORIGINS.contains(origin)) {
      throw new ProtectedResource("Acceso no autorizado");
    }
  }

  public static void verifySQLInjection(String str) {
    if (str.trim().matches(".*(--|[;+*^$|?{}\\[\\]()'\"\\']).*")
        || str.trim().contains("SELECT") || str.trim().contains("DELETE")
        || str.trim().contains("UPDATE") || str.trim().contains("INSERT")
        || str.trim().contains("DROP") || str.trim().contains(" OR ")) {
      throw new SQLInjectionException("Esas cosas son del diablo");
    }
  }

  public static boolean isValidEmail(String email) {
    email = email.trim();
    if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
      return false;
    } else {
      return true;
    }
  }

  public static boolean isValidPassword(String password) {
    // Reglas para la contraseña:
    // - Mínimo 6 caracteres
    // - Al menos 1 letra minúscula
    // - Al menos 1 letra mayúscula
    // - Al menos 1 número
    // - Debe contener al menos un carácter especial
    // - No permitir comillas simples o dobles

    if (password == null) {
        return false;
    }
    
    password = password.trim();
    
    // Validar que la contraseña no contenga comillas simples o dobles
    if (password.contains("'") || password.contains("\"")) {
        return false;
    }
    
    // Validar longitud mínima
    if (password.length() < 6) {
        return false;
    }
    
    // Validar que contenga al menos una letra minúscula
    if (!password.matches(".*[a-z].*")) {
        return false;
    }
    
    // Validar que contenga al menos una letra mayúscula
    if (!password.matches(".*[A-Z].*")) {
        return false;
    }
    
    // Validar que contenga al menos un número
    if (!password.matches(".*\\d.*")) {
        return false;
    }
    
    // Validar que contenga al menos un carácter especial
    if (!password.matches(".*[!@#$%^&*()\\-+_=.;:,<>?/\\\\|{}\\[\\]].*")) {
        return false;
    }
    
    return true;
  }

  public static void isSupportedImage(MultipartFile image) {
    // Tipos MIME y extensiones permitidas
    final List<String> SUPPORTED_MIME_TYPES = List.of("image/png", "image/jpeg", "image/jpg");
    final List<String> SUPPORTED_EXTENSIONS = List.of(".png", ".jpg", ".jpeg");

    // Validar tipo MIME
    String contentType = image.getContentType();
    if (!SUPPORTED_MIME_TYPES.contains(contentType)) {
      throw new IllegalArgumentException("Formatos permitidos: PNG, JPG y JPEG");
    }

    // Validar extensión del archivo
    String fileName = image.getOriginalFilename();
    if (fileName == null) {
      throw new IllegalArgumentException("Nombre de archivo inválido");
    }

    String lowerCaseFileName = fileName.toLowerCase();
    boolean isValidExtension = SUPPORTED_EXTENSIONS.stream()
        .anyMatch(lowerCaseFileName::endsWith);

    if (!isValidExtension) {
      throw new IllegalArgumentException("Formatos permitidos: PNG, JPG y JPEG");
    }

    if (image.getSize() > 2 * 1024 * 1024) {
      throw new IllegalArgumentException("Tamaño máximo de imagen: 2 MB");
    }
  }
}
