package com.brianuceda.sserafimflow.controllers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.brianuceda.sserafimflow.enums.LogType;

@RestController
@RequestMapping("/api/v1/logs")
public class _LogController {
  @Value("${IS_PRODUCTION}")
  private Boolean isProduction;
  
  private final String correctPassword;

  public _LogController() {
    this.correctPassword = "admin123";
  }
  
  @GetMapping()
  public ResponseEntity<?> loadLogs(
      @RequestParam(required = false) String password,
      @RequestParam(required = false) LogType type) {
    
    // Development mode
    if (this.isProduction == false) {
      return loadLogs(type);
    }
    
    // Production mode
    else {
      if (password != null && password.equals(correctPassword)) {
        return loadLogs(type);
      } else {
        return new ResponseEntity<String>("Recurso no disponible", HttpStatus.FORBIDDEN);
      }
    }
  }

  private ResponseEntity<?> loadLogs(LogType type) {
    try {
      String plainLogs = readFileContent("logs/all.log");

      if (type != null) {
        if (type == LogType.ALL) {
          plainLogs = readFileContent("logs/all.log");
        } else if (type == LogType.USER) {
          plainLogs = readFileContent("logs/user.log");
        } else {
          return new ResponseEntity<String>("Tipo de log inválido", HttpStatus.BAD_REQUEST);
        }
      }
      
      String formattedLogs = formatTextWithStylesAndJs(plainLogs);

      return new ResponseEntity<String>(formattedLogs, HttpStatus.OK);
    } catch (Exception e) {
      return new ResponseEntity<String>("Error al leer el log", HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  private String readFileContent(String filePath) throws IOException {
    return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
  }

  private String formatTextWithStylesAndJs(String plainLogs) {
    // Colores
    String bgColor = "#3C3C3C"; // Gris oscuro
    String textColor = "#d9d9d9"; // Gris claro
    String textHoverColor = "#a6a6a6"; // Gris oscuro
    String warnColor = "#ffcfa5"; // Naranja claro
    String errorColor = "#ffa5a5"; // Rojo claro
    String successColor = "#a5ffd1"; // Verde claro
    
    String fontSize = "14px";
    String fontFamily = "system-ui, cursive, -apple-system, sans-serif";

    // Cambios de color
    String formattedLogs = plainLogs
        .replaceAll("(?m)^\\d{2}:\\d{2}:\\d{2}\\.\\d{3} \\[.*?\\] INFO .*Tomcat started on port \\d+.*", "<span style=\"color: " + successColor + ";\">$0</span>")
        .replaceAll("(?m)^\\d{2}:\\d{2}:\\d{2}\\.\\d{3} \\[.*?\\] WARN (.*)", "<span style=\"color: " + warnColor + ";\">$0</span>")
        .replaceAll("(?m)^\\d{2}:\\d{2}:\\d{2}\\.\\d{3} \\[.*?\\] ERROR (.*)", "<span style=\"color: " + errorColor + ";\">$0</span>")
        .replaceAll("(?m)^(?!\\d{2}:\\d{2}:\\d{2}\\.\\d{3}).*", "<span style=\"color: " + errorColor + ";\">$0</span>");


    // Variables
    if (formattedLogs.isEmpty()) {
      formattedLogs = "No hay logs para mostrar.";
    } else {
      // Contadores
      int warnCount = (int) plainLogs.lines().filter(line -> line.matches("^\\d{2}:\\d{2}:\\d{2}\\.\\d{3} \\[.*?\\] WARN .*")).count();
      int errorCount = (int) plainLogs.lines().filter(line -> line.matches("^\\d{2}:\\d{2}:\\d{2}\\.\\d{3} \\[.*?\\] ERROR .*")).count();
  
      if (warnCount > 0 || errorCount > 0)  {
        // Separador
        formattedLogs += "<div style=\"margin: 16px 0; width: 100%; height: 1px; background-color: " + textColor + ";\"></div>";

        // Contador de advertencias y errores
        if (warnCount > 0)  {
          formattedLogs += "<p style=\"color: " + warnColor + "; margin: 0;\">Advertencias: <span style=\"color: " + textColor + " ;\">" + warnCount + "</span></p>";
        }
        if (errorCount > 0)  {
          formattedLogs += "<p style=\"color: " + errorColor + "; margin: 0;\">Errores: <span style=\"color: " + textColor + " ;\">" + errorCount + "</span></p>";
        }

        // Separador
        formattedLogs += "<div style=\"margin-top: 16px; width: 100%; height: 1px; background-color: " + textColor + ";\"></div>";
      }
    }

    // HTML y CSS
    formattedLogs =
      "<a title=\"Creador de este Log\" href=\"https://github.com/brianuceda\" target=\"_blank\" style=\"width: 40px; height: 40px; position: fixed; top: 15px; right: 15px; border-radius: 50%; overflow: hidden; transition: transform 0.3s;\" onmouseover=\"this.style.transform='scale(1.05)';\" onmouseout=\"this.style.transform='scale(1)';\">" +
        "<img src=\"https://brianuceda.vercel.app/assets/my-avatar.png\" style=\"width: 100%; height: 100%;\" />" +
      "</a>" +
      "<div style=\"padding: 8px; min-height: calc(100dvh - 16px);\">" +
        "<p style=\"margin: 0\">" + formattedLogs.replace("\n", "<br>") + "</p>" +
      "</div>";

    // JavaScript
    formattedLogs +=
      "<script>" +
        // Scroll bar to bottom of page
        "window.onload = function() {" +
          "setTimeout(function() {" +
            "window.scrollTo(0, document.body.scrollHeight);" +
          "}, 1);" +
        "};" +

        "const style = document.createElement('style');" +
        "style.innerHTML = `" +

          // Global Styles
          "html, body {" +
          "  scroll-behavior: smooth;" +
          "  margin: 0;" +
          "  background-color: " + bgColor + ";" +
          "  color: " + textColor + ";" +
          "  font-size: " + fontSize + ";" +
          "  font-family: " + fontFamily + ";" +
          "  overflow-x: hidden;" +
          "}" +

          // Scrollbar style
          "::-webkit-scrollbar {" +
          "  width: 8px;" +
          "}" +
          "::-webkit-scrollbar-track {" +
          "  background: " + bgColor + ";" +
          "}" +
          "::-webkit-scrollbar-thumb {" +
          "  background: " + textColor + ";" +
          "  border-radius: 4px;" +
          "}" +
          "::-webkit-scrollbar-thumb:hover {" +
          "  background: " + textHoverColor + ";" +
          "}" +

        "`;" +
        "document.head.appendChild(style);" +
      "</script>";
            
    return formattedLogs;
  }
}