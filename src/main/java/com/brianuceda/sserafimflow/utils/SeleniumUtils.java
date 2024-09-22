package com.brianuceda.sserafimflow.utils;

import java.net.URL;
import java.time.Duration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.github.bonigarcia.wdm.WebDriverManager;

@Component
public class SeleniumUtils {
  @Value("${IS_PRODUCTION}")
  private boolean isProduction;
  
  public final static Duration TIMEOUT = Duration.ofSeconds(5);

  public ThreadLocal<RemoteWebDriver> setUp(ThreadLocal<RemoteWebDriver> driver) throws Exception {
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--start-maximized"); // Maximizar ventana
    options.addArguments("--disable-notifications"); // Desactivar notificaciones
    
    if (this.isProduction) {      
      options.addArguments("--headless"); // Si estás corriendo en un entorno sin GUI
      options.addArguments("--no-sandbox"); // Desactivar sandbox
      options.addArguments("--disable-dev-shm-usage"); // Desactivar uso de memoria compartida
      options.addArguments("--remote-allow-origins=*"); // Permitir orígenes remotos

      // Configura la URL del Selenium Hub (cambia la URL si es necesario)
      driver.set(new RemoteWebDriver(new URL("http://selenium-hub:4444"), options));
    } else {
      WebDriverManager.chromedriver().setup();
      driver.set(new ChromeDriver(options));
    }

    return driver;
  }

  public void closeBrowser(ThreadLocal<RemoteWebDriver> driver) {
    driver.get().quit();
    driver.remove();
  }
  
  // Espera hasta que el texto de un elemento con id específico cambie
  public static void waitUntilTextChanges(WebDriver driver, By locator) {
    WebDriverWait wait = new WebDriverWait(driver, SeleniumUtils.TIMEOUT);
    WebElement element = driver.findElement(locator);
    String initialText = element.getText().trim();
    wait.until(ExpectedConditions.not(ExpectedConditions.textToBe(locator, initialText)));
  }
}